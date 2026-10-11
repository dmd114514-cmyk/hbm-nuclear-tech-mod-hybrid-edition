package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The splash: a small bouncing droplet sheet, gone on landing.
 *
 * Layer 1 (block atlas), texture `blocks/particle/particle_splash`.
 * The old four-corner hand-copied vanilla billboard became ParticleSprites vertex appends; blending and depth untouched, as vanilla.
 *
 * One necessary deviation: the old version gave each droplet a stable mirrored facing from entityId parity (so on-screen splashes don't all match),
 * but this version's Particle isn't an Entity and measurably has no id, so one roll at construction is stored —
 * what matters is "fixed per particle, never per frame", which matches the old effect.
 * Brightness follows this version's vanilla style: per-vertex own lighting. The old block set neither brightness nor normals, effectively reusing
 * whatever the previous particle left in the tessellator — unreliable to begin with.
 */
@SideOnly(Side.CLIENT)
public class ParticleSplash extends Particle {

	private final boolean flipU;
	private final boolean flipV;

	public ParticleSplash(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_splash"));
		this.particleRed = this.particleGreen = this.particleBlue = 1F - world.rand.nextFloat() * 0.2F;
		this.particleAlpha = 0.5F;
		this.particleScale = 0.4F;
		this.particleMaxAge = 200 + world.rand.nextInt(50);
		this.particleGravity = 0.4F;
		this.flipU = this.rand.nextBoolean();
		this.flipV = this.rand.nextBoolean();
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!this.onGround) {
			this.motionX += this.rand.nextGaussian() * 0.002D;
			this.motionZ += this.rand.nextGaussian() * 0.002D;

			// terminal velocity: droplets don't accelerate forever
			if(this.motionY < -0.5D) this.motionY = -0.5D;
		} else {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		float u0 = this.flipU ? 1F : 0F;
		float u1 = this.flipU ? 0F : 1F;
		float v0 = this.flipV ? 1F : 0F;
		float v1 = this.flipV ? 0F : 1F;

		int light = this.getBrightnessForRender(partial);

		ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
				rotX, rotZ, rotYZ, rotXY, rotXZ, this.particleScale,
				u0, v0, u1, v1,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha,
				light & 65535, light >> 16);
	}
}
