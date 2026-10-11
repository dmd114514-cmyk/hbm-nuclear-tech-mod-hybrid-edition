package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.inventory.container.ContainerCrateTungsten;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.storage.TileEntityCrateTungsten;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

/** The tungsten crate's GUI: swaps to a "hot" background while burning */
public class GUICrateTungsten extends GuiCrateBase {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/storage/gui_crate_tungsten.png");
	private static final ResourceLocation texture_hot = new ResourceLocation(RefStrings.MODID + ":textures/gui/storage/gui_crate_tungsten_hot.png");
	private final IInventory crate;

	public GUICrateTungsten(InventoryPlayer invPlayer, IInventory inv) {
		super(new ContainerCrateTungsten(invPlayer, inv));
		crate = inv;

		this.xSize = 176;
		this.ySize = 168;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String name = this.crate.hasCustomName() ? this.crate.getName() : I18n.format(this.crate.getName());

		this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 0xffffff);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0xffffff);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		boolean hot = crate instanceof TileEntityCrateTungsten
				&& ((TileEntityCrateTungsten) crate).getWorld() != null
				&& ((TileEntityCrateTungsten) crate).getWorld().getBlockState(((TileEntityCrateTungsten) crate).getPos()).getValue(BlockStorageCrate.HEAT);

		Minecraft.getMinecraft().getTextureManager().bindTexture(hot ? texture_hot : texture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}
