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
 * The contrail: six jittering quads stacked, fading with age.
 *
 * Regular alpha blending (SRC_ALPHA / ONE_MINUS_SRC_ALPHA), so per plan C it quads into the shared buffer,
 * with the texture from the block atlas's `hbm:particle/contrail` (registered by A2's HbmTextureStitcher);
 * no bindTexture, no GL state, no begin/draw of its own — the six quads are just appended to the same vertex batch.
 *
 * The jitter sequence must be identical every frame (the old one newed Random(entityId) per frame), so the seed is stored and rendering reuses the same
 * Random via setSeed: behaviorally equivalent, without the per-frame allocation.
 *
 * The camera baseline is Particle.interpPos: everything in the shared buffer takes it as reference, so following the camera lines up
 * (the old version hand-computed the player's interpolated position and misplaces in third person).
 */
@SideOnly(Side.CLIENT)
public class ParticleContrail extends Particle {

	// six sheets per draw, for that fluffy trail look
	private static final int PUFFS = 6;

	private final long seed;
	private final Random urandom = new Random();
	private int age;
	private int maxAge;

	public ParticleContrail(TextureManager texManager, World world, double x, double y, double z) {
		this(texManager, world, x, y, z, 0F, 0F, 0F, 1F);
	}

	public ParticleContrail(TextureManager texManager, World world, double x, double y, double z, float red, float green, float blue, float scale) {
		super(world, x, y, z);
		this.maxAge = 100 + this.rand.nextInt(40);
		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;
		this.particleScale = scale;
		this.seed = this.rand.nextLong();
		this.setParticleTexture(ParticleSprites.sprite("contrail"));
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		// the fade steps by whole ticks, no partial-tick jitter
		this.particleAlpha = 1F - ((float) this.age / (float) this.maxAge);

		if(++this.age >= this.maxAge) this.setExpired();
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

		// adding the same mod to all three channels brightens overall without shifting hue
		float scale = (this.particleAlpha + 0.5F) * this.particleScale;

		for(int i = 0; i < PUFFS; i++) {

			float mod = this.urandom.nextFloat() * 0.2F + 0.2F;

			float jx = pX + (float) (this.urandom.nextGaussian() * 0.5 * this.particleScale);
			float jy = pY + (float) (this.urandom.nextGaussian() * 0.5 * this.particleScale);
			float jz = pZ + (float) (this.urandom.nextGaussian() * 0.5 * this.particleScale);

			ParticleSprites.emitQuad(buffer, this.particleTexture, jx, jy, jz,
					rotX, rotZ, rotYZ, rotXY, rotXZ, scale,
					0F, 0F, 1F, 1F,
					this.particleRed + mod, this.particleGreen + mod, this.particleBlue + mod, this.particleAlpha);
		}
	}
}
