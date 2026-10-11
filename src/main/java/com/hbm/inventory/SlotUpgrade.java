package com.hbm.inventory;

import com.hbm.items.machine.ItemMachineUpgrade;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** Upgrade slot: takes upgrade kits only. The old onSlotChange that just called super had no behavior; not ported per "no empty shells". */
public class SlotUpgrade extends Slot {

	public SlotUpgrade(IInventory inventory, int i, int j, int k) {
		super(inventory, i, j, k);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ItemMachineUpgrade;
	}
}
