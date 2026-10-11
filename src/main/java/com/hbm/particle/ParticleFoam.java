package com.hbm.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The foam: a rising soap bubble with an ever-fainter trail, in surge-crest-fall phases.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch.
 * Each point draws 8/6/4 bubbles (by phase), each bubble's position jittered by a seeded random — the seed must be frame-stable,
 * or the whole trail dissolves into noise. The old seed was `getEntityId() + quantized position`; Particle has no id here,
 * so it's one construction-time roll stored, plus the same position term.
 *
 * Two honest deletions:
 *   · in the old "vanish on cobweb" check, isInWeb was never assigned — this class's onUpdate is hand-written and
 *     never calls super, while that flag was maintained by the parent's onUpdate, so the criterion was always false; deleting it changes nothing;
 *   · TrailPoint's constructor took an alpha parameter it neither stored nor read; gone too.
 */
@SideOnly(Side.CLIENT)
public class ParticleFoam extends Particle {

	private int age;
	public int maxAge;
	private float baseScale = 1.0F;
	private float maxScale = 1.5F;

	// the trail: one point added atop each tick, trimmed from the tail past the length
	private final List<TrailPoint> trail = new ArrayList<TrailPoint>();
	private int trailLength = 15;
	private float buoyancy = 0.05F;
	private float jitter = 0.15F;
	private float drag = 0.96F;
	private int explosionPhase; // 0=surge 1=crest 2=fall

	private final long seed;
	private final Random urandom = new Random();

	private static class TrailPoint {
		double x, y, z;

		public TrailPoint(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	public ParticleFoam(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));

		this.maxAge = 60 + this.rand.nextInt(60);
		this.particleGravity = 0.005F + this.rand.nextFloat() * 0.015F;

		float initialVelocity = 2.0F + this.rand.nextFloat() * 3.0F;
		this.motionY = initialVelocity;

		double angle = this.rand.nextDouble() * Math.PI * 2;
		double strength = this.rand.nextDouble() * 0.5;
		this.motionX = Math.cos(angle) * strength;
		this.motionZ = Math.sin(angle) * strength;

		this.explosionPhase = 0;

		this.particleScale = 0.3F + this.rand.nextFloat() * 0.7F;
		this.seed = this.rand.nextLong();
	}

	public void setBaseScale(float f) { this.baseScale = f; }
	public void setMaxScale(float f) { this.maxScale = f; }
	public void setTrailLength(int length) { this.trailLength = length; }
	public void setBuoyancy(float buoyancy) { this.buoyancy = buoyancy; }

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.trail.add(0, new TrailPoint(this.posX, this.posY, this.posZ));

		while(this.trail.size() > this.trailLength) {
			this.trail.remove(this.trail.size() - 1);
		}

		++this.age;

		if(this.age == this.maxAge) this.setExpired();

		float phaseRatio = (float) this.age / (float) this.maxAge;

		if(phaseRatio < 0.3F) {
			this.explosionPhase = 0;

			if(phaseRatio < 0.15F) {
				this.motionY += this.buoyancy * 6.0F;
			} else {
				this.motionY += this.buoyancy * (1.0F - (phaseRatio / 0.3F)) * 2.0F;
			}

			this.particleScale = this.baseScale + (this.maxScale - this.baseScale) * (phaseRatio / 0.3F);
		} else if(phaseRatio < 0.6F) {
			this.explosionPhase = 1;
			this.motionY *= 0.98F;

			this.particleScale = this.maxScale;
		} else {
			this.explosionPhase = 2;
			this.motionY -= this.particleGravity;

			this.particleScale = this.maxScale * (1.0F - ((phaseRatio - 0.6F) / 0.4F) * 0.7F);
		}

		this.particleAlpha = 0.8F * (1.0F - phaseRatio * phaseRatio);

		this.motionX += (this.rand.nextFloat() - 0.5F) * this.jitter;
		this.motionZ += (this.rand.nextFloat() - 0.5F) * this.jitter;

		// drag wears down the surge
		this.motionX *= this.drag;
		this.motionY *= this.drag;
		this.motionZ *= this.drag;

		this.move(this.motionX, this.motionY, this.motionZ);

		if(this.onGround) this.setExpired();
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		renderBubbles(buffer, partial, rotX, rotZ, rotYZ, rotXY, rotXZ, this.posX, this.posY, this.posZ, this.particleScale, this.particleAlpha);

		for(int i = 1; i < this.trail.size(); i++) {
			TrailPoint point = this.trail.get(i);
			float trailScale = this.particleScale * (1.0F - (float) i / this.trailLength);
			float trailAlpha = this.particleAlpha * (1.0F - (float) i / this.trailLength) * 0.7F;

			renderBubbles(buffer, partial, rotX, rotZ, rotYZ, rotXY, rotXZ, point.x, point.y, point.z, trailScale, trailAlpha);
		}
	}

	// the camera basis vectors must be threaded all the way down: bubbles are billboards, facing the view — hard-coded constants turn them into a ground-hugging plate
	private void renderBubbles(BufferBuilder buffer, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ,
			double x, double y, double z, float scale, float alpha) {

		urandom.setSeed(this.seed + (long) (x * 100) + (long) (y * 10) + (long) z);

		int bubbleCount = this.explosionPhase == 0 ? 8 : (this.explosionPhase == 1 ? 6 : 4);

		double baseX = x - interpPosX;
		double baseY = y - interpPosY;
		double baseZ = z - interpPosZ;

		int light = this.getBrightnessForRender(partial);

		for(int i = 0; i < bubbleCount; i++) {

			float whiteness = 0.9F + this.urandom.nextFloat() * 0.1F;
			float bubbleScale = scale * (this.urandom.nextFloat() * 0.5F + 0.75F);
			float offset = this.explosionPhase == 0 ? 0.4F : (this.explosionPhase == 1 ? 0.6F : 0.9F);

			float pX = (float) (baseX + this.urandom.nextGaussian() * offset);
			float pY = (float) (baseY + this.urandom.nextGaussian() * offset * 0.7F);
			float pZ = (float) (baseZ + this.urandom.nextGaussian() * offset);

			ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
					rotX, rotZ, rotYZ, rotXY, rotXZ, bubbleScale,
					0F, 0F, 1F, 1F,
					whiteness, whiteness, whiteness, alpha,
					light & 65535, light >> 16);
		}
	}
}
