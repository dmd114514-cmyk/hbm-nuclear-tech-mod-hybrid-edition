package com.hbm.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotPattern extends SlotItemHandler {

	protected boolean canHover = true;

	protected boolean allowStackSize = false;

	public SlotPattern(IItemHandler handler, int index, int x, int y) {
		super(handler, index, x, y);
	}

	public SlotPattern allowStackSize() {
		this.allowStackSize = true;
		return this;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		return false;
	}

	@Override
	public int getSlotStackLimit() {
		return allowStackSize ? 64 : 1;
	}

	@Override
	public void putStack(ItemStack stack) {
		// 1.12.2 says ItemStack.EMPTY for empty, not null
		if (!stack.isEmpty()) {
			stack = stack.copy();

			if(!allowStackSize)
				stack.setCount(1);
		}
		super.putStack(stack);
	}

	public SlotPattern disableHover() {
		this.canHover = false;
		return this;
	}

	// 1.12.2 renamed the old func_111238_b to isEnabled
	@Override
	public boolean isEnabled() {
		return canHover;
	}
}
