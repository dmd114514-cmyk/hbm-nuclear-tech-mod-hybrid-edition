package com.hbm.inventory.container;

import com.hbm.items.tool.ItemLeadBox.InventoryLeadBox;

import net.minecraft.entity.player.InventoryPlayer;

/**
 * The lead box container: a 4×5 in-box grid (one item per cell) on top, the player inventory below as usual.
 * "the currently-open held slot is untouchable" is ContainerBase's job; this only places things.
 */
public class ContainerLeadBox extends ContainerBase {

	public ContainerLeadBox(InventoryPlayer invPlayer, InventoryLeadBox box) {
		super(invPlayer, box);
		box.openInventory(invPlayer.player);

		addSlots(box, 0, 43, 18, 4, 5);
		playerInv(invPlayer, 8, 104, 162);
	}
}
