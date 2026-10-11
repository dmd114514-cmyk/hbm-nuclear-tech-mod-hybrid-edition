package com.hbm.blocks.machine;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Refueling: a car rolling over gains speed wholesale, but it's rigid track — no curves */
public class RailBooster extends RailGeneric {

	public RailBooster(String tool, int harvestLevel) {
		super(tool, harvestLevel);
		this.setMaxSpeed(1.0F);
		this.setFlexible(false);
	}

	@Override
	public void onMinecartPass(World world, EntityMinecart cart, BlockPos pos) {
		cart.motionX *= 1.15F;
		cart.motionY *= 1.15F;
		cart.motionZ *= 1.15F;
	}
}
