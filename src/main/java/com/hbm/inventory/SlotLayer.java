package com.hbm.inventory;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SlotLayer extends SlotItemHandler {

	private int originalYPosition;
	private int layer;
	private boolean isVisible = true;

	/** A slot that can hide per layer */
	public SlotLayer(IItemHandler handler, int id, int x, int y, int layer) {
		super(handler, id, x, y);
		this.layer = layer;
		this.originalYPosition = y;

		if(layer != 0) hide();
	}

	public boolean isVisible() {
		return this.isVisible;
	}

	public void setLayer(int layer) {
		if(this.layer == layer) {
			show();
		} else {
			hide();
		}
	}

	private void hide() {
		isVisible = false;
		yPos = 9999;
	}

	private void show() {
		isVisible = true;
		yPos = originalYPosition;
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return isVisible;
	}

}
