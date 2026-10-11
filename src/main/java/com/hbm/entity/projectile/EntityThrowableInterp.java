package com.hbm.entity.projectile;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The throwable's client-side interpolation layer: the server reports pose every few ticks, and the client doesn't snap to it —
 * it closes in over approachNum() ticks, so bullets don't look jumpy.
 *
 * Only names follow this version: worldObj became world, wrapAngleTo180_double became wrapDegrees
 * (it takes and returns double, so not even the formula changes).
 *
 * The server half is still the parent's physics; this class takes over onUpdate on the client only, touching nothing else.
 */
public abstract class EntityThrowableInterp extends EntityThrowableNT {

	protected int turnProgress;
	protected double syncPosX;
	protected double syncPosY;
	protected double syncPosZ;
	protected double syncYaw;
	protected double syncPitch;
	@SideOnly(Side.CLIENT)
	protected double velocityX;
	@SideOnly(Side.CLIENT)
	protected double velocityY;
	@SideOnly(Side.CLIENT)
	protected double velocityZ;

	public EntityThrowableInterp(World world) {
		super(world);
	}

	public EntityThrowableInterp(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public void onUpdate() {

		if(!world.isRemote) {
			super.onUpdate();
		} else {
			this.lastTickPosX = this.posX;
			this.lastTickPosY = this.posY;
			this.lastTickPosZ = this.posZ;
			if(this.turnProgress > 0) {
				double interpX = this.posX + (this.syncPosX - this.posX) / (double) this.turnProgress;
				double interpY = this.posY + (this.syncPosY - this.posY) / (double) this.turnProgress;
				double interpZ = this.posZ + (this.syncPosZ - this.posZ) / (double) this.turnProgress;
				double d = MathHelper.wrapDegrees(this.syncYaw - (double) this.rotationYaw);
				this.rotationYaw = (float) ((double) this.rotationYaw + d / (double) this.turnProgress);
				this.rotationPitch = (float) ((double) this.rotationPitch + (this.syncPitch - (double) this.rotationPitch) / (double) this.turnProgress);
				--this.turnProgress;
				this.setPosition(interpX, interpY, interpZ);
			} else {
				this.setPosition(this.posX, this.posY, this.posZ);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void setVelocity(double velX, double velY, double velZ) {
		this.velocityX = this.motionX = velX;
		this.velocityY = this.motionY = velY;
		this.velocityZ = this.motionZ = velZ;
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int theNumberThree) {
		this.syncPosX = x;
		this.syncPosY = y;
		this.syncPosZ = z;
		this.syncYaw = yaw;
		this.syncPitch = pitch;
		this.turnProgress = theNumberThree + approachNum();
		this.motionX = this.velocityX;
		this.motionY = this.velocityY;
		this.motionZ = this.velocityZ;
	}

	/**
	 * Added on top of the base approach step of "3": bigger means smoother but further from truth.
	 */
	public int approachNum() {
		return 0;
	}
}
