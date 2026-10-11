package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Cooling tower steam: an upward column of smoke, thickness/lift/lifetime set through setters.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch — one quad.
 * Lifetime doesn't come from the constructor: the old version set it via the Creator calling setLife (the constructor set nothing,
 * so unset ones inherit the parent's "random few ticks" default) — none of these setters change, and the Creator keeps writing as before.
 * That constant wind (+0.02 / -0.01, weighing in with age) is hard-coded from the old version; not a typo — the tower's smoke drifts sideways.
 */
@SideOnly(Side.CLIENT)
public class ParticleCoolingTower extends Particle {

	private float baseScale = 1.0F;
	private float maxScale = 1.0F;
	private float lift = 0.3F;
	private float strafe = 0.075F;
	private boolean windDir = true;
	private float alphaMod = 0.25F;

	public ParticleCoolingTower(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.particleRed = this.particleGreen = this.particleBlue = 0.9F + world.rand.nextFloat() * 0.05F;
		// the old noClip = true: the column shouldn't be blocked by blocks
		this.canCollide = false;
	}

	public void setBaseScale(float f) { this.baseScale = f; }
	public void setMaxScale(float f) { this.maxScale = f; }
	public void setLift(float f) { this.lift = f; }
	public void setLife(int i) { this.particleMaxAge = i; }
	public void setStrafe(float f) { this.strafe = f; }
	public void noWind() { this.windDir = false; }
	public void alphaMod(float mod) { this.alphaMod = mod; }

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		float ageScale = (float) this.particleAge / (float) this.particleMaxAge;

		this.particleAlpha = this.alphaMod - ageScale * this.alphaMod;
		this.particleScale = this.baseScale + (float) Math.pow(this.maxScale * ageScale - this.baseScale, 2);

		this.particleAge++;

		if(this.lift > 0 && this.motionY < this.lift) this.motionY += 0.01F;
		if(this.lift < 0 && this.motionY > this.lift) this.motionY -= 0.01F;

		this.motionX += this.rand.nextGaussian() * this.strafe * ageScale;
		this.motionZ += this.rand.nextGaussian() * this.strafe * ageScale;

		if(this.windDir) {
			this.motionX += 0.02 * ageScale;
			this.motionZ -= 0.01 * ageScale;
		}

		if(this.particleAge >= this.particleMaxAge) this.setExpired();

		this.move(this.motionX, this.motionY, this.motionZ);

		this.motionX *= 0.925;
		this.motionY *= 0.925;
		this.motionZ *= 0.925;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		int light = this.getBrightnessForRender(partial);

		ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
				rotX, rotZ, rotYZ, rotXY, rotXZ, this.particleScale,
				0F, 0F, 1F, 1F,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha,
				light & 65535, light >> 16);
	}
}
