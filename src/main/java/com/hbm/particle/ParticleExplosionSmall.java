package com.hbm.particle;

import java.awt.Color;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The small explosion: a small tumbling fireball, spreading as it grays out.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch; rotation rides EntityFXRotating.
 * The color/brightness line copies the old one: saturation drops first, brightness clamped to (hue*0.01-0.1 ~ 1), alpha at half —
 * that 1/2 rode the old setColorRGBA_F and must now be passed to the base explicitly to take effect (see EntityFXRotating's note).
 * The old flip came from entityId parity; no id here, one roll at construction stored in a field.
 */
@SideOnly(Side.CLIENT)
public class ParticleExplosionSmall extends EntityFXRotating {

	public float hue;

	private final int parity;

	public ParticleExplosionSmall(World world, double x, double y, double z, float scale, float speedMult) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.particleMaxAge = 25 + this.rand.nextInt(10);
		this.particleScale = scale * 0.9F + this.rand.nextFloat() * 0.2F;

		this.motionX = world.rand.nextGaussian() * speedMult;
		this.motionZ = world.rand.nextGaussian() * speedMult;

		this.particleGravity = this.rand.nextFloat() * -0.01F;

		this.hue = 20F + this.rand.nextFloat() * 20F;
		Color color = Color.getHSBColor(this.hue / 255F, 1F, 1F);
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;

		// the old noClip = true
		this.canCollide = false;

		this.parity = this.rand.nextInt(2);
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
		this.rotationPitch += (1 - ageScaled) * 5 * (this.parity - 0.5);

		this.motionX *= 0.65D;
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

		Color color = Color.getHSBColor(this.hue / 255F, Math.max(1F - (float) ageScaled * 2F, 0), MathHelper.clamp(1.25F - (float) ageScaled * 2F, this.hue * 0.01F - 0.1F, 1F));
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;

		this.particleAlpha = (float) Math.pow(1 - Math.min(ageScaled, 1), 0.25);

		double scale = (0.25 + 1 - Math.pow(1 - ageScaled, 4) + (this.particleAge + partial) * 0.02) * this.particleScale;

		this.renderParticleRotated(buffer, partial, rotX, rotZ, rotYZ, rotXY, rotXZ, scale,
				this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha * 0.5F);
	}
}
