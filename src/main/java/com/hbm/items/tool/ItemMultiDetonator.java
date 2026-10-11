package com.hbm.items.tool;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
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
 * The multi detonator. Sneak-right-click **adds one coordinate to the list**, mid-air sneak-right-click clears, right-click fires them all.
 *
 * Coordinates store as three int arrays xValues/yValues/zValues (that's the save shape); length mismatch means
 * no locations ({@link #getLocations}'s criterion) — no guessing which end is authoritative.
 */
public class ItemMultiDetonator extends ItemBase {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add("Shift right-click block to add position,");
		list.add("right-click to detonate!");
		list.add("Shift right-click in the air to clear positions.");

		if(stack.getTagCompound() == null || getLocations(stack) == null) {
			list.add(TextFormatting.RED + "No position set!");
		} else {

			int[][] locs = getLocations(stack);

			for(int i = 0; i < locs[0].length; i++) {

				list.add(TextFormatting.YELLOW + "" + locs[0][i] + " / " + locs[1][i] + " / " + locs[2][i]);
			}
		}
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float cx, float cy, float cz) {

		ItemStack stack = player.getHeldItem(hand);

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		if(!player.isSneaking())
			return EnumActionResult.PASS;

		addLocation(stack, pos.getX(), pos.getY(), pos.getZ());

		if(!world.isRemote) {
			player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
					.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
					.next("] ").color(TextFormatting.DARK_AQUA)
					.next("Position added!").color(TextFormatting.GREEN).flush());
		}

		world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BOOP), SoundCategory.PLAYERS, 2.0F, 1.0F);

		return EnumActionResult.SUCCESS;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(stack.getTagCompound() == null || getLocations(stack) == null) {

			if(!world.isRemote) {
				player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
						.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
						.next("] ").color(TextFormatting.DARK_AQUA)
						.next("No position set!").color(TextFormatting.RED).flush());
			}

		} else {

			if(!player.isSneaking()) {
				int[][] locs = getLocations(stack);

				int succ = 0;

				for(int i = 0; i < locs[0].length; i++) {

					int x = locs[0][i];
					int y = locs[1][i];
					int z = locs[2][i];

					if(world.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof IBomb) {

						if(!world.isRemote) {
							BombReturnCode ret = ((IBomb) world.getBlockState(new BlockPos(x, y, z)).getBlock()).explode(world, new BlockPos(x, y, z));

							if(ret.wasSuccessful())
								succ++;

							if(GeneralConfig.enableExtendedLogging)
								MainRegistry.logger.log(Level.INFO, "[DET] Tried to detonate block at " + x + " / " + y + " / " + z + " by " + player.getDisplayName() + "!");
						}
					}
				}

				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BLEEP), SoundCategory.PLAYERS, 1.0F, 1.0F);

				if(!world.isRemote) {
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.next("Triggered " + succ + "/" + locs[0].length + "!").color(TextFormatting.YELLOW).flush());
				}

			} else {

				NBTTagCompound tag = stack.getTagCompound();
				tag.setIntArray("xValues", new int[0]);
				tag.setIntArray("yValues", new int[0]);
				tag.setIntArray("zValues", new int[0]);

				world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve(NTMSounds.TECH_BOOP), SoundCategory.PLAYERS, 2.0F, 1.0F);

				if(!world.isRemote) {
					player.sendMessage(ChatBuilder.start("[").color(TextFormatting.DARK_AQUA)
							.nextTranslation(this.getUnlocalizedName() + ".name").color(TextFormatting.DARK_AQUA)
							.next("] ").color(TextFormatting.DARK_AQUA)
							.next("Locations cleared!").color(TextFormatting.RED).flush());
				}
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	private static void addLocation(ItemStack stack, int x, int y, int z) {

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		NBTTagCompound tag = stack.getTagCompound();

		int[] xs = tag.getIntArray("xValues");
		int[] ys = tag.getIntArray("yValues");
		int[] zs = tag.getIntArray("zValues");

		tag.setIntArray("xValues", ArrayUtils.add(xs, x));
		tag.setIntArray("yValues", ArrayUtils.add(ys, y));
		tag.setIntArray("zValues", ArrayUtils.add(zs, z));
	}

	private static int[][] getLocations(ItemStack stack) {

		if(!stack.hasTagCompound())
			return null;

		NBTTagCompound tag = stack.getTagCompound();

		int[] xs = tag.getIntArray("xValues");
		int[] ys = tag.getIntArray("yValues");
		int[] zs = tag.getIntArray("zValues");

		if(xs.length == 0 || ys.length == 0 || zs.length == 0) {
			return null;
		}

		return new int[][] { xs, ys, zs };
	}
}
