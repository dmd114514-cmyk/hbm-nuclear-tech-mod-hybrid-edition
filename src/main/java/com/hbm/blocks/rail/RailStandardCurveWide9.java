package com.hbm.blocks.rail;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** The 9-wide standard curve: the old version's check table and lay table for this tier were already the same cells, merged into one entry here */
public class RailStandardCurveWide9 extends RailStandardCurveBase {

	private static final int[][] ARC_WIDE9 = {
		{1, 0, 0}, {2, 0, 0}, {0, 1, 1}, {1, 1, 0}, {2, 1, 0}, {3, 1, 0}, {4, 1, 0},
		{2, 2, 1}, {3, 2, 1}, {4, 2, 1}, {5, 2, 0}, {4, 3, 1}, {5, 3, 1}, {5, 4, 1},
		{6, 3, 0}, {6, 4, 0}, {7, 4, 0}, {6, 5, 1}, {7, 5, 1}, {6, 6, 1}, {7, 6, 1},
		{7, 7, 1}, {7, 8, 1}, {8, 6, 0}, {8, 7, 0}, {8, 8, 0},
	};

	public RailStandardCurveWide9() {
		super();
		this.width = 8;
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, EnumFacing dir, int o) {
		return arcFits(world, x, y, z, dir, ARC_WIDE9);
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, EnumFacing dir, int o) {
		placeArc(world, x, y, z, dir, ARC_WIDE9);
	}
}
