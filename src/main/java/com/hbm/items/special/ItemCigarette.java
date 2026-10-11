package com.hbm.items.special;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class ItemCigarette extends ItemBase {

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BOW;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 30;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {
		stack.shrink(1);

		if(!world.isRemote && entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;

			if(this == ModItems.cigarette) {
				HbmLivingProps.incrementBlackLung(player, 2000);
				HbmLivingProps.incrementAsbestos(player, 2000);
				HbmLivingProps.incrementRadiation(player, 100F);

				// Skipped (the old entry was the "smoking with a no9 mask" achievement check): no9 isn't registered, and this version's achievements moved to
				// Statistics, with no MainRegistry.achNo9 to trigger; add it back with the achievement name when no9 lands.
			}

			if(this == ModItems.crackpipe) {
				HbmLivingProps.incrementBlackLung(player, 500);
				player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 200, 0));
				player.heal(10F);
			}

			world.playSound(null, player.posX, player.posY, player.posZ, ItemSounds.resolve("player.cough"), SoundCategory.PLAYERS, 1.0F, 1.0F);

			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("type", "vomit");
			nbt.setString("mode", "smoke");
			nbt.setInteger("count", 30);
			nbt.setInteger("entity", player.getEntityId());
			AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(nbt, 0, 0, 0), new TargetPoint(player.dimension, player.posX, player.posY, player.posZ, 25));
		}

		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.cigarette) {
			list.add(TextFormatting.RED + "✓ Asbestos filter");
			list.add(TextFormatting.RED + "✓ High in tar");
			list.add(TextFormatting.RED + "✓ Tobacco contains 100% Polonium-210");
			list.add(TextFormatting.RED + "✓ Yum");
		} else {
			String[] colors = new String[] {
					TextFormatting.RED + "",
					TextFormatting.GOLD + "",
					TextFormatting.YELLOW + "",
					TextFormatting.GREEN + "",
					TextFormatting.AQUA + "",
					TextFormatting.BLUE + "",
					TextFormatting.DARK_PURPLE + "",
					TextFormatting.LIGHT_PURPLE + "",
			};
			int len = 2000;
			list.add("This can't be good for me, but I feel " + colors[(int)(System.currentTimeMillis() % len * colors.length / len)] + "GREAT");
		}
	}
}
