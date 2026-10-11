package com.hbm.entity.mob;

import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * The ghost: unhittable, forever at full health, dispersing itself the moment a player comes near (the old 50-block check).
 *
 * Three conventions this version:
 * - {@code isEntityInvulnerable()} now takes a {@code DamageSource}; the always-true semantics unchanged.
 * - {@code worldObj}/{@code boundingBox} became this version's {@code world}/{@code getEntityBoundingBox()}.
 * - the old {@code this.renderDistanceWeight *= 10} (letting ghosts draw from further out) moved effective points:
 *   this version's {@code Entity.renderDistanceWeight} is a private static field vanilla rendering no longer reads,
 *   distance and frustum checks both collapsed into {@code RenderManager.shouldRender}, so the right answer is {@code ignoreFrustumCheck}
 *   (the family's 25× chicken does exactly that in 1.12.2), set true below; the multiplier tier can't be done here.
 *
 * No drops written: the old version had none, and this version points at no table either.
 */
public class EntityGhost extends EntityCreature {

	public EntityGhost(World world) {
		super(world);

		this.tasks.addTask(0, new EntityAISwimming(this));
		this.tasks.addTask(1, new EntityAIWander(this, 1.0D));
		this.tasks.addTask(2, new EntityAILookIdle(this));
		this.tasks.addTask(3, new EntityAIWatchClosest(this, EntityPlayer.class, 15.0F));

		this.ignoreFrustumCheck = true;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2D);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!this.world.isRemote) {
			double despawnRange = 50;
			List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class,
					this.getEntityBoundingBox().expand(despawnRange, despawnRange, despawnRange));
			if(!players.isEmpty()) this.setDead();
		}
	}

	/*As in the old version: whoever writes its health, the real value never changes — always received as full */
	@Override
	public void setHealth(float health) {
		super.setHealth(this.getMaxHealth());
	}

	@Override
	public boolean isEntityInvulnerable(DamageSource source) {
		return true;
	}
}
