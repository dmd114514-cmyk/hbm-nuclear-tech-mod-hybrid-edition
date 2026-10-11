package com.hbm.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The glow: a white halo swelling to maximum then shrinking back.
 *
 * Layer 1 (block atlas) + texture `blocks/particle/yelflare`; the quad reuses the parent's vanilla billboard drawing,
 * so this class carries no renderParticle — the old hand-copied one was the parent's implementation anyway.
 * Brightness pinned at full (15728880 = sky light 240 shifted left 16); the getBrightnessForRender override kept as before.
 */
@SideOnly(Side.CLIENT)
public class ParticleGlow extends Particle {

	private final float peakScale;

	public ParticleGlow(World world, double x, double y, double z, double mX, double mY, double mZ, float scale) {
		super(world, x, y, z, mX, mY, mZ);
		this.setParticleTexture(ParticleSprites.legacySprite("yelflare"));
		this.particleRed = 1.0F;
		this.particleGreen = 1.0F;
		this.particleBlue = 1.0F;
		this.peakScale = scale;
		this.particleScale = scale;
		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.particleAge = 1;
		this.particleMaxAge = 50 + world.rand.nextInt(50);
		this.particleAlpha = 0.8F;
		// the old noClip = true
		this.canCollide = false;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		// swell to peak then recede: t runs the 0→1 triangle wave
		float t = (float) this.particleAge / (float) this.particleMaxAge;
		t = Math.abs(0.5F - (t - 0.5F)) * 2;

		this.particleScale = this.peakScale * (float) Math.sin(t * Math.PI / 2);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public int getBrightnessForRender(float partial) {
		return 15728880;
	}
}
