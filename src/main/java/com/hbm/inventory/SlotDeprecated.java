package com.hbm.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/** For old saves' dead slots: things can go in but nothing interacts */
public class SlotDeprecated extends SlotItemHandler {

	public SlotDeprecated(IItemHandler handler, int id, int x, int y) {
		super(handler, id, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}

	// 1.12.2 renamed the old func_111238_b to isEnabled
	@Override
	public boolean isEnabled() {
		return false;
	}
}
