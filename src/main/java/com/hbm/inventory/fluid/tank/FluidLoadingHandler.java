package com.hbm.inventory.fluid.tank;

import net.minecraft.item.ItemStack;

/**
 * Fill/drain handling between tanks and held containers. A pluggable handler list,
 * because the item families (plain cells, fillables, the infinite bucket) differ too much in how they decide,
 * and one if-chain would become a monster nobody dares touch.
 */
public abstract class FluidLoadingHandler {

	public abstract boolean fillItem(ItemStack[] slots, int in, int out, FluidTank tank);
	public abstract boolean emptyItem(ItemStack[] slots, int in, int out, FluidTank tank);
}
