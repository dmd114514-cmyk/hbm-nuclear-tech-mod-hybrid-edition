package com.hbm.particle;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.hbm.util.Vec3NT;

/**
 * Ashes: a sheet drifting down, flattening onto the ground on landing, with the occasional puff of smoke.
 *
 * Layer 1 (block atlas `blocks/particle/particle_base`), pure alpha, no depth touch; the flip gives EntityFXRotating its
 * rotationPitch pair. After landing it stops billboarding and lays flat as a ground-aligned square (the old version branched the same way).
 *
 * Two necessary substitutions: the old version used entityId parity for the flip and % 5 for the smoke chance, but this version's Particle has
 * no id at all — one roll at construction stored in a field instead; `spawnParticle("smoke", …)` became EnumParticleTypes.SMOKE_LARGE.
 */
@SideOnly(Side.CLIENT)
public class ParticleAshes extends EntityFXRotating {

	private static final double FLAT_Y_OFFSET = 0.09;

	private final int parity;
	private final boolean smolders;

	public ParticleAshes(World world, double x, double y, double z, float scale) {
		super(world, x, y, z);
		this.setParticleTexture(ParticleSprites.legacySprite("particle_base"));
		this.particleMaxAge = 1200 + this.rand.nextInt(20);
		this.particleScale = scale * 0.9F + this.rand.nextFloat() * 0.2F;

		this.particleGravity = 0.01F;

		this.particleRed = this.particleGreen = this.particleBlue = this.rand.nextFloat() * 0.1F + 0.1F;

		this.parity = this.rand.nextInt(2);
		this.smolders = this.rand.nextInt(5) == 0;
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

		if(!this.onGround) this.rotationPitch += 2 * (this.parity - 0.5);

		this.motionX *= 0.95D;
		this.motionY *= 0.99D;
		this.motionZ *= 0.95D;

		boolean wasOnGround = this.onGround;
		this.move(this.motionX, this.motionY, this.motionZ);

		// a random facing on landing, so the ash sheet doesn't all lie the same way
		if(!wasOnGround && this.onGround) this.rotationPitch = this.rand.nextFloat() * 360F;

		if(this.smolders && this.onGround && this.rand.nextInt(15) == 0) {
			this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.125, this.posZ, 0, 0.05, 0);
		}
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		float timeLeft = this.particleMaxAge - (this.particleAge + partial);

		if(timeLeft < 40) {
			this.particleAlpha = timeLeft / 40F;
		} else {
			this.particleAlpha = 1F;
		}

		if(!this.onGround) {
			this.renderParticleRotated(buffer, partial, rotX, rotZ, rotYZ, rotXY, rotXZ, this.particleScale);
			return;
		}

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial - interpPosX);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial - interpPosY);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial - interpPosZ);

		int light = this.getBrightnessForRender(partial);

		Vec3NT vec = new Vec3NT(this.particleScale, 0, this.particleScale).rotateAroundYDeg(this.rotationPitch);

		flatVertex(buffer, pX + vec.x, pY - FLAT_Y_OFFSET, pZ + vec.z, this.particleTexture.getMaxU(), this.particleTexture.getMaxV(), light);
		vec.rotateAroundYDeg(90);
		flatVertex(buffer, pX + vec.x, pY - FLAT_Y_OFFSET, pZ + vec.z, this.particleTexture.getMaxU(), this.particleTexture.getMinV(), light);
		vec.rotateAroundYDeg(90);
		flatVertex(buffer, pX + vec.x, pY - FLAT_Y_OFFSET, pZ + vec.z, this.particleTexture.getMinU(), this.particleTexture.getMinV(), light);
		vec.rotateAroundYDeg(90);
		flatVertex(buffer, pX + vec.x, pY - FLAT_Y_OFFSET, pZ + vec.z, this.particleTexture.getMinU(), this.particleTexture.getMaxV(), light);
	}

	private void flatVertex(BufferBuilder buffer, double x, double y, double z, double u, double v, int light) {
		buffer.pos(x, y, z).tex(u, v).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha)
			.lightmap(light & 65535, light >> 16).endVertex();
	}
}
