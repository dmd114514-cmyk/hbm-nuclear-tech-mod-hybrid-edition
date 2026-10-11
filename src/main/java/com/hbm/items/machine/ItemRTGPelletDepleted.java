package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;

/** The spent RTG pellet. One use left — recycling — so it's just a per-element subtype container. */
public class ItemRTGPelletDepleted extends ItemEnumMulti {

	public ItemRTGPelletDepleted() {
		super(DepletedRTGMaterial.class, true, true);
	}

	public enum DepletedRTGMaterial {
		BISMUTH,
		MERCURY,
		NEPTUNIUM,
		LEAD,
		ZIRCONIUM,
		AMERICIUM,
		NICKEL;
	}
}
