package com.hbm.items;

import com.hbm.blocks.IEnumMulti;

/**
 * Piling all the multi-subtype enums into one class — unclear if that's wise;
 * but a class per item's enum defeats ItemEnumMulti's whole point.
 * Of two evils, pile them here.
 */
public class ItemEnums {

	public static enum EnumCokeType implements IEnumMulti {
		COAL,
		LIGNITE,
		PETROLEUM
	}

	public static enum EnumTarType implements IEnumMulti {
		CRUDE,
		CRACK,
		COAL,
		WOOD,
		WAX,
		PARAFFIN
	}

	public static enum EnumAshType implements IEnumMulti {
		WOOD,
		COAL,
		MISC,
		FLY,
		SOOT,
		FULLERENE
	}

	public static enum EnumBriquetteType implements IEnumMulti {
		COAL,
		LIGNITE,
		WOOD
	}

	public static enum EnumLegendaryType implements IEnumMulti {
		TIER1,
		TIER2,
		TIER3
	}

	public static enum EnumPlantType implements IEnumMulti {
		TOBACCO,
		ROPE,
		MUSTARDWILLOW,
	}


	public static enum EnumChunkType implements IEnumMulti {
		RARE,
		MALACHITE,
		CRYOLITE,
		PENTLANDITE,
		MOONSTONE
	}

	public static enum EnumAchievementType implements IEnumMulti {
		GOFISH,
		ACID,
		BALLS,
		DIGAMMASEE,
		DIGAMMAFEEL,
		DIGAMMAKNOW,
		DIGAMMAKAUAIMOHO,
		DIGAMMAUPONTOP,
		DIGAMMAFOROURRIGHT,
		QUESTIONMARK
	}

	public static enum EnumFuelAdditive implements IEnumMulti {
		ANTIKNOCK,
		DEICER
	}

	public static enum EnumPages implements IEnumMulti {
		PAGE1, PAGE2, PAGE3, PAGE4, PAGE5, PAGE6, PAGE7, PAGE8
	}

	public static enum EnumSecretType implements IEnumMulti {
		CANISTER, CONTROLLER, SELENIUM_STEEL, ABERRATOR, FOLLY
	}

	public static enum EnumCasingType implements IEnumMulti {
		SMALL, LARGE, SMALL_STEEL, LARGE_STEEL, SHOTSHELL, BUCKSHOT, BUCKSHOT_ADVANCED
	}

	public static enum EnumIngotMetal implements IEnumMulti {
		SCRAP, INGOT, COUNTER, KEY, BEACON, CASING, CLOCKWORK, BAR, DETECTOR
	}

	public static enum EnumExpensiveType implements IEnumMulti {
		STEEL_PLATING, HEAVY_FRAME, CIRCUIT, LEAD_PLATING, FERRO_PLATING, COMPUTER, BRONZE_TUBES, PLASTIC, GOLD_DUST, DEGENERATE_MATTER, STAINLESS_BEAM, AVIONICS, ASTRO
	}
}
