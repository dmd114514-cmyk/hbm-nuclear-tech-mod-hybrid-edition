package com.hbm.handler;

import net.minecraft.util.EnumFacing;

/**
 * Three ints packed into a comparable, HashMap-ready coordinate value object.
 * Structure gen, air masses and range tools all key on it, so equals/hashCode must change as a pair.
 */
public class ThreeInts implements Comparable<ThreeInts> {

	public int x;
	public int y;
	public int z;

	public ThreeInts(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		result = prime * result + z;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		ThreeInts other = (ThreeInts) obj;
		if(x != other.x)
			return false;
		if(y != other.y)
			return false;
		if(z != other.z)
			return false;
		return true;
	}

	// The old compareTo only told equal from unequal (enough for set dedup); don't use it for sorting
	@Override
	public int compareTo(ThreeInts o) {
		return equals(o) ? 0 : 1;
	}

	public ThreeInts getPositionAtOffset(int dx, int dy, int dz) {
		return new ThreeInts(dx + x, dy + y, dz + z);
	}

	// The old version passed ForgeDirection, this one EnumFacing, and offsets are read differently (getFrontOffset*)
	public ThreeInts getPositionAtOffset(EnumFacing dir) {
		return new ThreeInts(dir.getFrontOffsetX() + x, dir.getFrontOffsetY() + y, dir.getFrontOffsetZ() + z);
	}

	public double getDistanceSquared(ThreeInts to) {
		int ox = x - to.x, oy = y - to.y, oz = z - to.z;
		return ox * ox + oy * oy + oz * oz;
	}

	public double getDistance(ThreeInts to) {
		return Math.sqrt(getDistanceSquared(to));
	}

}
