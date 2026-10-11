package com.hbm.inventory.container;

import com.hbm.items.tool.ItemToolBox.InventoryToolBox;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

/**
 * The toolbox container: a 3×8 in-box grid on top, the player inventory below as usual.
 * Positions match the old version (box (17,49) / inventory 129 / hotbar 187).
 */
public class ContainerToolBox extends ContainerBase {

	private final InventoryToolBox box;

	public ContainerToolBox(InventoryPlayer invPlayer, InventoryToolBox box) {
		super(invPlayer, box);
		this.box = box;
		box.openInventory(invPlayer.player);

		addSlots(box, 0, 17, 49, 3, 8);
		playerInv(invPlayer, 8, 129, 187);
	}

	/**
	 * The currently-open held slot is untouchable: swapping it by hotkey or clicking it moves the whole box out of your hand,
	 * while the GUI still hangs off the original contents view — the next NBT write would land in a different toolbox.
	 */
	@Override
	public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {

		if(mode == ClickType.SWAP && button == player.inventory.currentItem) return ItemStack.EMPTY;
		if(index == box.getSizeInventory() + 27 + player.inventory.currentItem) return ItemStack.EMPTY;
		return super.slotClick(index, button, mode, player);
	}

	@Override
	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		this.box.closeInventory(player);
	}
}
