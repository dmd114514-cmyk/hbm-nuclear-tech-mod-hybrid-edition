package com.hbm.inventory.container;

import com.hbm.inventory.SlotNonRetarded;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * The common base for machine/rolling-stock container GUIs: "which cell, how the player inventory lays out, how shift-drags merge"
 * — everything every container writes once — collected in one place.
 *
 * Two old sugar methods deliberately not ported: addOutputSlots wants SlotCraftingOutput (bound to AchievementHandler,
 * whose achievement system is a separate line here), and addTakeOnlySlots wants the IInventory-flavored SlotTakeOnly (this tree's
 * goes §16 IItemHandler). Add them when those classes have this-version shapes; no half-right signatures here.
 */
public class ContainerBase extends Container {

	public IInventory tile;

	public ContainerBase(InventoryPlayer invPlayer, IInventory tedf) {
		tile = tedf;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tile.isUsableByPlayer(player);
	}

	/** The reworked merge logic: honors both the stack limit and whether the slot takes the item at all */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {

		Slot slot = inventorySlots.get(index);
		if(slot == null || !slot.getHasStack())
			return ItemStack.EMPTY;

		ItemStack slotStack = slot.getStack();
		ItemStack original = slotStack.copy();

		if(index < tile.getSizeInventory()) {
			if(!mergeItemStack(slotStack, tile.getSizeInventory(), inventorySlots.size(), true))
				return ItemStack.EMPTY;
		} else if(!mergeItemStack(slotStack, 0, tile.getSizeInventory(), false)) {
			return ItemStack.EMPTY;
		}

		if(slotStack.getCount() == 0)
			slot.putStack(ItemStack.EMPTY);
		else
			slot.onSlotChanged();

		slot.onTake(player, slotStack);
		return original;
	}

	/** The standard player inventory: horizontal default 8, hotbar 58 below it */
	public void playerInv(InventoryPlayer invPlayer, int playerInvY) {
		playerInv(invPlayer, 8, playerInvY, playerInvY + 58);
	}

	public void playerInv(InventoryPlayer invPlayer, int playerInvX, int playerInvY) {
		playerInv(invPlayer, playerInvX, playerInvY, playerInvY + 58);
	}

	public void playerInv(InventoryPlayer invPlayer, int playerInvX, int playerInvY, int playerHotbarY) {

		for(int row = 0; row < 3; row++)
			for(int col = 0; col < 9; col++)
				addSlotToContainer(new SlotNonRetarded(invPlayer, col + row * 9 + 9, playerInvX + col * 18, playerInvY + row * 18));

		for(int i = 0; i < 9; i++)
			addSlotToContainer(new SlotNonRetarded(invPlayer, i, playerInvX + i * 18, playerHotbarY));
	}

	/** Lays one regular slot sheet at a time; from is which inventory slot to start pairing at */
	public void addSlots(IInventory inv, int from, int x, int y, int rows, int cols) {
		addSlots(inv, from, x, y, rows, cols, 18);
	}

	public void addSlots(IInventory inv, int from, int x, int y, int rows, int cols, int slotSize) {

		for(int row = 0; row < rows; row++)
			for(int col = 0; col < cols; col++)
				addSlotToContainer(new SlotNonRetarded(inv, col + row * cols + from, x + col * slotSize, y + row * slotSize));
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int fromInclusive, int toExclusive, boolean toPlayerInventory) {

		boolean didMerge = false;
		int targetIndex = toPlayerInventory ? toExclusive - 1 : fromInclusive;

		int maxStackSize = stack.getMaxStackSize();
		if(!toPlayerInventory)
			maxStackSize = Math.min(maxStackSize, tile.getInventoryStackLimit());

		if(stack.isStackable()) {
			while(stack.getCount() > 0 && (toPlayerInventory ? targetIndex >= fromInclusive : targetIndex < toExclusive)) {

				Slot targetSlot = getSlot(targetIndex);
				ItemStack targetStack = targetSlot.getStack();

				if(!targetStack.isEmpty() && targetStack.getItem() == stack.getItem()
						&& (!stack.getHasSubtypes() || stack.getItemDamage() == targetStack.getItemDamage())
						&& ItemStack.areItemStackTagsEqual(stack, targetStack) && targetSlot.isItemValid(stack)) {

					int total = targetStack.getCount() + stack.getCount();

					if(total <= maxStackSize) {
						stack.setCount(0);
						targetStack.setCount(total);
						targetSlot.onSlotChanged();
						didMerge = true;
					} else if(targetStack.getCount() < maxStackSize) {
						stack.shrink(maxStackSize - targetStack.getCount());
						targetStack.setCount(maxStackSize);
						targetSlot.onSlotChanged();
						didMerge = true;
					}
				}

				targetIndex += toPlayerInventory ? -1 : 1;
			}
		}

		if(stack.getCount() > 0) {
			targetIndex = toPlayerInventory ? toExclusive - 1 : fromInclusive;

			while(toPlayerInventory ? targetIndex >= fromInclusive : targetIndex < toExclusive) {

				Slot targetSlot = getSlot(targetIndex);
				ItemStack targetStack = targetSlot.getStack();

				if(targetStack.isEmpty() && targetSlot.isItemValid(stack)) {

					if(stack.getCount() <= maxStackSize) {
						targetSlot.putStack(stack.copy());
						targetSlot.onSlotChanged();
						stack.setCount(0);
						didMerge = true;
						break;
					}

					ItemStack newStack = stack.copy();
					newStack.setCount(maxStackSize);
					targetSlot.putStack(newStack);
					targetSlot.onSlotChanged();
					stack.shrink(maxStackSize);
					didMerge = true;
				}

				targetIndex += toPlayerInventory ? -1 : 1;
			}
		}

		return didMerge;
	}
}
