package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;

/**
 * The armor battery. It changes no stats itself, only offering a capacity multiplier into the battery slot —
 * the powered armors are the ones actually reading that multiplier.
 */
public class ItemModBattery extends ItemArmorMod {

	public double mod;

	public ItemModBattery(double mod) {
		super(ArmorModHandler.battery, true, true, true, true);
		this.mod = mod;
	}
}
