package com.hbm.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Rocket exhaust MUD tier: red and green maxed, blue left to jitter, hence whitish; brightness only 20 in the old version — the darkest of the five tiers. */
@SideOnly(Side.CLIENT)
public class ParticleRocketMUD extends ParticleRocketFlame {

	public ParticleRocketMUD(TextureManager texman, World world, double x, double y, double z) {
		super(texman, world, x, y, z);
	}

	// The old five variants copied the parent's layer loop wholesale and changed only the channel math and brightness; the port keeps that shape,
	// making future diffs against 1.7.10 easy. Those setNormal lines were dead (this version's vertex format has no normal slot) and are gone.
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		urandom.setSeed(this.seed);

		double baseX = this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX;
		double baseY = this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY;
		double baseZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ;

		float spread = (float) Math.pow(((float) this.particleAge / (float) this.particleMaxAge) * 4F, 1.5) + 1F;
		spread *= this.particleScale;

		for(int i = 0; i < 10; i++) {

			float add = this.urandom.nextFloat() * 0.3F;

			// The old code had one line per channel computing 1*light+add, immediately overwritten by the next — self-clobbering dead lines;
			// with the dead lines gone, light is never used in this variant, so it isn't computed anymore
			this.particleRed = 1 + add;
			this.particleGreen = 1 + add;
			this.particleBlue = 0 + add;

			this.particleAlpha = (float) Math.pow(1 - Math.min(((float) this.particleAge) / (float) this.particleMaxAge, 1), 0.5);

			float scale = (this.urandom.nextFloat() * 0.5F + 0.1F + ((float) this.particleAge / (float) this.particleMaxAge) * 2F) * this.particleScale;
			float pX = (float) (baseX + (this.urandom.nextGaussian() - 1D) * 0.2F * spread);
			float pY = (float) (baseY + (this.urandom.nextGaussian() - 1D) * 0.5F * spread);
			float pZ = (float) (baseZ + (this.urandom.nextGaussian() - 1D) * 0.2F * spread);

			ParticleSprites.emitQuad(buffer, this.particleTexture, pX, pY, pZ,
					rotX, rotZ, rotYZ, rotXY, rotXZ, scale,
					0F, 0F, 1F, 1F,
					this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.75F,
					20, 0);
		}
	}
}
