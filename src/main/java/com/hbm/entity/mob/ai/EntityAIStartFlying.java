package com.hbm.entity.mob.ai;

import com.hbm.entity.mob.IFlyingCreature;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

/**
 * Flying creatures: the takeoff step. Old criteria copied — hit, on fire, or randomly (averaging every 30 seconds) lifts off.
 *
 * The two state bits live in {@link IFlyingCreature}; criteria written against the interface, not locked to one species.
 *
 * One delta: the old target query {@code getAITarget()} doesn't exist on the base here (decompile-verified across Entity, EntityLivingBase,
 * and EntityLiving); its stand-in is {@code EntityLiving.getAttackTarget()} — the old getter read exactly
 * what the target selector stuffed in, same semantics. Since that getter sits on EntityLiving rather than the base,
 * the holding type narrows from {@code EntityLivingBase} to {@code EntityLiving}.
 */
public class EntityAIStartFlying extends EntityAIBase {

	private final EntityLiving living;
	private final IFlyingCreature flying;

	public EntityAIStartFlying(EntityLiving living, IFlyingCreature flying) {
		this.living = living;
		this.flying = flying;
	}

	@Override
	public boolean shouldExecute() {
		return this.flying.getFlyingState() == IFlyingCreature.STATE_WALKING
				&& (this.living.getAttackTarget() != null || this.living.isBurning() || this.living.getRNG().nextInt(600) == 0);
	}

	@Override
	public void startExecuting() {
		this.flying.setFlyingState(IFlyingCreature.STATE_FLYING);
	}
}
