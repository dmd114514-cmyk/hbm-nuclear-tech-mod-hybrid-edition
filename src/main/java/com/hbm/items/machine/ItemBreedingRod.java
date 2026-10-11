package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;

/** The breeding rod. Seventeen kinds by element; single/double/quad are just three instances of one item. */
public class ItemBreedingRod extends ItemEnumMulti {

	public ItemBreedingRod() {
		super(BreedingRodType.class, true, true);
	}

	public enum BreedingRodType {
		LITHIUM,
		TRITIUM,
		CO,
		CO60,
		TH232,
		THF,
		U235,
		NP237,
		U238,
		PU238,
		PU239,
		RGP,
		WASTE,

		//Required for prototype
		LEAD,
		URANIUM,

		RA226,
		AC227
	}
}
