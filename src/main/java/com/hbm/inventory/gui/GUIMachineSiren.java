package com.hbm.inventory.gui;

import com.hbm.inventory.container.ContainerMachineSiren;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineSiren;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GUIMachineSiren extends GuiContainer {
	
	private static ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gui_siren.png");
	private TileEntityMachineSiren siren;

	public GUIMachineSiren(InventoryPlayer invPlayer, TileEntityMachineSiren tedf) {
		super(new ContainerMachineSiren(invPlayer, tedf));
		siren = tedf;
		
		this.xSize = 176;
		this.ySize = 166;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.siren.hasCustomName() ? this.siren.getName() : I18n.format(this.siren.getName());
		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
		
		//the tape info goes here; that's how the old version did it
		if(!siren.getCurrentType().name().equals(TrackType.NULL.name())) {
			int color = siren.getCurrentType().getColor();
			this.fontRenderer.drawString(siren.getCurrentType().getTrackTitle(), 46, 28, color);
			this.fontRenderer.drawString("Type: " + siren.getCurrentType().getType().name(), 46, 40, color);
			this.fontRenderer.drawString("Volume: " + siren.getCurrentType().getVolume(), 46, 52, color);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}
