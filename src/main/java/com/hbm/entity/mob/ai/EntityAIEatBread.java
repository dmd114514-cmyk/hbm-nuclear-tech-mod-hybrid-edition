package com.hbm.entity.mob.ai;

import java.util.List;

import com.hbm.entity.mob.EntityPigeon;
import com.hbm.entity.mob.IFlyingCreature;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;

/**
 * The pigeon's foraging: bread on the ground gets walked to and pecked; a full round of eating fattens it up ({@code isFat}).
 *
 * The deltas are all on the "the dropped-item entity changed face" side:
 * - the old {@code getEntityItem()}/{@code setEntityItemStack()} are {@code getItem()}/{@code setItem()} here,
 *   counts via {@code getCount()}/{@code shrink(1)}.
 * - the old four steps — {@code new EntityItem(world)} + {@code setPosition} + stuff the stack + {@code spawnEntityInWorld} —
 *   collapse into this version's four-arg constructor (coordinates and stack ride along at construction).
 * - the old {@code "random.eat"} string wants a {@code SoundEvent} here; vanilla's {@code ENTITY_GENERIC_EAT} it is.
 * - the old {@code getDistanceToEntity} is gone, replaced by {@code getDistance}, still returning float.
 * - the "keep going?" hook was renamed on {@code EntityAIBase},
 *   {@code continueExecuting()} became {@code shouldContinueExecuting()} (decompile-verified); the body untouched.
 *
 * The leftover-bread action follows the old version: one dice roll before eating (one in three), and on a hit the extra bite goes back on the ground —
 * so both "swallowed whole" and "half left" outcomes remain; the criterion unchanged.
 */
public class EntityAIEatBread extends EntityAIBase {

	private final EntityPigeon pigeon;
	private final double speed;
	private EntityItem item;

	public EntityAIEatBread(EntityPigeon pigeon, double speed) {
		this.pigeon = pigeon;
		this.speed = speed;
		this.setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {

		if(this.pigeon.isFat() || this.pigeon.getFlyingState() != IFlyingCreature.STATE_WALKING) {
			return false;
		}

		List<EntityItem> items = this.pigeon.world.getEntitiesWithinAABB(EntityItem.class, this.pigeon.getEntityBoundingBox().expand(10.0D, 10.0D, 10.0D));

		for(EntityItem entityitem : items) {

			if(entityitem.getItem().getItem() == Items.BREAD) {
				this.item = entityitem;
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean shouldContinueExecuting() {
		return this.item != null && !this.item.isDead && this.shouldExecute();
	}

	@Override
	public void updateTask() {

		this.pigeon.getLookHelper().setLookPositionWithEntity(this.item, 30.0F, (float) this.pigeon.getVerticalFaceSpeed());

		if(this.pigeon.getDistance(this.item) > 1.0F) {
			this.pigeon.getNavigator().tryMoveToEntityLiving(this.item, this.speed);
		} else {

			if(this.pigeon.getRNG().nextInt(3) == 0) {
				ItemStack stack = this.item.getItem();

				if(stack.getCount() > 1) {
					stack.shrink(1);
					this.pigeon.world.spawnEntity(new EntityItem(this.pigeon.world, this.item.posX, this.item.posY, this.item.posZ, stack));
				}

				this.item.setDead();
			}

			this.pigeon.setFat(true);
			this.pigeon.playSound(SoundEvents.ENTITY_GENERIC_EAT, 0.5F + 0.5F * this.pigeon.getRNG().nextInt(2), (this.pigeon.getRNG().nextFloat() - this.pigeon.getRNG().nextFloat()) * 0.2F + 1.0F);
		}
	}
}
