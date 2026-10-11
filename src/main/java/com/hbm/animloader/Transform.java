package com.hbm.animloader;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.Vec3d;

/**
 * Keyframe translate/scale/rotate. Rotation interpolates as a quaternion (slerp), translation and scale lerp on their own,
 * skipping BobMathUtil here saves two vector allocations per bone per frame.
 */
public class Transform {

	protected static FloatBuffer auxGLMatrix = GLAllocation.createDirectFloatBuffer(16);

	Vec3d scale;
	Vec3d translation;
	Quaternion rotation;

	boolean hidden = false;

	public Transform(float[] matrix) {
		scale = getScaleFromMatrix(matrix);
		auxGLMatrix.put(matrix);
		auxGLMatrix.rewind();
		rotation = new Quaternion().setFromMatrix((Matrix4f) new Matrix4f().load(auxGLMatrix));
		translation = new Vec3d(matrix[0 * 4 + 3], matrix[1 * 4 + 3], matrix[2 * 4 + 3]);
		auxGLMatrix.rewind();
	}

	private Vec3d getScaleFromMatrix(float[] matrix) {

		float scaleX = (float) Math.sqrt(matrix[0] * matrix[0] + matrix[1] * matrix[1] + matrix[2] * matrix[2]);
		float scaleY = (float) Math.sqrt(matrix[4] * matrix[4] + matrix[5] * matrix[5] + matrix[6] * matrix[6]);
		float scaleZ = (float) Math.sqrt(matrix[8] * matrix[8] + matrix[9] * matrix[9] + matrix[10] * matrix[10]);

		matrix[0] = matrix[0] / scaleX;
		matrix[1] = matrix[1] / scaleX;
		matrix[2] = matrix[2] / scaleX;

		matrix[4] = matrix[4] / scaleY;
		matrix[5] = matrix[5] / scaleY;
		matrix[6] = matrix[6] / scaleY;

		matrix[8] = matrix[8] / scaleZ;
		matrix[9] = matrix[9] / scaleZ;
		matrix[10] = matrix[10] / scaleZ;

		return new Vec3d(scaleX, scaleY, scaleZ);
	}

	public void interpolateAndApply(Transform other, float inter) {

		Vec3d trans = lerp(this.translation, other.translation, inter);
		Vec3d scale = lerp(this.scale, other.scale, inter);
		Quaternion rot = slerp(rotation, other.rotation, inter);

		quatToGlMatrix(auxGLMatrix, rot);
		applyScale(auxGLMatrix, scale);
		auxGLMatrix.put(12, (float) trans.x);
		auxGLMatrix.put(13, (float) trans.y);
		auxGLMatrix.put(14, (float) trans.z);

		GlStateManager.multMatrix(auxGLMatrix);
	}

	private static Vec3d lerp(Vec3d a, Vec3d b, float inter) {
		return new Vec3d(a.x + (b.x - a.x) * inter, a.y + (b.y - a.y) * inter, a.z + (b.z - a.z) * inter);
	}

	public static FloatBuffer quatToGlMatrix(FloatBuffer buf, Quaternion q) {

		buf.clear();
		float xx = q.x * q.x;
		float xy = q.x * q.y;
		float xz = q.x * q.z;
		float xw = q.x * q.w;
		float yy = q.y * q.y;
		float yz = q.y * q.z;
		float yw = q.y * q.w;
		float zz = q.z * q.z;
		float zw = q.z * q.w;

		buf.put(1.0F - 2.0F * (yy + zz));
		buf.put(2.0F * (xy + zw));
		buf.put(2.0F * (xz - yw));
		buf.put(0.0F);

		buf.put(2.0F * (xy - zw));
		buf.put(1.0F - 2.0F * (xx + zz));
		buf.put(2.0F * (yz + xw));
		buf.put(0.0F);

		buf.put(2.0F * (xz + yw));
		buf.put(2.0F * (yz - xw));
		buf.put(1.0F - 2.0F * (xx + yy));
		buf.put(0.0F);

		buf.put(0.0F);
		buf.put(0.0F);
		buf.put(0.0F);
		buf.put(1.0F);

		buf.rewind();
		return buf;
	}

	private static void applyScale(FloatBuffer matrix, Vec3d scale) {

		matrix.put(0, (float) (matrix.get(0) * scale.x));
		matrix.put(4, (float) (matrix.get(4) * scale.x));
		matrix.put(8, (float) (matrix.get(8) * scale.x));
		matrix.put(12, (float) (matrix.get(12) * scale.x));

		matrix.put(1, (float) (matrix.get(1) * scale.y));
		matrix.put(5, (float) (matrix.get(5) * scale.y));
		matrix.put(9, (float) (matrix.get(9) * scale.y));
		matrix.put(13, (float) (matrix.get(13) * scale.y));

		matrix.put(2, (float) (matrix.get(2) * scale.z));
		matrix.put(6, (float) (matrix.get(6) * scale.z));
		matrix.put(10, (float) (matrix.get(10) * scale.z));
		matrix.put(14, (float) (matrix.get(14) * scale.z));
	}

	// Quaternion slerp lifted off Wikipedia, don't ask me why it looks like this
	protected Quaternion slerp(Quaternion v0, Quaternion v1, float t) {

		double dot = Quaternion.dot(v0, v1);

		// Negative dot product means we're on the long arc; flip v1 to take the short one
		if(dot < 0.0f) {
			v1 = new Quaternion(-v1.x, -v1.y, -v1.z, -v1.w);
			dot = -dot;
		}

		final double DOT_THRESHOLD = 0.9999999;

		// Slerp goes numerically unstable when the two poses nearly coincide; degrade to lerp then normalize
		if(dot > DOT_THRESHOLD) {
			Quaternion result = new Quaternion(v0.x + t * v1.x, v0.y + t * v1.y, v0.z + t * v1.z, v0.w + t * v1.w);
			result.normalise();
			return result;
		}

		double theta_0 = Math.acos(dot);
		double theta = theta_0 * t;
		double sin_theta = Math.sin(theta);
		double sin_theta_0 = Math.sin(theta_0);

		float s0 = (float) (Math.cos(theta) - dot * sin_theta / sin_theta_0);
		float s1 = (float) (sin_theta / sin_theta_0);

		return new Quaternion(s0 * v0.x + s1 * v1.x,
				s0 * v0.y + s1 * v1.y,
				s0 * v0.z + s1 * v1.z,
				s0 * v0.w + s1 * v1.w);
	}
}
