package com.hbm.particle;

import java.awt.Color;

import com.hbm.particle.helper.FlameCreator;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The flamethrower's tongue: tinted by fuel type, drifting upward while tumbling.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch; rotation rides EntityFXRotating.
 * The four fuels' fade curves (pure oxygen runs cold, black powder runs dark, the other three whiten) copy the old version verbatim —
 * don't merge them into one universal curve; those differences are exactly why the fuels look different.
 *
 * One change per "what actually took effect": the old getBrightnessForRender override returned 15728880, but its renderParticle
 * explicitly called setBrightness(240), so the former was never read; this version's base reads getBrightnessForRender uniformly,
 * so the door is changed to 240 (splitting as block light 240, sky light 0), reproducing the brightness that actually reached the screen.
 * Flip direction per family convention: entityId parity in the old version, no id here, one roll at construction stored in a field.
 */
@SideOnly(Side.CLIENT)
public class ParticleFlamethrower extends EntityFXRotating {

	public int type;

	private final int parity;

	public ParticleFlamethrower(World world, double x, double y, double z, int type) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.particleMaxAge = 20 + this.rand.nextInt(10);
		this.particleScale = 0.5F;
		this.type = type;

		this.motionX = world.rand.nextGaussian() * 0.02;
		this.motionZ = world.rand.nextGaussian() * 0.02;

		float initialColor = 15F + this.rand.nextFloat() * 25F;

		if(type == FlameCreator.META_BALEFIRE) initialColor = 65F + this.rand.nextFloat() * 35F;
		if(type == FlameCreator.META_DIGAMMA) initialColor = 0F - this.rand.nextFloat() * 15F;

		Color color = Color.getHSBColor(initialColor / 255F, 1F, 1F);
		this.particleRed = color.getRed() / 255F;
		this.particleGreen = color.getGreen() / 255F;
		this.particleBlue = color.getBlue() / 255F;

		// these two tiers skip the HSB route: pure oxygen is all white, black powder is darkened by the fade curve below
		if(type == FlameCreator.META_OXY) this.particleRed = this.particleGreen = this.particleBlue = 1F;
		if(type == FlameCreator.META_BLACK) this.particleRed = this.particleGreen = this.particleBlue = 1F;

		this.parity = this.rand.nextInt(2);
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		this.particleAge++;

		if(this.particleAge >= this.particleMaxAge) this.setExpired();

		this.motionX *= 0.91D;
		this.motionY *= 0.91D;
		this.motionZ *= 0.91D;

		// the flame climbs
		this.motionY += 0.01D;
		this.prevRotationPitch = this.rotationPitch;
		this.rotationPitch += 30 * (this.parity - 0.5);

		this.move(this.motionX, this.motionY, this.motionZ);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public int getBrightnessForRender(float partial) {
		return 240;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		double ageScaled = (double) this.particleAge / (double) this.particleMaxAge;

		float red = this.particleRed;
		float green = this.particleGreen;
		float blue = this.particleBlue;
		float alpha;
		float vertexAlpha;

		if(this.type == FlameCreator.META_OXY) {
			alpha = (float) (1 - ageScaled);
			float add = (float) ageScaled * 1.25F - 0.25F;
			red -= add;
			green -= add * 0.75F;
			vertexAlpha = alpha;
		} else if(this.type == FlameCreator.META_BLACK) {
			alpha = (float) (1 - ageScaled);
			float add = (float) ageScaled * 2F - 0.25F;
			red -= add * 0.75F;
			green -= add;
			blue -= add * 0.5F;
			vertexAlpha = alpha;
		} else {
			alpha = (float) Math.pow(1 - Math.min(ageScaled, 1), 0.5);
			float add = 0.75F - (float) ageScaled;
			red += add;
			green += add;
			blue += add;
			// the regular fuel tier draws at half opacity: the field still holds the unhalved value
			vertexAlpha = alpha * 0.5F;
		}

		this.particleAlpha = alpha;

		double scale = (ageScaled * 1.25 + 0.25) * this.particleScale;

		// the old version passed this tinting through the tessellator's current color; per-vertex explicit values are mandatory here (see EntityFXRotating's note)
		this.renderParticleRotated(buffer, partial, rotX, rotZ, rotYZ, rotXY, rotXZ, scale, red, green, blue, vertexAlpha);
	}
}
