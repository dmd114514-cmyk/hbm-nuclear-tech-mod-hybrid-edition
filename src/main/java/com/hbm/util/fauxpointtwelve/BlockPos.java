package com.hbm.util.fauxpointtwelve;

import api.hbm.net.Directions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;

/**
 * A reusable block coordinate value.
 *
 * Vanilla `net.minecraft.util.math.BlockPos` is immutable, but the use case here is "one instance moved around through a pile of checks" —
 * newing one per step throws memory away on thousand-player servers, hence a `mutate` setter.
 * Rotation borrows vanilla's `Rotation` (its constants match this mod's semantics); no second set.
 */
public class BlockPos implements Cloneable {

	private int x;
	private int y;
	private int z;

	public BlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public BlockPos(TileEntity te) {
		this(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
	}

	public BlockPos(double x, double y, double z) {
		this(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}

	/*Mutates in place and returns itself, pairing with the "one instance, many checks" pattern above */
	public BlockPos mutate(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public boolean compare(int x, int y, int z) {
		return this.x == x && this.y == y && this.z == z;
	}

	public BlockPos add(int x, int y, int z) {
		return x == 0 && y == 0 && z == 0 ? this : new BlockPos(this.getX() + x, this.getY() + y, this.getZ() + z);
	}

	public BlockPos add(double x, double y, double z) {
		return x == 0.0D && y == 0.0D && z == 0.0D ? this : new BlockPos((double) this.getX() + x, (double) this.getY() + y, (double) this.getZ() + z);
	}

	public BlockPos add(BlockPos vec) {
		return this.add(vec.getX(), vec.getY(), vec.getZ());
	}

	public BlockPos rotate(Rotation rotationIn) {
		switch(rotationIn) {
		case NONE:
		default: return this;
		case CLOCKWISE_90: return new BlockPos(-this.getZ(), this.getY(), this.getX());
		case CLOCKWISE_180: return new BlockPos(-this.getX(), this.getY(), -this.getZ());
		case COUNTERCLOCKWISE_90: return new BlockPos(this.getZ(), this.getY(), -this.getX());
		}
	}

	public BlockPos offset(EnumFacing dir) {
		return this.offset(dir, 1);
	}

	/*Offsets go through Directions rather than EnumFacing's offset getters, so mapping renames don't ripple here */
	public BlockPos offset(EnumFacing dir, int distance) {
		return new BlockPos(x + Directions.offsetX(dir) * distance, y + Directions.offsetY(dir) * distance, z + Directions.offsetZ(dir) * distance);
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getZ() {
		return this.z;
	}

	/*Serves as a HashMap key, so it computes its own cheap integer fingerprint instead of vanilla's long packing */
	@Override
	public int hashCode() {
		return getIdentity(this.getX(), this.getY(), this.getZ());
	}

	public static int getIdentity(int x, int y, int z) {
		return (y + z * 27644437) * 27644437 + x;
	}

	@Override
	public boolean equals(Object toCompare) {
		if(this == toCompare) {
			return true;
		} else if(!(toCompare instanceof BlockPos)) {
			return false;
		} else {
			BlockPos pos = (BlockPos) toCompare;
			if(this.getX() != pos.getX()) {
				return false;
			} else if(this.getY() != pos.getY()) {
				return false;
			} else {
				return this.getZ() == pos.getZ();
			}
		}
	}

	@Override
	public BlockPos clone() {
		try {
			return (BlockPos) super.clone();
		} catch(Exception x) { }
		return null;
	}
}
