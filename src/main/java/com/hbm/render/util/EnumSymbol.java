package com.hbm.render.util;

public enum EnumSymbol {
	NONE(0, 0),
	RADIATION(195, 2),
	NOWATER(195, 63),
	ACID(195, 124),
	ASPHYXIANT(195, 185),
	CROYGENIC(134, 185),
	ANTIMATTER(73, 185),
	OXIDIZER(12, 185);

	// Pixel offsets of the hazard diamonds in the icon sheet, for the layout code under render.util
	public int x;
	public int y;

	private EnumSymbol(int x, int y) {
		this.x = x;
		this.y = y;
	}
}
