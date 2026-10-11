package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A dead leaf: drifting down lazily, stopping where it lands.
 *
 * Layer 1 (block atlas), texture `blocks/particle/dead_leaf`. Same writing as the splash: the hand-copied billboard became
 * ParticleSprites vertex appends, the mirrored facing likewise "one roll at construction" (the old one borrowed entityId parity; Particle has no id).
 * The only real difference from ParticleSplash is terminal velocity: leaves fall at -0.025, splashes at -0.5.
 */
@SideOnly(Side.CLIENT)
public class ParticleDeadLeaf extends Particle {

	private final boolean flipU;
	private final boolean flipV;

	public ParticleDeadLeaf(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("dead_leaf"));
		this.particleRed = this.particleGreen = this.particleBlue = 1F - world.rand.nextFloat() * 0.2F;
		this.particleScale = 0.1F;
		this.particleMaxAge = 200 + world.rand.nextInt(50);
		this.particleGravity = 0.2F;
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
