package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerToolBox;
import com.hbm.items.tool.ItemToolBox.InventoryToolBox;
import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * The toolbox GUI: the box name draws above the in-box grid (y=37), with the usual "Inventory" label below.
 * Custom names win, same rule as the crates.
 */
public class GUIToolBox extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_toolbox.png");
	private final InventoryToolBox inventory;

	public GUIToolBox(InventoryPlayer invPlayer, InventoryToolBox box) {
		super(new ContainerToolBox(invPlayer, box));
		this.inventory = box;

		this.xSize = 176;
		this.ySize = 211;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

		String name = I18n.format(this.inventory.getName());

		if(this.inventory.hasCustomName()) {
			name = this.inventory.target.getDisplayName();
		}

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 37, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
}
