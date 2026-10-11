package com.hbm.inventory.container;

import com.hbm.inventory.SlotPattern;
import com.hbm.inventory.SlotTakeOnly;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/** The mass storage's container: input / filter / output three cells; the filter is editable only while empty */
public class ContainerMassStorage extends ContainerBase {

	private final TileEntityMassStorage storage;

	public ContainerMassStorage(InventoryPlayer invPlayer, TileEntityMassStorage te) {
		super(invPlayer, te);

		this.storage = te;
		this.storage.openInventory(invPlayer.player);

		this.addSlotToContainer(new Slot(storage, 0, 61, 17));
		this.addSlotToContainer(new SlotPattern(storage.getItemHandler(), 1, 61, 53));
		this.addSlotToContainer(new SlotTakeOnly(storage.getItemHandler(), 2, 61, 89));

		playerInv(invPlayer, 8, 139, 197);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int index) {

		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		//top up the output first if needed, then run the normal merge
		if(index == 2 && slot != null && !slot.getHasStack()) {
			slot.putStack(storage.quickExtract());
		}

		if(slot != null && slot.getHasStack()) {

			ItemStack initial = slot.getStack();
			result = initial.copy();

			if(index == 0 || index == 2) {
				if(!this.mergeItemStack(initial, storage.getSizeInventory(), this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				//try the instant deposit first, falling back to normal slots
				if(!storage.quickInsert(initial) && !this.mergeItemStack(initial, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			}

			if(initial.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			slot.onTake(player, initial);
		}

		return result;
	}

	@Override
	public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

		if(index != 1) {
			return super.slotClick(index, button, mode, player);
		}

		Slot slot = this.getSlot(index);

		ItemStack ret = ItemStack.EMPTY;
		if(slot.getHasStack()) ret = slot.getStack().copy();

		//no filter swaps while the vat still holds anything
		if(storage.getStockpile() > 0) return ret;

		slot.putStack(player.inventory.getItemStack());

		return ret;
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return storage.isUsableByPlayer(player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.storage.closeInventory(player);
	}
}
