package com.hbm.items.tool;

import java.util.List;

import org.apache.logging.log4j.Level;

import com.hbm.config.GeneralConfig;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IBomb.BombReturnCode;
import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.util.ChatBuilder;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The remote detonator. Sneak-right-click a block to record its coordinates; that later right-click "beep" is the detonation.
 *
 * Coordinates store as an integer triple on the item NBT (old saves carry the same shape; don't change the keys); detonation goes through the block's own
 * `IBomb.explode`, with the block reporting success — this only narrates the result to the player.
 */
public class ItemDetonator extends ItemBase {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add("Shift right-click to set position,");
		list.add("right-click to detonate!");
		if(stack.getTagCompound() == null) {
			list.add(TextFormatting.RED + "No position set!");
		} else {
			NBTTagCompound tag = stack.getTagCompound();
			list.add(TextFormatting.YELLOW + "Linked to " + tag.getInteger("x") + ", " + tag.getInteger("y") + ", " + tag.getInteger("z"));
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float cx, float cy, float cz) {

		ItemStack stack = player.getHeldItem(hand);

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		if(!player.isSneaking())
			return EnumActionResult.PASS;

		stack.getTagCompound().setInteger("x", pos.getX());
		stack.getTagCompound().setInteger("y", pos.getY());
		stack.getTagCompound().setInteger("z", pos.getZ());

		if(!world.isRemote) {
			player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
					.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
					.next("] ").color(TextFormatting.DARK_AQUA)
					.next("Position set!").color(TextFormatting.GREEN).flush());
		}

		world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BOOP), SoundCategory.PLAYERS, 2.0F, 1.0F);

		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(stack.getTagCompound() == null) {
			if(!world.isRemote) {
				player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("No position set!").color(TextFormatting.RED).flush());
			}
		} else {
			int x = stack.getTagCompound().getInteger("x");
			int y = stack.getTagCompound().getInteger("y");
			int z = stack.getTagCompound().getInteger("z");
			BlockPos target = new BlockPos(x, y, z);

			if(world.getBlockState(target).getBlock() instanceof IBomb) {
				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BLEEP), SoundCategory.PLAYERS, 1.0F, 1.0F);
				if(!world.isRemote) {
					BombReturnCode ret = ((IBomb) world.getBlockState(target).getBlock()).explode(world, target);

					if(GeneralConfig.enableExtendedLogging)
						MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");

					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.nextTranslation(ret.getUnlocalizedMessage()).color(ret.wasSuccessful() ? TextFormatting.YELLOW : TextFormatting.RED).flush());
				}

			} else {
				if(!world.isRemote) {
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.nextTranslation(BombReturnCode.ERROR_NO_BOMB.getUnlocalizedMessage()).color(TextFormatting.RED).flush());
				}
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}
}
