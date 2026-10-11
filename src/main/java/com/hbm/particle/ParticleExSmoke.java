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
 * Exhaust smoke: six jittering gray sheets stacked, fading with age.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch, drawn like ParticleContrail's family.
 *
 * The jitter sequence must be frame-stable (the old one newed Random(getEntityId()) per frame), but Particle has no id here,
 * so the seed is stored in a field and rendering reuses the same Random via setSeed: behaviorally equivalent, no per-frame allocation.
 * The old renderParticle rewrote particleRed/Green/Blue in place (a render function mutating state is a side effect,
 * anything reading those fields that frame gets silently changed); only the gray value goes into the per-vertex color here.
 */
@SideOnly(Side.CLIENT)
public class ParticleExSmoke extends Particle {

	private static final int PUFFS = 6;

	private final long seed;
	private final Random urandom = new Random();
	private int age;
	public int maxAge;

	public ParticleExSmoke(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.maxAge = 100 + this.rand.nextInt(40);
		this.seed = this.rand.nextLong();
	}

	// This version's Particle motion* is protected; producers (the registered branches) need to set velocity per mode, so the door lives here
	public ParticleExSmoke setMotion(double mx, double my, double mz) {
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

		// the fade steps by whole ticks, no partial-tick jitter
		this.particleAlpha = 1 - ((float) this.age / (float) this.maxAge);

		if(++this.age >= this.maxAge) this.setExpired();

		this.motionX *= 0.76D;
		this.motionY *= 0.76D;
		this.motionZ *= 0.76D;

		this.move(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		urandom.setSeed(this.seed);

		float baseX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float baseY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float baseZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		int light = this.getBrightnessForRender(partial);

		for(int i = 0; i < PUFFS; i++) {

			float gray = this.urandom.nextFloat() * 0.25F + 0.25F;
			float scale = this.urandom.nextFloat() + 0.5F;

			float pX = (float) (baseX + (this.urandom.nextGaussian() - 1D) * 0.75F);
			float pY = (float) (baseY + (this.urandom.nextGaussian() - 1D) * 0.75F);
			float pZ = (float) (baseZ + (this.urandom.nextGaussian() - 1D) * 0.75F);

			ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
					rotX, rotZ, rotYZ, rotXY, rotXZ, scale,
					0F, 0F, 1F, 1F,
					gray, gray, gray, this.particleAlpha,
					light & 65535, light >> 16);
		}
	}
}
