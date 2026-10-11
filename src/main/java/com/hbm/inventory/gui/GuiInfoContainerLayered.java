package com.hbm.inventory.gui;

import com.hbm.inventory.SlotLayer;
import com.hbm.network.NetworkHandler;
import com.hbm.network.packet.GuiLayerPacket;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

/** The layered GUI: one screen showing different slots per layer, switching the server's container along with it */
public abstract class GuiInfoContainerLayered extends GuiInfoContainer {

	protected int currentLayer = 0;

	public GuiInfoContainerLayered(Container container) {
		super(container);
	}

	public void setLayer(int layer) {
		currentLayer = layer;

		for(Slot slot : inventorySlots.inventorySlots) {
			if(slot instanceof SlotLayer) {
				((SlotLayer) slot).setLayer(layer);
			}
		}

		NetworkHandler.sendToServer(new GuiLayerPacket(layer));
	}

	public int getLayer() {
		return currentLayer;
	}
}
