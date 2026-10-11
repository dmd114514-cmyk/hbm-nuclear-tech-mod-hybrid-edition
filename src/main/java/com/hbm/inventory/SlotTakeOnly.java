package com.hbm.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotTakeOnly extends SlotItemHandler {

	public SlotTakeOnly(IItemHandler handler, int i, int j, int k) {
		super(handler, i, j, k);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
}
