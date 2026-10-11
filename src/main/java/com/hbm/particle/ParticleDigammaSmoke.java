package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Digamma smoke: a wide sheet of dark-red smoke, drifting through walls and fading out.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch — one quad does it.
 * The brightness door returned 240 in the old version (split as block light 240, sky light 0), but its hand-rolled billboard never called
 * setBrightness, so nobody read it back then; only after per-vertex lightmap writes here does this override take effect.
 * Don't "fix" it to full — it would wash the smoke white even in daylight, and the look changes.
 */
@SideOnly(Side.CLIENT)
public class ParticleDigammaSmoke extends Particle {

	private int age;
	public int maxAge;

	public ParticleDigammaSmoke(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.maxAge = 100 + this.rand.nextInt(40);
		// the old noClip = true
		this.canCollide = false;

		this.particleScale = 5;

		this.particleRed = 0.5F + this.rand.nextFloat() * 0.2F;
		this.particleGreen = 0F;
		this.particleBlue = 0F;
	}

	// This version's Particle motion* is protected; producers (the registered branches) need to set velocity per mode, so the door lives here
	public ParticleDigammaSmoke setMotion(double mx, double my, double mz) {
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		return this;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAlpha = 1 - ((float) this.age / (float) this.maxAge);

		if(++this.age >= this.maxAge) this.setExpired();

		this.motionX *= 0.99D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.99D;

		this.move(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public int getBrightnessForRender(float partial) {
		return 240;
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
