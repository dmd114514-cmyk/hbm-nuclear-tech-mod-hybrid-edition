package com.hbm.inventory.gui;

import java.io.IOException;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

/** The crate GUI base: clicking a slot first disables touch mode, so a tap doesn't become a right-click on mobile */
public abstract class GuiCrateBase extends GuiContainer {

	public GuiCrateBase(Container container) {
		super(container);
	}

	@Override
	protected void mouseClicked(int x, int y, int button) throws IOException {
		boolean touchScreen = this.mc.gameSettings.touchscreen;
		this.mc.gameSettings.touchscreen = false;
		super.mouseClicked(x, y, button);
		this.mc.gameSettings.touchscreen = touchScreen;
	}
}
