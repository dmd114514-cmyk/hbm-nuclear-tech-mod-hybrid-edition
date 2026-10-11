package com.hbm.inventory;

import com.hbm.inventory.fluid.FluidType;

import net.minecraft.item.ItemStack;

public class FluidContainer {

	//the filled container (a deuterium cell, say)
	public ItemStack fullContainer;
	//the empty container (an empty cell)
	public ItemStack emptyContainer;
	//the fluid type inside (deuterium, say)
	public FluidType type;
	//the amount, in mB (1000, say)
	public int content;

	public FluidContainer(ItemStack full, ItemStack empty, FluidType type, int amount) {
		fullContainer = full;
		emptyContainer = empty;
		this.type = type;
		content = amount;
	}

}
