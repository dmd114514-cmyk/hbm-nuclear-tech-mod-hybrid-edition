package com.hbm.util.fauxpointtwelve;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;

/** A coordinate with a facing, for laying multiblock cells one by one */
public class DirPos extends BlockPos {

	protected EnumFacing dir;

	public DirPos(int x, int y, int z, EnumFacing dir) {
		super(x, y, z);
		this.dir = dir;
	}

	public DirPos(TileEntity te, EnumFacing dir) {
		super(te);
		this.dir = dir;
	}

	public DirPos(double x, double y, double z, EnumFacing dir) {
		super(x, y, z);
		this.dir = dir;
	}

	/**
	 * Horizontal rotation delegates to vanilla `Rotation.rotate(EnumFacing)`: it returns up/down unchanged,
	 * matching "spin about the vertical axis". Only 180° needs getOpposite —
	 * a half turn about the vertical doesn't swap sky for ground, and here full reversal is what's wanted.
	 */
	@Override
	public DirPos rotate(Rotation rotationIn) {
		switch(rotationIn) {
		case NONE:
		default: return this;
		case CLOCKWISE_90: return new DirPos(-this.getZ(), this.getY(), this.getX(), Rotation.CLOCKWISE_90.rotate(this.dir));
		case CLOCKWISE_180: return new DirPos(-this.getX(), this.getY(), -this.getZ(), this.dir.getOpposite());
		case COUNTERCLOCKWISE_90: return new DirPos(this.getZ(), this.getY(), -this.getX(), Rotation.COUNTERCLOCKWISE_90.rotate(this.dir));
		}
	}

	public EnumFacing getDir() {
		return this.dir;
	}
}
