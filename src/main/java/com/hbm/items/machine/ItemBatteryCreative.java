package com.hbm.items.machine;

import com.hbm.items.ItemBase;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.item.ItemStack;

/** The creative battery: fills nothing, drains forever — an infinitely long power cord in essence. */
public class ItemBatteryCreative extends ItemBase implements IBatteryItem {

	@Override public void chargeBattery(ItemStack stack, long i) { }
	@Override public void setCharge(ItemStack stack, long i) { }
	@Override public void dischargeBattery(ItemStack stack, long i) { }

	@Override public long getCharge(ItemStack stack) { return Long.MAX_VALUE / 2L; }
	@Override public long getMaxCharge(ItemStack stack) { return Long.MAX_VALUE; }

	@Override public long getChargeRate(ItemStack stack) { return Long.MAX_VALUE / 100L; }
	@Override public long getDischargeRate(ItemStack stack) { return Long.MAX_VALUE / 100L; }
}
