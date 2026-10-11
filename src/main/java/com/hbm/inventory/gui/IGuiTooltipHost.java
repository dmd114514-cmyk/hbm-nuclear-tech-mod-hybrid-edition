package com.hbm.inventory.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;

/**
 * The host capabilities tooltips need.
 *
 * Why not take GuiInfoContainer directly: a tank's renderTankInfo needs only "font, item renderer, width/height" — four things —
 * while GuiInfoContainer back-depends on ContainerBase/packets/ModItems/upgrade interfaces. Mutual references make a cycle,
 * and neither can land first. So the need narrows to a small interface; GuiInfoContainer implements it later,
 * caller behavior unchanged.
 */
public interface IGuiTooltipHost {

	public FontRenderer getFontRenderer();

	public RenderItem getItemRenderer();

	public int getScaledWidth();

	public int getScaledHeight();
}
