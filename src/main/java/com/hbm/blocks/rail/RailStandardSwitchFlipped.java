package com.hbm.blocks.rail;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

/**
 * The mirrored switch: the branch merges in from the other side, the node table mirrors with it, and the laid branch placeholders hug the other edge.
 *
 * One leftover: the footprint check still uses the normal switch's math (the old two already disagreed; copied as-is, not "fixed" on my own),
 * so it demands one cell more of clear space than it actually lays.
 */
public class RailStandardSwitchFlipped extends RailStandardSwitch {

	@Override
	protected int sidingBase() {
		return 1;
	}

	@Override
	protected EnumFacing sidingRotation(EnumFacing dir) {
		return dir.rotateYCCW();
	}

	@Override
	protected void defineNodes(RailDef main, RailDef side) {

		main.nodes.add(new Vec3d(-8.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(-7.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(6.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
		main.nodes.add(new Vec3d(8.5, 0.1875, 0.5));

		side.nodes.add(new Vec3d(-8.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-7.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-6.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-5.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-4.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-3.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-2.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-1.5, 0.1875, -3.5));
		side.nodes.add(new Vec3d(-0.5, 0.1875, -3.25));
		side.nodes.add(new Vec3d(0.5, 0.1875, -2.9375));
		side.nodes.add(new Vec3d(1.5, 0.1875, -2.375));
		side.nodes.add(new Vec3d(2.5, 0.1875, -1.4625));
		side.nodes.add(new Vec3d(3.5, 0.1875, -0.75));
		side.nodes.add(new Vec3d(4.5, 0.1875, -0.1875));
		side.nodes.add(new Vec3d(5.5, 0.1875, 0.175));
		side.nodes.add(new Vec3d(6.5, 0.1875, 0.375));
		side.nodes.add(new Vec3d(7.5, 0.1875, 0.5));
		side.nodes.add(new Vec3d(8.5, 0.1875, 0.5));
	}
}
