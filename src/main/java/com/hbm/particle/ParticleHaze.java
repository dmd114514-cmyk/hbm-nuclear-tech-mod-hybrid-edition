package com.hbm.particle;

import java.util.Random;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The poison fog / dust cloud: a fixed-seed Gaussian jitter spread across 25 translucent sheets, drifting while sparking off the ground.
 *
 * Regular alpha blending (old 770/771), landing in the shared buffer per plan C, texture from the atlas's `hbm:particle/haze`.
 *
 * ⚠️ The old version accumulated per-sheet displacement with `glTranslatef` inside the loop. The shared buffer's vertices are transformed by the matrix only at the final
 * unified draw, so "changing the matrix mid-submission" does nothing here — the accumulation is computed straight into the coordinates instead.
 *
 * The old constructors' red/green/blue components were wholly overridden by `glColor4f(1,1,1,alpha)` and never mattered (the default constructor
 * even set them to 0, which would have been black fog had they shown). White stays for the same look, with alpha moved into the per-vertex color.
 */
@SideOnly(Side.CLIENT)
public class ParticleHaze extends Particle {

	private static final int PUFFS = 25;

	// fixed seed, so every frame's jitter layout is identical and the fog doesn't dissolve into noise
	private static final int JITTER_SEED = 50;

	private final int maxAge;

	// the default tier is the long-lived fog (600+100t), the tinted tier short-lived (100+40t); don't merge them or lifetimes cross
	public ParticleHaze(TextureManager texManager, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.maxAge = 600 + this.rand.nextInt(100);
		this.particleScale = 10F;
		this.setParticleTexture(ParticleSprites.sprite("haze"));
	}

	public ParticleHaze(TextureManager texManager, World world, double x, double y, double z, float red, float green, float blue, float scale) {
		super(world, x, y, z);
		this.maxAge = 100 + this.rand.nextInt(40);
		this.particleScale = scale;
		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;
		this.setParticleTexture(ParticleSprites.sprite("haze"));
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.maxAge) this.setExpired();

		this.motionX *= 0.96D;
		this.motionY *= 0.96D;
		this.motionZ *= 0.96D;

		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}

		// the fog strikes sparks off the ground
		int x = (int) Math.floor(this.posX) + this.rand.nextInt(15) - 7;
		int z = (int) Math.floor(this.posZ) + this.rand.nextInt(15) - 7;
		int y = this.world.getHeight(x, z);
		this.world.spawnParticle(EnumParticleTypes.LAVA, x + this.rand.nextDouble(), y + 0.1D, z + this.rand.nextDouble(), 0D, 0D, 0D);
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

		float alpha = (float) Math.sin(this.particleAge * Math.PI / 400F) * 0.25F * 0.1F;

		Random rand = new Random(JITTER_SEED);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		// the old version's whole-drift came from in-loop accumulated glTranslatef; accumulated locally here
		double accX = 0;
		double accY = 0;
		double accZ = 0;

		for(int i = 0; i < PUFFS; i++) {

			accX += rand.nextGaussian() * 2.5D;
			accY += rand.nextGaussian() * 0.15D;
			accZ += rand.nextGaussian() * 2.5D;

			double size = (rand.nextDouble() * 0.25D + 0.75D) * this.particleScale;

			float jx = (float) (pX + accX + rand.nextGaussian() * 0.5D);
			float jy = (float) (pY + accY + rand.nextGaussian() * 0.5D);
			float jz = (float) (pZ + accZ + rand.nextGaussian() * 0.5D);

			ParticleSprites.emitQuad(buffer, this.particleTexture, jx, jy, jz,
					rotX, rotZ, rotYZ, rotXY, rotXZ, (float) size,
					0F, 0F, 1F, 1F, 1F, 1F, 1F, alpha);
		}
	}
}
