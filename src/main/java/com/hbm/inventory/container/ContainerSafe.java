package com.hbm.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/** The safe's container: 3×5 slots */
public class ContainerSafe extends ContainerCrateBase {

	public ContainerSafe(InventoryPlayer invPlayer, IInventory tedf) {
		super(invPlayer, tedf);

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 5; j++) {
				this.addSlotToContainer(new Slot(tedf, j + i * 5, 44 + j * 18, 18 + i * 18));
			}
		}

		this.playerInv(invPlayer, 8, 86, 144);
	}
}
