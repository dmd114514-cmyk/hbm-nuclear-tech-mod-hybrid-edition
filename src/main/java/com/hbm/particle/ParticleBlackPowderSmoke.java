package com.hbm.particle;

import java.awt.Color;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Black powder smoke: a colorful puff tumbling about its own axis, saturation fading to white with age.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch;
 * the rotating billboard is EntityFXRotating's job (Particle isn't an Entity here and has no rotationPitch,
 * so the field rides the base class).
 *
 * The old flip direction came from entityId parity; this version's Particle measurably has no id,
 * so one roll at construction is stored — preserving "fixed per particle, never per frame". The roll happens after the existing draws,
 * so the maxAge/scale/hue draw order doesn't shift.
 */
@SideOnly(Side.CLIENT)
public class ParticleBlackPowderSmoke extends EntityFXRotating {

	public float hue;

	private final int parity;

	public ParticleBlackPowderSmoke(World world, double x, double y, double z, float scale) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.particleMaxAge = 30 + this.rand.nextInt(15);
		this.particleScale = scale * 0.9F + this.rand.nextFloat() * 0.2F;

		this.particleGravity = 0F;

		this.hue = 20F + this.rand.nextFloat() * 20F;
		Color color = Color.getHSBColor(this.hue / 255F, 1F, 1F);
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;

		// the old noClip = true
		this.canCollide = false;

		this.parity = this.rand.nextInt(2);
	}

	/*The creator gives an initial velocity per the packet's throw direction; Particle.motionX is protected and unwritable outside, so a same-style door */
	public ParticleBlackPowderSmoke setMotion(double mx, double my, double mz) {
		this.motionX = mx;
		this.motionY = my;
		this.motionZ = mz;
		return this;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAge++;

		if(this.particleAge >= this.particleMaxAge) this.setExpired();

		this.motionY -= this.particleGravity;
		this.prevRotationPitch = this.rotationPitch;

		float ageScaled = (float) this.particleAge / (float) this.particleMaxAge;
		this.rotationPitch += (1 - ageScaled) * 2 * (this.parity - 0.5);

		this.motionX *= 0.65D;
		this.motionY *= 0.65D;
		this.motionZ *= 0.65D;

		this.move(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		double ageScaled = (double) (this.particleAge + partial) / (double) this.particleMaxAge;

		// saturation drains first, then brightness rises: the smoke fades from powder colors to gray-white
		Color color = Color.getHSBColor(this.hue / 255F, Math.max(1F - (float) ageScaled * 4F, 0), MathHelper.clamp(1.25F - (float) ageScaled * 2F, 0.7F, 1F));
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;

		this.particleAlpha = (float) Math.pow(1 - Math.min(ageScaled, 1), 0.25);

		double scale = (0.25 + ageScaled + (this.particleAge + partial) * 0.025) * this.particleScale;

		// The old version called setColorRGBA_F(…, alpha * 0.25F) before letting the base emit vertices — that quarter opacity was real and took effect
		this.renderParticleRotated(buffer, partial, rotX, rotZ, rotYZ, rotXY, rotXZ, scale,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.25F);
	}
}
