package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IItemFluidIdentifier {

	/**
	 * In a GUI both world and pos may be null — only the item's own recorded type is needed then.
	 * In-world use passes an accurate pos, letting implementers make environment-aware decisions.
	 */
	public FluidType getType(World world, BlockPos pos, ItemStack stack);
}
