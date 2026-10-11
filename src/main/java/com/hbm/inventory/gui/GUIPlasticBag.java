package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerPlasticBag;
import com.hbm.items.tool.ItemPlasticBag.InventoryPlasticBag;
import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * The plastic bag GUI: one background, no box name (the old version only drew the "Inventory" line).
 */
public class GUIPlasticBag extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID + ":textures/gui/storage/gui_plastic_bag.png");

	public GUIPlasticBag(InventoryPlayer invPlayer, InventoryPlasticBag bag) {
		super(new ContainerPlasticBag(invPlayer, bag));

		this.xSize = 176;
		this.ySize = 216;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
}
