package com.hbm.inventory.container;

import com.hbm.items.tool.ItemPlasticBag.InventoryPlasticBag;

import net.minecraft.entity.player.InventoryPlayer;

/**
 * The plastic bag container: one cell on top, the player inventory below. Positions match the old version ((80,65) / 134 / 192).
 */
public class ContainerPlasticBag extends ContainerBase {

	public ContainerPlasticBag(InventoryPlayer invPlayer, InventoryPlasticBag bag) {
		super(invPlayer, bag);
		bag.openInventory(invPlayer.player);

		addSlots(bag, 0, 80, 65, 1, 1);
		playerInv(invPlayer, 8, 134, 192);
	}
}
