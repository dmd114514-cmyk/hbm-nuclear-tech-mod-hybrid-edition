package com.hbm.particle;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The giblet: flies, lands, stops, shedding bits along the way.
 *
 * The old version was layer-3 self-drawn, binding its own meat / slime / metal textures and per-frame
 * rewriting the global lightmap coordinates via OpenGlHelper.setLightmapTextureCoords. Those three textures are already in the block atlas,
 * so per plan C it quads into layer 1 (verified: only i==1 binds the block atlas), with lighting rewritten as per-vertex lightmap —
 * same effect, without polluting the global state of every particle in the batch. No depth concession: the old version never disabled depth writes,
 * and shouldDisableDepth() stays unoverridden, so the particle lands on ParticleManager's depth-writing page naturally.
 *
 * Two honest deviations:
 *   · the old version disabled blending and relied on alpha-tested hard edges; the shared buffer uses vanilla's SRC_ALPHA blending,
 *     but all three textures are hard-edged, indistinguishable to the eye;
 *   · the old onUpdate pushed rotationPitch/rotationYaw as a tumble, yet its renderParticle never read either value —
 *     that tumble was dead code to begin with, and this version's Particle neither extends Entity nor has the fields, so the whole block goes.
 * The shed bit also stops reflection-jamming particleMaxAge — setMaxAge is public now.
 */
@SideOnly(Side.CLIENT)
public class ParticleGiblet extends Particle {

	private final int gibType;

	public ParticleGiblet(TextureManager texman, World world, double x, double y, double z, double mX, double mY, double mZ, int gibType) {
		super(world, x, y, z);
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.particleMaxAge = 140 + this.rand.nextInt(20);
		this.particleGravity = 2F;
		this.gibType = gibType;

		if(gibType == 2) this.particleGravity *= 2F;

		this.setParticleTexture(ParticleSprites.sprite(gibType == 2 ? "metal" : gibType == 1 ? "slime" : "meat"));
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.onGround) return;
		// metal shards are hard; no shedding
		if(this.gibType == 2) return;

		Block crumb = this.gibType == 1 ? Blocks.MELON_BLOCK : Blocks.REDSTONE_BLOCK;
		// This version's BLOCK_DUST factory treats the first integer after the coordinates as a **block state id** (Block.getStateById),
		// so Block.getStateId(...) must be passed; an item id resolves into some other state and the debris silently draws wrong or not at all
		Particle fx = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(),
				this.posX, this.posY, this.posZ, 0D, 0D, 0D, Block.getStateId(crumb.getDefaultState()));
		if(fx != null) fx.setMaxAge(20 + this.rand.nextInt(20));
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		int light = this.getBrightnessForRender(partial);

		float pX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partial - interpPosX);
		float pY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partial - interpPosY);
		float pZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partial - interpPosZ);

		ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
				rotX, rotZ, rotYZ, rotXY, rotXZ, this.particleScale * 0.1F,
				0F, 0F, 1F, 1F,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha,
				light & 65535, light >> 16);
	}
}
