package com.hbm.items.special;

import com.hbm.items.ItemSounds;
import com.hbm.items.machine.ItemBattery;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * The potato battery. Held in hand it discharges tirelessly, pitch following charge — fuller means shriller.
 * Two hundred ticks plus a random add between cries, so a pile of potatoes doesn't sing in unison.
 */
public class ItemPotatos extends ItemBattery {

	private static final SoundEvent RANDOM = ItemSounds.resolve("potatos.random");

	public ItemPotatos(long dura, long chargeRate, long dischargeRate) {
		super(dura, chargeRate, dischargeRate);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {

		if(getCharge(stack) == 0) return;

		if(getTimer(stack) > 0) {
			setTimer(stack, getTimer(stack) - 1);
			return;
		}

		if(entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;

			if(player.getHeldItem(EnumHand.MAIN_HAND) == stack || player.getHeldItem(EnumHand.OFF_HAND) == stack) {

				float pitch = (float) getCharge(stack) / (float) this.getMaxCharge(stack) * 0.5F + 0.5F;

				world.playSound(null, player.posX, player.posY, player.posZ, RANDOM, SoundCategory.PLAYERS, 1.0F, pitch);
				setTimer(stack, 200 + itemRand.nextInt(100));
			}
		}
	}

	private static int getTimer(ItemStack stack) {

		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getInteger("timer");
	}

	private static void setTimer(ItemStack stack, int i) {

		if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("timer", i);
	}
}
