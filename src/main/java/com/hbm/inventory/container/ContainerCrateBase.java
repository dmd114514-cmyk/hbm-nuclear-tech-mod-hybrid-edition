package com.hbm.inventory.container;

import com.hbm.inventory.SlotNonRetarded;
import com.hbm.items.ItemInventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

/**
 * The crate/safe container base: player inventory as usual, but "the held slot currently open" is untouchable —
 * otherwise the player can move the open crate out of their hand and the GUI becomes an orphan.
 */
public class ContainerCrateBase extends ContainerBase {

	public ContainerCrateBase(InventoryPlayer invPlayer, IInventory tedf) {
		super(invPlayer, tedf);
		//this version's openInventory wants a player; taken from InventoryPlayer
		tile.openInventory(invPlayer.player);
	}

	@Override
	public void playerInv(InventoryPlayer invPlayer, int playerInvX, int playerInvY, int playerHotbarY) {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new SlotNonRetarded(invPlayer, j + i * 9 + 9, playerInvX + j * 18, playerInvY + i * 18));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlotToContainer(
				(invPlayer.currentItem == i && this.tile instanceof ItemInventory) ?
				new SlotPlayerCrateLocked(invPlayer, i, playerInvX + i * 18, playerHotbarY) :
				new SlotNonRetarded(invPlayer, i, playerInvX + i * 18, playerHotbarY)
			);
		}
	}

	@Override
	public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

		//when the held item IS the currently-open crate (the GUI hangs on the inventory, not the world), moving it is forbidden
		ItemStack held = player.inventory.getStackInSlot(player.inventory.currentItem);

		if(!held.isEmpty() && this.tile instanceof ItemInventory && !(this.tile instanceof net.minecraft.tileentity.TileEntity)) {

			if(mode == ClickType.SWAP && button == player.inventory.currentItem) {
				return ItemStack.EMPTY;
			}
			if(index == player.inventory.currentItem + 27 + this.tile.getSizeInventory()) {
				return ItemStack.EMPTY;
			}
		}

		return super.slotClick(index, button, mode, player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		tile.closeInventory(player);
	}

	/** This cell: hands off entirely — nothing taken, nothing placed */
	public class SlotPlayerCrateLocked extends SlotNonRetarded {

		public SlotPlayerCrateLocked(IInventory inventory, int id, int x, int y) {
			super(inventory, id, x, y);
		}

		@Override
		public boolean canTakeStack(EntityPlayer player) {
			return false;
		}

		@Override
		public boolean isItemValid(ItemStack item) {
			return false;
		}
	}
}
