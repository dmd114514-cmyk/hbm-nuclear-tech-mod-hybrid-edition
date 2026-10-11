package com.hbm.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * Lets access ports give per-direction inventory behavior instead of the old ISidedInventory one-size-fits-all.
 * Item movers check whether the port knows this interface before touching it.
 */
public interface IConditionalInvAccess {

	public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack);
	public default boolean canInsertItem(BlockPos pos, int slot, ItemStack stack, EnumFacing side) { return isItemValidForSlot(pos, slot, stack); }
	public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, EnumFacing side);
	public int[] getAccessibleSlotsFromSide(BlockPos pos, EnumFacing side);
}
