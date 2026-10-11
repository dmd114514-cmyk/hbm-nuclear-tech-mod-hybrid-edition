package com.hbm.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * An ItemStackHandler with a "contents changed" callback.
 * Machines using it must wire the callback to markDirty(); otherwise inventory changes don't save and things vanish on restart.
 *
 * With outputOnly = true nothing can be inserted from outside (typical output slot behavior), extraction still works.
 */
public class HbmItemHandler extends ItemStackHandler {

	private final Runnable onChanged;
	private final boolean outputOnly;

	public HbmItemHandler(int size, Runnable onChanged) {
		this(size, onChanged, false);
	}

	public HbmItemHandler(int size, Runnable onChanged, boolean outputOnly) {
		super(size);
		this.onChanged = onChanged;
		this.outputOnly = outputOnly;
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(onChanged != null) onChanged.run();
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if(outputOnly) return stack;
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if(outputOnly) return ItemStack.EMPTY;
		return super.extractItem(slot, amount, simulate);
	}
}
