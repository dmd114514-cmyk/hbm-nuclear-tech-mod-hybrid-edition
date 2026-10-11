package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The falling note: a decorative music note that floats and lands, drawn like the splash/dead-leaf family (layer 1, pure alpha, no depth touch) */
@SideOnly(Side.CLIENT)
public class ParticleDuoDec2 extends Particle {

	public ParticleDuoDec2(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particlenote1"));
		this.particleRed = this.particleGreen = this.particleBlue = 1F - world.rand.nextFloat() * 0.2F;
		this.particleScale = 0.3F;
		this.particleMaxAge = 50 + world.rand.nextInt(50);
		this.particleGravity = 0.2F;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.onGround) return;

		this.motionX += this.rand.nextGaussian() * 0.002D;
		this.motionZ += this.rand.nextGaussian() * 0.002D;

		if(this.motionY < -0.025D) this.motionY = -0.025D;
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
