package com.hbm.inventory.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

/** The iron crate's container: 4×9 slots */
public class ContainerCrateIron extends ContainerCrateBase {

	public ContainerCrateIron(InventoryPlayer invPlayer, IInventory tedf) {
		super(invPlayer, tedf);

		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlotToContainer(new Slot(tedf, j + i * 9, 8 + j * 18, 18 + i * 18));
			}
		}

		this.playerInv(invPlayer, 8, 104, 162);
	}
}
