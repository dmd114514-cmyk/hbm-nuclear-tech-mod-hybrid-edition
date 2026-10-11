package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A billboard particle that spins about its own normal. The angle originally came from Entity.rotationPitch (1.12.2's Particle isn't an Entity),
 * so it carries its own rotationPitch/prevRotationPitch.
 *
 * Vertices go straight into the buffer ParticleManager has already begun: that layer's format is fixed at
 * PARTICLE_POSITION_TEX_COLOR_LMAP with GL_QUADS, and **no** begin/draw or GL state changes may happen in renderParticle,
 * or it bleeds into every particle on the layer.
 */
@SideOnly(Side.CLIENT)
public class EntityFXRotating extends Particle {

	public float rotationPitch;
	public float prevRotationPitch;

	protected EntityFXRotating(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	// The four corners of a unit square, readable as a rectangle on a plane rotated about Y.
	// Color must arrive as a parameter: the old version leaned on the tessellator's "current color" (setColorRGBA_F(..., alpha * 0.25F) before drawing),
	// which the base never set itself; this version's per-vertex colors leave no "current color" to inherit, so without the parameter only field values flow,
	// and those alpha multipliers silently stop working (BlackPowderSmoke is a quarter opacity; using fields would make it fully opaque, 4× too strong).
	public void renderParticleRotated(BufferBuilder buffer, float interp, float sX, float sY, float sZ, float dX, float dZ, double scale) {
		renderParticleRotated(buffer, interp, sX, sY, sZ, dX, dZ, scale,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
	}

	public void renderParticleRotated(BufferBuilder buffer, float interp, float sX, float sY, float sZ, float dX, float dZ, double scale,
			float red, float green, float blue, float alpha) {

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) interp - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) interp - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp - interpPosZ);
		float rotation = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * interp;

		double x1 = 0 - sX * scale - dX * scale;
		double y1 = 0 - sY * scale;
		double z1 = 0 - sZ * scale - dZ * scale;
		double x2 = 0 - sX * scale + dX * scale;
		double y2 = 0 + sY * scale;
		double z2 = 0 - sZ * scale + dZ * scale;
		double x3 = 0 + sX * scale + dX * scale;
		double y3 = 0 + sY * scale;
		double z3 = 0 + sZ * scale + dZ * scale;
		double x4 = 0 + sX * scale - dX * scale;
		double y4 = 0 - sY * scale;
		double z4 = 0 + sZ * scale - dZ * scale;

		// the face normal, defining the rotation axis
		double nX = ((y2 - y1) * (z3 - z1)) - ((z2 - z1) * (y3 - y1));
		double nY = ((z2 - z1) * (x3 - x1)) - ((x2 - x1) * (z3 - z1));
		double nZ = ((x2 - x1) * (y3 - y1)) - ((y2 - y1) * (x3 - x1));

		Vec3d vec = new Vec3d(nX, nY, nZ).normalize();
		nX = vec.x;
		nY = vec.y;
		nZ = vec.z;

		double cosTh = Math.cos(rotation * Math.PI / 180D);
		double sinTh = Math.sin(rotation * Math.PI / 180D);

		// Rodrigues' rotation formula, rotating by rotation degrees about unit(n)
		double x01 = x1 * cosTh + (nY * z1 - nZ * y1) * sinTh;
		double y01 = y1 * cosTh + (nZ * x1 - nX * z1) * sinTh;
		double z01 = z1 * cosTh + (nX * y1 - nY * x1) * sinTh;
		double x02 = x2 * cosTh + (nY * z2 - nZ * y2) * sinTh;
		double y02 = y2 * cosTh + (nZ * x2 - nX * z2) * sinTh;
		double z02 = z2 * cosTh + (nX * y2 - nY * x2) * sinTh;
		double x03 = x3 * cosTh + (nY * z3 - nZ * y3) * sinTh;
		double y03 = y3 * cosTh + (nZ * x3 - nX * z3) * sinTh;
		double z03 = z3 * cosTh + (nX * y3 - nY * x3) * sinTh;
		double x04 = x4 * cosTh + (nY * z4 - nZ * y4) * sinTh;
		double y04 = y4 * cosTh + (nZ * x4 - nX * z4) * sinTh;
		double z04 = z4 * cosTh + (nX * y4 - nY * x4) * sinTh;

		int light = this.getBrightnessForRender(interp);
		int lu = light & 65535;
		int lv = light >> 16;

		emit(buffer, pX + x01, pY + y01, pZ + z01, this.particleTexture.getMaxU(), this.particleTexture.getMaxV(), lu, lv, red, green, blue, alpha);
		emit(buffer, pX + x02, pY + y02, pZ + z02, this.particleTexture.getMaxU(), this.particleTexture.getMinV(), lu, lv, red, green, blue, alpha);
		emit(buffer, pX + x03, pY + y03, pZ + z03, this.particleTexture.getMinU(), this.particleTexture.getMinV(), lu, lv, red, green, blue, alpha);
		emit(buffer, pX + x04, pY + y04, pZ + z04, this.particleTexture.getMinU(), this.particleTexture.getMaxV(), lu, lv, red, green, blue, alpha);
	}

	private void emit(BufferBuilder buffer, double x, double y, double z, double u, double v, int lu, int lv,
			float red, float green, float blue, float alpha) {
		buffer.pos(x, y, z).tex(u, v)
			.color(red, green, blue, alpha)
			.lightmap(lu, lv).endVertex();
	}
}
