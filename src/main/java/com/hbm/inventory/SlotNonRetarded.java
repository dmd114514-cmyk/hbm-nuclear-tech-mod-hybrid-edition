package com.hbm.inventory;

import com.hbm.interfaces.NotableComments;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Vanilla's default slot behavior has a trap: it never asks IInventory's isItemValidForSlot,
 * so "this machine's slot 3 rejects this item" is completely ignored while dragging. This class wires it back in.
 *
 * Also the slot's display cap must follow the inventory's own limit — set a bigger number yourself and
 * the GUI simply stops drawing; faked information isn't a favor either.
 */
@NotableComments
public class SlotNonRetarded extends Slot {

	public SlotNonRetarded(IInventory inventory, int id, int x, int y) {
		super(inventory, id, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return inventory.isItemValidForSlot(slotNumber, stack);
	}

	@Override
	public int getSlotStackLimit() {
		return Math.max(inventory.getInventoryStackLimit(), getHasStack() ? getStack().getCount() : 1);
	}
}
