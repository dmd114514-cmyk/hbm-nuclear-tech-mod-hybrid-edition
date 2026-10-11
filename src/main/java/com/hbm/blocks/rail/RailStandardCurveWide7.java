package com.hbm.blocks.rail;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** The 7-wide standard curve: radius follows width, laying one ring more than the base class */
public class RailStandardCurveWide7 extends RailStandardCurveBase {

	/** The old lay table hand-wrote 21 cells, copied cell by cell; third column: 0=incoming 1=side */
	private static final int[][] ARC_WIDE7 = {
		{1, 0, 0}, {2, 0, 0}, {0, 1, 1}, {1, 1, 1}, {2, 1, 1}, {3, 1, 0}, {4, 1, 0},
		{2, 2, 1}, {3, 2, 0}, {4, 2, 0}, {5, 2, 0}, {3, 3, 1}, {4, 3, 0}, {5, 3, 0},
		{4, 4, 1}, {5, 4, 0}, {6, 4, 0}, {5, 5, 1}, {5, 6, 1}, {6, 5, 1}, {6, 6, 1},
	};

	public RailStandardCurveWide7() {
		super();
		this.width = 6;
	}

	// The 13 checked cells are verbatim identical to the base class in the old version (the 6-wide lay actually needs 21 cells; the old check never grew to match),
	// so only the lay is overridden here and the check stays on the base class — behavior identical to 1.7.10
	@Override
	protected void fillSpace(World world, int x, int y, int z, EnumFacing dir, int o) {
		placeArc(world, x, y, z, dir, ARC_WIDE7);
	}
}
