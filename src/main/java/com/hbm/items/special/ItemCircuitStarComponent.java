package com.hbm.items.special;

import com.hbm.items.ItemEnumMulti;

/** The star circuit's four-tier parts. Named like RAM sticks, but just four different circuit components. */
public class ItemCircuitStarComponent extends ItemEnumMulti {

	public ItemCircuitStarComponent() {
		super(CircuitComponentType.class, true, true);
	}

	public static enum CircuitComponentType {
		CHIPSET,
		CPU,
		RAM,
		CARD
	}
}
