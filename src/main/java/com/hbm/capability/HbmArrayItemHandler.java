package com.hbm.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/**
 * An inventory capability backed by an external ItemStack[].
 *
 * Why it exists: tons of old content-layer code reads and writes `ItemStack[] slots` directly,
 * and swapping in ItemStackHandler's internal list means a full rewrite. This capability hugs that array,
 * both views operate on the same data, neither side holds more truth than the other.
 *
 * Same contract as ItemStackHandler: insertItem returns the remainder that didn't fit,
 * extractItem returns what was actually taken; never produces new ItemStack instances unless truly necessary.
 */
public class HbmArrayItemHandler implements IItemHandler {

	private final ItemStack[] slots;
	private final Runnable onChanged;
	private final boolean outputOnly;

	public HbmArrayItemHandler(ItemStack[] slots, Runnable onChanged) {
		this(slots, onChanged, false);
	}

	public HbmArrayItemHandler(ItemStack[] slots, Runnable onChanged, boolean outputOnly) {
		this.slots = slots;
		this.onChanged = onChanged;
		this.outputOnly = outputOnly;
	}

	@Override
	public int getSlots() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slots[slot] == null ? ItemStack.EMPTY : slots[slot];
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(outputOnly || stack.isEmpty()) return stack;

		ItemStack current = slots[slot];

		int limit = Math.min(getSlotLimit(slot), stack.getMaxStackSize());

		if(current == null || current.isEmpty()) {
			int accept = Math.min(limit, stack.getCount());
			if(!simulate) {
				slots[slot] = stack.copy();
				slots[slot].setCount(accept);
				changed();
			}
			return accept >= stack.getCount() ? ItemStack.EMPTY : copyWithCount(stack, stack.getCount() - accept);
		}

		// Only merges with the same item
		if(!current.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(current, stack)) return stack;

		int room = limit - current.getCount();
		if(room <= 0) return stack;

		int accept = Math.min(room, stack.getCount());
		if(!simulate) {
			current.grow(accept);
			changed();
		}
		return accept >= stack.getCount() ? ItemStack.EMPTY : copyWithCount(stack, stack.getCount() - accept);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if(amount <= 0) return ItemStack.EMPTY;

		ItemStack current = slots[slot];
		if(current == null || current.isEmpty()) return ItemStack.EMPTY;

		int take = Math.min(amount, current.getCount());
		if(take <= 0) return ItemStack.EMPTY;

		ItemStack out = copyWithCount(current, take);
		if(!simulate) {
			current.shrink(take);
			if(current.isEmpty()) slots[slot] = ItemStack.EMPTY;
			changed();
		}
		return out;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	private static ItemStack copyWithCount(ItemStack stack, int count) {
		ItemStack copy = stack.copy();
		copy.setCount(count);
		return copy;
	}

	private void changed() {
		if(onChanged != null) onChanged.run();
	}
}
