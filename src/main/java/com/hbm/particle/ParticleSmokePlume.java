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
 * The smoke plume: six gray jittering sheets, scaling up and fading with age, bouncing its rise off the ground.
 *
 * Pure alpha blending (old 770/771), landing in the shared buffer per plan C, texture from the atlas's `hbm:particle/contrail`.
 *
 * Two deliberate changes:
 * · the old renderParticle rewrote particleRed/Green/Blue in place — a render function shouldn't mutate particle state
 *   (anything reading those fields in the same frame gets silently changed). Here the gray value goes only into the per-vertex color; fields stay untouched.
 * · the old used Entity.isCollidedVertically, absent on 1.12.2's Particle; onGround substitutes by semantics
 *   (the plume is a ground-hugger; equivalent in this scenario).
 */
@SideOnly(Side.CLIENT)
public class ParticleSmokePlume extends Particle {

	// six sheets per draw, for the plume's volume
	private static final int PUFFS = 6;

	private final long seed;
	private final Random urandom = new Random();
	private int age;
	private int maxAge;

	public ParticleSmokePlume(TextureManager texManager, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.maxAge = 80 + this.rand.nextInt(20);
		this.particleScale = 0.25F;
		this.seed = this.rand.nextLong();
		this.setParticleTexture(ParticleSprites.sprite("contrail"));
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAlpha = 1F - ((float) this.age / (float) this.maxAge);

		float prevScale = this.particleScale;
		this.particleScale = 0.25F + ((float) this.age / (float) this.maxAge) * 2F;

		if(++this.age >= this.maxAge) this.setExpired();

		// the height the smoke swells is compensated straight into displacement, or it won't read as rising
		double bak = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);

		this.move(this.motionX, this.motionY + (this.particleScale - prevScale), this.motionZ);

		if(this.onGround) this.motionY = bak;

		this.motionX *= 0.925D;
		this.motionY *= 0.925D;
		this.motionZ *= 0.925D;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public boolean shouldDisableDepth() {
		// the old version drew this with depth writes off; depth doesn't read the layer number now — this door picks the page
		return true;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		urandom.setSeed(this.seed);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		float scale = this.particleScale;

		for(int i = 0; i < PUFFS; i++) {

			// each sheet carries its own gray value, or the whole puff is one flat block of color
			float grey = this.urandom.nextFloat() * 0.75F + 0.1F;

			float jx = pX + (float) (this.urandom.nextGaussian() * 0.5 * scale);
			float jy = pY + (float) (this.urandom.nextGaussian() * 0.5 * scale);
			float jz = pZ + (float) (this.urandom.nextGaussian() * 0.5 * scale);

			ParticleSprites.emitQuad(buffer, this.particleTexture, jx, jy, jz,
					rotX, rotZ, rotYZ, rotXY, rotXZ, scale,
					0F, 0F, 1F, 1F, grey, grey, grey, this.particleAlpha);
		}
	}
}
