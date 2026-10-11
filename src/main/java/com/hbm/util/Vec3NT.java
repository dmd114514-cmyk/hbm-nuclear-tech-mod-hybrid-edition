package com.hbm.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * A mutable 3D vector.
 *
 * Deliberately **not** extending Vec3d: 1.12.2's Vec3d components are final, a subclass can't touch them,
 * while this class's whole purpose is "reuse one instance, rewrite repeatedly" to stop hot-path allocation storms.
 * Convert with toVec3d() when handing one to a vanilla API.
 */
public class Vec3NT {

	public double x;
	public double y;
	public double z;

	public Vec3NT() { this(0, 0, 0); }

	public Vec3NT(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3NT(Vec3d vec) {
		this(vec.x, vec.y, vec.z);
	}

	public Vec3NT(Entity entity) {
		this(entity.posX, entity.posY, entity.posZ);
	}

	public static Vec3NT from(Vec3d vec) {
		return new Vec3NT(vec);
	}

	public Vec3d toVec3d() {
		return new Vec3d(x, y, z);
	}

	public Vec3NT eyeHeight(Entity entity) { return this.add(0, entity.getEyeHeight(), 0); }

	public Vec3NT halfHeight(Entity entity) { return this.add(0, entity.height / 2D, 0); }

	public Vec3NT normalizeSelf() {
		double len = Math.sqrt(x * x + y * y + z * z);
		return len < 1.0E-4D ? multiply(0D) : multiply(1D / len);
	}

	public Vec3NT add(double dx, double dy, double dz) {
		this.x += dx;
		this.y += dy;
		this.z += dz;
		return this;
	}

	public Vec3NT add(Vec3d vec) {
		return this.add(vec.x, vec.y, vec.z);
	}

	public Vec3NT multiply(double m) {
		this.x *= m;
		this.y *= m;
		this.z *= m;
		return this;
	}

	public Vec3NT multiply(double mx, double my, double mz) {
		this.x *= mx;
		this.y *= my;
		this.z *= mz;
		return this;
	}

	public double distanceTo(double tx, double ty, double tz) {
		double dX = tx - x, dY = ty - y, dZ = tz - z;
		return Math.sqrt(dX * dX + dY * dY + dZ * dZ);
	}

	public Vec3NT setComponents(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vec3NT rotateAroundXRad(double alpha) {
		double cos = Math.cos(alpha), sin = Math.sin(alpha);
		return setComponents(x, y * cos + z * sin, z * cos - y * sin);
	}

	public Vec3NT rotateAroundYRad(double alpha) {
		double cos = Math.cos(alpha), sin = Math.sin(alpha);
		return setComponents(x * cos + z * sin, y, z * cos - x * sin);
	}

	public Vec3NT rotateAroundZRad(double alpha) {
		double cos = Math.cos(alpha), sin = Math.sin(alpha);
		return setComponents(x * cos + y * sin, y * cos - x * sin, z);
	}

	public Vec3NT rotateAroundXDeg(double alpha) { return rotateAroundXRad(alpha / 180D * Math.PI); }
	public Vec3NT rotateAroundYDeg(double alpha) { return rotateAroundYRad(alpha / 180D * Math.PI); }
	public Vec3NT rotateAroundZDeg(double alpha) { return rotateAroundZRad(alpha / 180D * Math.PI); }

	public static double getMinX(Vec3NT... vecs) {
		double min = Double.POSITIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.x < min) min = vec.x;
		return min;
	}

	public static double getMinY(Vec3NT... vecs) {
		double min = Double.POSITIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.y < min) min = vec.y;
		return min;
	}

	public static double getMinZ(Vec3NT... vecs) {
		double min = Double.POSITIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.z < min) min = vec.z;
		return min;
	}

	public static double getMaxX(Vec3NT... vecs) {
		double max = Double.NEGATIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.x > max) max = vec.x;
		return max;
	}

	public static double getMaxY(Vec3NT... vecs) {
		double max = Double.NEGATIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.y > max) max = vec.y;
		return max;
	}

	public static double getMaxZ(Vec3NT... vecs) {
		double max = Double.NEGATIVE_INFINITY;
		for(Vec3NT vec : vecs) if(vec.z > max) max = vec.z;
		return max;
	}
}
