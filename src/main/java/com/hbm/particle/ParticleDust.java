package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The ground poof: a small brown dust puff.
 *
 * The old four-corner hand-copied vanilla billboard goes to ParticleSprites appending into the shared buffer (the atlas layer is layer 1).
 * Blending/depth untouched, so ParticleManager's defaults apply: depth writes, SRC_ALPHA blending — as the old version.
 */
@SideOnly(Side.CLIENT)
public class ParticleDust extends Particle {

	public ParticleDust(World world, double x, double y, double z, double mX, double mY, double mZ, float scale) {
		super(world, x, y, z, mX, mY, mZ);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.particleRed = 0.4F;
		this.particleGreen = 0.2F;
		this.particleBlue = 0.1F;
		this.particleScale = scale;
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.particleAge = 1;
		this.particleMaxAge = 50 + world.rand.nextInt(50);
		this.particleAlpha = 0.5F;
		// the old noClip = true: this version has no noClip, and this is the physical counterpart
		this.canCollide = false;
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

		// The old block set no brightness at all, effectively reusing whatever the previous particle left in the tessellator (unreliable).
		// Written per-vertex with own lighting per this version's vanilla style: ground dust should follow ambient light, not glow in a cave.
		int light = this.getBrightnessForRender(partial);

		ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
				rotX, rotZ, rotYZ, rotXY, rotXZ, this.particleScale,
				0F, 0F, 1F, 1F,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha,
				light & 65535, light >> 16);
	}
}
