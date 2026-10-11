package com.hbm.entity.mob;

import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The fish family's shared base: its own "pick an underwater point, swim there" driver, no vanilla navigation.
 *
 * Three deltas, all on the old hooks:
 * - the old `getNavigator().setCanSwim(true)`: setCanSwim lives only on PathNavigateGround here,
 *   and the right answer for swimmers is swapping the navigator for PathNavigateSwimmer, hence overriding createNavigator.
 * - the old `EntityAIWander(this, swimSpeed)`: both constructors take only EntityCreature here,
 *   which swimmers aren't; and travel()'s in-water branch never reads the AI's moveForward/moveStrafing anyway,
 *   so that goal never worked in water. Dropping it changes nothing; swimming stays on the updateSwimTarget chain,
 *   leaving swimSpeed without an effect point (kept so subclass signatures and a future real speed hook don't touch the base).
 * - the old `moveEntityWithHeading(strafe, forward)` is this version's `travel(strafe, up, forward)`;
 *   the limb-swing math sat at its end, and bypassing super means re-adding it here, as the old version did.
 *
 * Also: the old empty `damageEntity` forward and the always-true `isAIEnabled` (AI is always on here) are deleted outright,
 * not behavior deltas.
 */
public abstract class EntityFish extends EntityWaterMob {

	private Vec3d currentSwimTarget;

	public EntityFish(World world, double swimSpeed, float watchDistance) {
		super(world);

		this.tasks.addTask(0, new EntityAIWatchClosest(this, EntityPlayer.class, watchDistance));
		this.tasks.addTask(1, new EntityAILookIdle(this));

		this.currentSwimTarget = new Vec3d(this.posX, this.posY, this.posZ);
		this.setSize(2.0F, 2.0F);
	}

	@Override
	protected PathNavigate createNavigator(World world) {
		return new PathNavigateSwimmer(this, world);
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(15.0);
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(10.0);
	}

	/*Out of water it flops: the old block lived in updateAITasks; the same-named method still exists here, kept in place */
	@Override
	protected void updateAITasks() {
		super.updateAITasks();

		if(!this.isInWater()) {
			this.rotationPitch += 0.2F;
			this.rotationYaw = 0.0F;
			this.setJumping(true);
			if(this.onGround) {
				this.addVelocity(0.4 * rand.nextDouble() - 0.4 * rand.nextDouble(), 0.0, 0.4 * rand.nextDouble() - 0.4 * rand.nextDouble());
			}
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		this.updateSwimTarget();
		this.moveTowardsTarget();
		updateRotation();
	}

	private void updateSwimTarget() {
		if(rand.nextInt(200) == 0 || !this.isInWater()) {
			double targetX = this.posX + (rand.nextDouble() - 0.5) * 20.0;
			double targetY = MathHelper.clamp(this.posY + (rand.nextDouble() - 0.5) * 8.0, 0, this.world.getHeight() - 1);
			double targetZ = this.posZ + (rand.nextDouble() - 0.5) * 20.0;
			this.currentSwimTarget = new Vec3d(targetX, targetY, targetZ);
		}
	}

	private void moveTowardsTarget() {
		double deltaX = this.currentSwimTarget.x - this.posX;
		double deltaY = this.currentSwimTarget.y - this.posY;
		double deltaZ = this.currentSwimTarget.z - this.posZ;
		double distance = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

		double dirX = deltaX / distance;
		double dirY = deltaY / distance;
		double dirZ = deltaZ / distance;

		double speed = 0.01;
		this.motionX += dirX * speed + (rand.nextDouble() - 0.5) * 0.02;
		this.motionY += dirY * speed + (rand.nextDouble() - 0.5) * 0.02;
		this.motionZ += dirZ * speed + (rand.nextDouble() - 0.5) * 0.02;

		updateRotation((float) dirX, (float) dirY, (float) dirZ);
	}

	@Override
	public void travel(float strafe, float vertical, float forward) {
		if(this.isInWater()) {
			this.motionY *= 0.8F;
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

			// limb swing was computed at the end of super.travel; bypassed here, so the same formula is re-added
			this.prevLimbSwingAmount = this.limbSwingAmount;
			double d0 = this.posX - this.prevPosX;
			double d1 = this.posZ - this.prevPosZ;
			// the old sqrt_double actually returned float (the 1.7.10 naming trap); this version's sqrt(double) honestly returns
			// double, hence the explicit narrowing — the formula itself untouched
			float f6 = (float) MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

			if(f6 > 1.0F) {
				f6 = 1.0F;
			}

			this.limbSwingAmount += (f6 - this.limbSwingAmount) * 0.4F;
			this.limbSwing += this.limbSwingAmount;
		} else {
			super.travel(strafe, vertical, forward);
		}
	}

	private void updateRotation() {
		double deltaX = this.motionX;
		double deltaZ = this.motionZ;
		double deltaY = this.motionY;
		float targetYaw = (float) (Math.atan2(deltaZ, deltaX) * (180D / Math.PI)) - 90F;
		float targetPitch = (float) -(Math.atan2(deltaY, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ)) * (180D / Math.PI));

		this.rotationYaw = this.updateRotation(this.rotationYaw, targetYaw, 10.0F);
		this.rotationPitch = this.updateRotation(this.rotationPitch, targetPitch, 10.0F);
	}

	/*At most maxIncrement degrees per tick, halved past 180 and clamped — wrapAngleTo180_float is wrapDegrees here */
	private float updateRotation(float currentRotation, float targetRotation, float maxIncrement) {
		float deltaRotation = MathHelper.wrapDegrees(targetRotation - currentRotation);
		if(deltaRotation > maxIncrement) {
			deltaRotation = maxIncrement;
		}
		if(deltaRotation < -maxIncrement) {
			deltaRotation = -maxIncrement;
		}
		return currentRotation + deltaRotation;
	}
}
