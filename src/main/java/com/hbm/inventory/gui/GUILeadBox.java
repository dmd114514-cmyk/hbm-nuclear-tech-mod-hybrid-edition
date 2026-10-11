package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerLeadBox;
import com.hbm.items.tool.ItemLeadBox.InventoryLeadBox;
import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * The lead box GUI: one background + the item name on top. Custom names win (same rule as the crates).
 */
public class GUILeadBox extends GuiContainer {

	private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_containment.png");
	private final InventoryLeadBox inventory;

	public GUILeadBox(InventoryPlayer invPlayer, InventoryLeadBox box) {
		super(new ContainerLeadBox(invPlayer, box));
		this.inventory = box;

		this.xSize = 176;
		this.ySize = 186;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {

		String name = I18n.format(this.inventory.getName());

		if(this.inventory.hasCustomName()) {
			name = this.inventory.target.getDisplayName();
		}

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		net.minecraft.client.renderer.GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
}
