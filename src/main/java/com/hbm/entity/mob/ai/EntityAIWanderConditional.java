package com.hbm.entity.mob.ai;

import java.util.function.Predicate;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.Vec3d;

/**
 * A conditional vanilla {@code EntityAIWander}: one extra criterion — while flying, don't wander the ground.
 *
 * Three deltas:
 * - target picking: the old {@code RandomPositionGenerator.findRandomTarget(EntityCreature, int, int)} returned
 *   {@code Vec3}; {@code Vec3d} here, fields renamed {@code xCoord} → {@code x}.
 * - the old {@code creature.getAge() >= 100} return-false gate hangs on
 *   {@code EntityLivingBase.getIdleTime()} here (decompile-verified: {@code EntityCreature} no longer has getAge,
 *   the counter moved to the base as idleTime, incrementing per tick and zeroing on hit). Vanilla's own
 *   {@code EntityAIWander} uses the same line in the same spot, so this follows it.
 *   The feel shifts slightly: the old gate asked "has this mob existed long enough", this one "has enough time passed since the last hit" —
 *   for the pigeon both read "don't wander while busy"; the direction is unchanged.
 * - the "keep going?" hook was renamed on {@code EntityAIBase},
 *   {@code continueExecuting()} becoming {@code shouldContinueExecuting()}; the body untouched.
 */
public class EntityAIWanderConditional extends EntityAIBase {

	private final EntityCreature creature;
	private final double speed;
	private final Predicate<EntityLivingBase> condition;
	private double xPosition;
	private double yPosition;
	private double zPosition;

	public EntityAIWanderConditional(EntityCreature creature, double speed, Predicate<EntityLivingBase> condition) {
		this.creature = creature;
		this.speed = speed;
		this.condition = condition;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {

		if(!this.condition.test(this.creature)) {
			return false;
		}

		if(this.creature.getIdleTime() >= 100) {
			return false;
		} else if(this.creature.getRNG().nextInt(120) != 0) {
			return false;
		} else {
			Vec3d vec3d = RandomPositionGenerator.findRandomTarget(this.creature, 10, 7);

			if(vec3d == null) {
				return false;
			} else {
				this.xPosition = vec3d.x;
				this.yPosition = vec3d.y;
				this.zPosition = vec3d.z;
				return true;
			}
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return !this.creature.getNavigator().noPath() && this.condition.test(this.creature);
	}

	@Override
	public void startExecuting() {
		this.creature.getNavigator().tryMoveToXYZ(this.xPosition, this.yPosition, this.zPosition, this.speed);
	}
}
