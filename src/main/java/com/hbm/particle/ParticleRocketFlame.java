package com.hbm.particle;

import java.util.Random;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The rocket exhaust: ten stacked tongues of fire, rising and spreading, dimming with age.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch.
 * The old version called `setBrightness(240)` per sheet — split as block light 240, sky light 0 — lit by its own fire and warm,
 * not the both-components-maxed white. Writing (240, 0) explicitly here keeps the same reading as the five variants (20/80/240).
 * The 0.75 alpha factor must also be passed explicitly; don't count on the field.
 *
 * High-altitude (low pressure) behavior copied: below 0.08 pressure it jitters randomly, below 0.05 lifetime is quartered,
 * below 0.2 halved — a side effect of the old setAtmosphericPressure, not a setter bug.
 *
 * One structural merge: the old version carried its own age/maxAge fields plus a setMaxAge returning itself, but this version's Particle already has
 * particleAge/particleMaxAge and a public setMaxAge(int); that fluent setter collides with the parent's same-named method on return type and won't compile.
 * Measured: every setMaxAge call in the tree is a standalone statement (return value unused), so the parent's is used directly — behavior identical.
 */
@SideOnly(Side.CLIENT)
public class ParticleRocketFlame extends Particle {

	private static final int PUFFS = 10;

	// Subclasses (the five Rocket variants) each copy the layer loop, sharing this Random as their seed source
	protected final long seed;
	protected final Random urandom = new Random();

	private float customRed;
	private float customGreen;
	private float customBlue;

	protected double pressure = 1;

	public ParticleRocketFlame(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.particleMaxAge = 300 + this.rand.nextInt(50);
		this.particleScale = 1F;
		// The old version newed Random(getEntityId()) per frame; Particle has no id here: the seed is stored and setSeed reuses it at render
		this.seed = this.rand.nextLong();
	}

	public ParticleRocketFlame setScale(float scale) {
		this.particleScale = scale;
		return this;
	}

	// This version's Particle motion* and canCollide are both protected; producers (the assembly side of explosions) can't reach them, so the doors live here
	public ParticleRocketFlame setMotion(double mx, double my, double mz) {
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		return this;
	}

	public ParticleRocketFlame setNoClip() {
		this.canCollide = false;
		return this;
	}

	public ParticleRocketFlame setCustomColor(float red, float green, float blue) {
		this.customRed = red;
		this.customGreen = green;
		this.customBlue = blue;
		return this;
	}

	public ParticleRocketFlame setAtmosphericPressure(double pressure) {

		this.pressure = pressure;

		if(pressure < 0.08) {
			double factor = (0.08 - pressure) * 20;
			this.motionX += (this.rand.nextDouble() - 0.5) * factor;
			this.motionY += (this.rand.nextDouble() - 0.5) * factor;
			this.motionZ += (this.rand.nextDouble() - 0.5) * factor;
		}

		if(pressure < 0.05) {
			this.particleMaxAge /= 4;
		} else if(pressure < 0.2) {
			this.particleMaxAge /= 2;
		}

		return this;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAge++;

		if(this.particleAge == this.particleMaxAge) this.setExpired();

		this.motionX *= 0.91D;
		this.motionY *= 0.91D;
		this.motionZ *= 0.91D;

		this.move(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		// not drawn on the spawn tick (as before), so the exhaust picks up the thruster's rhythm a beat late
		if(this.particleAge == 0) return;

		urandom.setSeed(this.seed);

		double baseX = this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX;
		double baseY = this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY;
		double baseZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ;

		// the spread radius swells with age; exponent 1.5 makes it slow-then-fast
		float spread = (float) Math.pow(((float) this.particleAge / (float) this.particleMaxAge) * 4F, 1.5) + 1F;
		spread *= this.particleScale;

		for(int i = 0; i < PUFFS; i++) {

			float add = this.urandom.nextFloat() * 0.3F;
			float dark = 1 - Math.min(((float) this.particleAge) / (float) (this.particleMaxAge * 0.25F), 1);

			this.particleRed = (this.customRed != 0) ? (this.customRed * dark + add) : (1 * dark + add);
			this.particleGreen = (this.customGreen != 0) ? (this.customGreen * dark + add) : (0.6F * dark + add);
			this.particleBlue = (this.customBlue != 0) ? (this.customBlue * dark + add) : (0 + add);

			this.particleAlpha = (float) Math.pow(1 - Math.min(((float) this.particleAge) / (float) this.particleMaxAge, 1), 0.5);

			float scale = (this.urandom.nextFloat() * 0.5F + 0.1F + ((float) this.particleAge / (float) this.particleMaxAge) * 2F) * this.particleScale;
			float pX = (float) (baseX + (this.urandom.nextGaussian() - 1D) * 0.2F * spread);
			float pY = (float) (baseY + (this.urandom.nextGaussian() - 1D) * 0.5F * spread);
			float pZ = (float) (baseZ + (this.urandom.nextGaussian() - 1D) * 0.2F * spread);

			// the old setBrightness(240) = block light 240, sky light 0; the variants each give 20/80/240, hence explicit parameters here
			ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
					rotX, rotZ, rotYZ, rotXY, rotXZ, scale,
					0F, 0F, 1F, 1F,
					this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F,
					240, 0);
		}
	}
}
