package com.hbm.entity.mob.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.util.math.MathHelper;

/**
 * The moon cow's gait: never turns, just head-down shuffling forward, hopping ledges.
 *
 * Two deltas:
 * - the old {@code if(this.update)} read a boolean field on {@code EntityMoveHelper}; this version's class moved to
 *   the {@code action} enum — {@code isUpdating()} checks {@code action == MOVE_TO}; so "does this trip continue"
 *   asks {@code isUpdating()}, and finishing flips {@code action} back to {@code WAIT}, the same thing as the old {@code update = false}.
 * - {@code boundingBox} became {@code getEntityBoundingBox()}, {@code MathHelper.floor_double} became {@code floor(double)},
 *   {@code movementSpeed} became uppercase {@code MOVEMENT_SPEED}. The hop is still {@code getJumpHelper().setJumping()}.
 *
 * Not one number moved (that {@code 2.500000277905201E-7D} and the 30-degree clamp are the old literals).
 */
public class EntityMoonWalkHelper extends EntityMoveHelper {

	public EntityMoonWalkHelper(EntityLiving entity) {
		super(entity);
	}

	@Override
	public void onUpdateMoveHelper() {
		this.entity.setMoveForward(0.0F);

		if(this.isUpdating()) {
			this.action = EntityMoveHelper.Action.WAIT;
			int i = MathHelper.floor(this.entity.getEntityBoundingBox().minY + 0.5D);
			double d0 = this.posX - this.entity.posX;
			double d1 = this.posZ - this.entity.posZ;
			double d2 = this.posY - (double) i;
			double d3 = d0 * d0 + d2 * d2 + d1 * d1;

			if(d3 >= 2.500000277905201E-7D) {
				float f = (float) (Math.atan2(d1, d0) * 180.0D / Math.PI) + 90.0F;
				this.entity.rotationYaw = this.limitAngle(this.entity.rotationYaw, f, 30.0F);
				float speed = (float) (this.speed * this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue());
				this.entity.setAIMoveSpeed(speed);
				this.entity.setMoveForward(-speed);

				if(d2 > 0.0D && d0 * d0 + d1 * d1 < 1.0D) {
					this.entity.getJumpHelper().setJumping();
				}
			}
		}
	}
}
