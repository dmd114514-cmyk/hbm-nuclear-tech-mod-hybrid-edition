package com.hbm.entity.mob.ai;

import java.util.function.Predicate;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigateGround;

/**
 * A conditional vanilla EntityAISwimming: one extra criterion — while the flying state isn't "walk", don't splash in water,
 * so the swim task and flight's vertical control don't fight over the same motionY.
 *
 * Two deltas:
 * - the old {@code handleLavaMovement()} is entirely gone (decompile-verified: Entity keeps only
 *   {@code isInWater}/{@code isInLava}/{@code handleWaterMovement}), and vanilla's own AISwimming
 *   switched to {@code isInLava} here too ⇒ same-shaped criterion.
 * - the old {@code getNavigator().setCanSwim(true)} can't be called directly: that switch lives only on
 *   {@code PathNavigateGround} (the base {@code PathNavigate} lacks it, decompile-verified),
 *   and vanilla's own AISwimming also instances first then forwards ⇒ same move. Ground-walking mobs (the pigeon)
 *   get exactly PathNavigateGround, so the line works; the vacuum-navigation family later
 *   takes the other branch and should use {@code PathNavigateFlying.setCanFloat}, not a parameter here.
 */
public class EntityAISwimmingConditional extends EntityAIBase {

	private final EntityLiving living;
	private final Predicate<EntityLivingBase> condition;

	public EntityAISwimmingConditional(EntityLiving living, Predicate<EntityLivingBase> condition) {
		this.living = living;
		this.condition = condition;
		this.setMutexBits(4);

		if(living.getNavigator() instanceof PathNavigateGround) {
			((PathNavigateGround) living.getNavigator()).setCanSwim(true);
		}
	}

	@Override
	public boolean shouldExecute() {
		return (this.living.isInWater() || this.living.isInLava()) && this.condition.test(this.living);
	}

	@Override
	public void updateTask() {
		if(this.living.getRNG().nextFloat() < 0.8F) {
			this.living.getJumpHelper().setJumping();
		}
	}
}
