package com.hbm.particle;

import java.awt.Color;

import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The gas flame: vanilla's smoke particle wearing a yellow-to-red-to-dark tint that follows age.
 * A "normal" particle — it doesn't draw itself; the inherited renderParticle drops quads into the ParticleManager's shared buffer,
 * so color changes only touch the particleRed/Green/Blue fields (written per-vertex downstream), never GL state.
 */
@SideOnly(Side.CLIENT)
public class ParticleGasFlame extends ParticleSmokeNormal {

	float colorMod = 1.0F;

	public ParticleGasFlame(World world, double x, double y, double z, double mX, double mY, double mZ, float scale) {
		// 1.12.2's smoke particle constructor is protected; subclasses may call it as before
		super(world, x, y, z, mX, mY * 1.5, mZ, scale);
		this.updateColor();
		this.particleScale = scale;
		this.colorMod = 0.8F + rand.nextFloat() * 0.2F;
		this.particleMaxAge = 30 + rand.nextInt(13);
	}

	@Override
	public void onUpdate() {

		double prevMo = this.motionY;
		super.onUpdate();
		this.updateColor();

		this.motionY = prevMo;
		this.motionX *= 0.75D;
		this.motionY += 0.005D;
		this.motionZ *= 0.75D;
	}

	protected void updateColor() {

		float time = (float) this.particleAge / (float) this.particleMaxAge;
		Color color = Color.getHSBColor(Math.max((60 - time * 100) / 360F, 0.0F), 1 - time * 0.25F, 1 - time * 0.5F);

		this.particleRed = color.getRed() / 255F * colorMod;
		this.particleGreen = color.getGreen() / 255F * colorMod;
		this.particleBlue = color.getBlue() / 255F * colorMod;
	}

	@Override
	public int getBrightnessForRender(float partial) {
		return 15728880;
	}
}
