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
 * The radiation fog: a greenish translucent fog, sine-fading with age, spreading slowly along the ground.
 *
 * Triple-checked before the move: pure alpha (770/771) + the old `depthMask(false)` + the `fog` texture inside the v1.6 atlas table
 * → layer 1 (verified: only i==1 binds the block atlas) + shouldDisableDepth() overridden onto the no-depth-write page; all three items line up.
 *
 * Two traps shared with ParticleHaze: the old version drifted its 25 sheets via in-loop **accumulated `glTranslatef`**,
 * while the shared buffer's vertices feel the matrix only at the final unified draw — that writing does nothing here, so the accumulation is computed into coordinates;
 * the jitter uses a fixed seed, keeping every frame's layout identical so the fog doesn't dissolve into noise.
 *
 * Color hard-set to (0.85, 0.9, 0.5) as before: the old glColor4f overrode particleRed/Green/Blue,
 * so the tint arguments both constructors took never actually worked (the default tier even zeroed those fields — black fog had they shown),
 * and the look is preserved unchanged here.
 */
@SideOnly(Side.CLIENT)
public class ParticleRadiationFog extends Particle {

	private static final int PUFFS = 25;

	// fixed seed, as the old version
	private static final int JITTER_SEED = 50;

	private static final float FOG_R = 0.85F;
	private static final float FOG_G = 0.9F;
	private static final float FOG_B = 0.5F;

	private int maxAge;

	public ParticleRadiationFog(TextureManager texManager, World world, double x, double y, double z) {
		this(texManager, world, x, y, z, 0F, 0F, 0F, 7.5F);
	}

	public ParticleRadiationFog(TextureManager texManager, World world, double x, double y, double z, float red, float green, float blue, float scale) {
		super(world, x, y, z);
		this.maxAge = 100 + this.rand.nextInt(40);
		this.particleScale = scale;
		this.setParticleTexture(ParticleSprites.sprite("fog"));
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		// the fade-in/out half of the sine must run its course; lifetimes under 400 are forced up to 400
		if(this.maxAge < 400) this.maxAge = 400;

		if(this.particleAge++ >= this.maxAge) this.setExpired();

		this.motionX *= 0.96D;
		this.motionY *= 0.96D;
		this.motionZ *= 0.96D;

		if(this.onGround) {
			this.motionX *= 0.7D;
			this.motionZ *= 0.7D;
		}
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

		float alpha = (float) Math.sin(this.particleAge * Math.PI / 400F) * 0.125F;

		Random rand = new Random(JITTER_SEED);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		// the old accumulated-glTranslatef effect, re-done as local accumulation
		double accX = 0;
		double accY = 0;
		double accZ = 0;

		for(int i = 0; i < PUFFS; i++) {

			accX += (rand.nextGaussian() - 1D) * 2.5D;
			accY += (rand.nextGaussian() - 1D) * 0.15D;
			accZ += (rand.nextGaussian() - 1D) * 2.5D;

			double size = rand.nextDouble() * this.particleScale;

			float jx = (float) (pX + accX + rand.nextGaussian() * 0.5D);
			float jy = (float) (pY + accY + rand.nextGaussian() * 0.5D);
			float jz = (float) (pZ + accZ + rand.nextGaussian() * 0.5D);

			ParticleSprites.emitQuad(buffer, this.particleTexture, jx, jy, jz,
					rotX, rotZ, rotYZ, rotXY, rotXZ, (float) size,
					0F, 0F, 1F, 1F, FOG_R, FOG_G, FOG_B, alpha);
		}
	}
}
