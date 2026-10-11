package com.hbm.items.special;

import com.hbm.entity.item.EntityItemWaste;
import com.hbm.items.ItemBase;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * The nuclear waste. Dropped on the ground it never despawns (infinite lifespan) and rides a custom drop entity —
 * one immune to all damage, unkillable and unpickable. The long-half-life family uses it as a base.
 */
public class ItemNuclearWaste extends ItemBase {

	@Override
	public boolean hasCustomEntity(ItemStack stack) {
		return true;
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return Integer.MAX_VALUE;
	}

	@Override
	public Entity createEntity(World world, Entity entityItem, ItemStack itemstack) {

		EntityItemWaste entity = new EntityItemWaste(world, entityItem.posX, entityItem.posY, entityItem.posZ, itemstack);
		entity.motionX = entityItem.motionX;
		entity.motionY = entityItem.motionY;
		entity.motionZ = entityItem.motionZ;
		// 1.12.2 replaced the old delayBeforeCanPickup field with setPickupDelay()
		entity.setPickupDelay(10);

		entityItem.setDead();

		return entity;
	}
}
