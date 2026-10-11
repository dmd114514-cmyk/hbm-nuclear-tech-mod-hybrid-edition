package com.hbm.blocks;

public class BlockEnums {

	public static enum EnumStoneType implements IEnumMulti {
		SULFUR,
		ASBESTOS,
		HEMATITE,
		MALACHITE,
		LIMESTONE,
		BAUXITE,
		CONGLOMERATE,
		CALCIUM,
	}

	public static enum EnumMeteorType implements IEnumMulti {
		IRON,
		COPPER,
		ALUMINIUM,
		RAREEARTH,
		COBALT
	}

	public static enum EnumBiomeType implements IEnumMulti {
		DESERT,
		WOODLAND
	}

	public static enum EnumStalagmiteType implements IEnumMulti {
		SULFUR,
		ASBESTOS,
		FLESH,
		ICE,
		SNOW,
		GLYPHID1,
		GLYPHID2,
		GLYPHID3
	}

	public static enum EnumCMMaterials implements IEnumMulti {
		STEEL,
		ALLOY,
		DESH,
		TCALLOY
	}

	public static enum EnumCMEngines implements IEnumMulti {
		STANDARD,
		DESH,
		BISMUTH
	}

	public static enum EnumCMCircuit implements IEnumMulti {
		ALUMINIUM,
		COPPER,
		RED_COPPER,
		GOLD,
		SCHRABIDIUM
	}

	/** The enums for deco blocks and structures */
	//an early apology to my future self

	public static enum TileType implements IEnumMulti {
		LARGE,
		SMALL
	}

	public static enum LightstoneType implements IEnumMulti {
		UNREFINED,
		TILE,
		BRICKS,
		BRICKS_CHISELED,
		CHISELED
	}

	public static enum DecoComputerEnum implements IEnumMulti {
		IBM_300PL
	}

	public static enum DecoCabinetEnum implements IEnumMulti {
		GREEN,
		STEEL
	}

	public static enum LightType implements IEnumMulti {
		INCANDESCENT,
		FLUORESCENT,
		HALOGEN
	}

	public static enum PlatemetalType implements IEnumMulti {
		BASE,
		BLACK,
		WHITE,
		RED,
		GREEN,
		LIGHT_GRAY,
		BLUE,
		PURPLE,
		CYAN,
		PINK,
		LIME,
		YELLOW,
		LIGHT_BLUE,
		MAGENTA,
		ORANGE
	}
}
