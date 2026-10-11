package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The debug particle: type 0 wears the power texture, type 1 the fluid texture tinted to the medium's color.
 *
 * It touches no blending/depth state — pure alpha — so per plan C it lands in the shared buffer: textures become the atlas's
 * `hbm:particle/debug_power|debug_fluid` (registered by A2's HbmTextureStitcher), and this only appends quads.
 *
 * Depth matches exactly: the old version never disabled depth writes, so shouldDisableDepth() isn't overridden and the particle lands on
 * ParticleManager's depth-writing page (j==1 → depthMask(true)). The earlier line "atlas texture + depth writes can't be expressed"
 * was my own note reversed — the layer only picks the texture; shouldDisableDepth() picks the depth page separately, see ParticleSprites.
 *
 * Keeping the hand-written renderParticle instead of the parent's: the old size was a fixed 0.05, never shrinking with age,
 * while the parent's shrinks by age — a different look, not skippable.
 */
@SideOnly(Side.CLIENT)
public class ParticleDebug extends Particle {

	// the small icon size, as the old version
	private static final float SCALE = 0.05F;

	public ParticleDebug(TextureManager texman, World world, double x, double y, double z, double motionX, double motionY, double motionZ) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.particleMaxAge = 10;
		this.setParticleTexture(ParticleSprites.sprite("debug_power"));
	}

	public ParticleDebug(TextureManager texman, World world, double x, double y, double z, double motionX, double motionY, double motionZ, int color) {
		super(world, x, y, z, motionX, motionY, motionZ);
		this.particleMaxAge = 10;
		this.particleRed = ((color & 0xff0000) >> 16) / 255F;
		this.particleGreen = ((color & 0x00ff00) >> 8) / 255F;
		this.particleBlue = (color & 0x0000ff) / 255F;
		this.setParticleTexture(ParticleSprites.sprite("debug_fluid"));
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) this.setExpired();
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

		ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
				rotX, rotZ, rotYZ, rotXY, rotXZ, SCALE,
				0F, 0F, 1F, 1F,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);
	}
}
