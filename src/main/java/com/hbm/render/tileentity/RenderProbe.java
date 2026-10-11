package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.ResourceLocation;

/**
 * The probe renderer: proving that "TESR drawing OBJ in the world" chain works.
 *
 * Bound to a vanilla enchanting table (no mod content involved; drop one in creative and look), drawing an industrial boiler,
 * turning a little every frame by wall clock — if the picture moves, the TESR really is running per frame and not baked dead.
 * Delete wholesale once the real machine TESRs start landing.
 */
public class RenderProbe extends RenderBase<TileEntityEnchantmentTable> {

	private IModelCustomNamed model;

	@Override
	protected void renderTileEntityAt(TileEntityEnchantmentTable te, float partialTicks) {

		// Lazy parse: resources are touched on first draw; startup reads not one OBJ
		if(model == null)
			model = ResourceManager.getModel("models/machines/boiler.obj");

		GL11.glRotated((System.currentTimeMillis() / 10D) % 360D, 0D, 1D, 0D);
		bindTexture(new ResourceLocation("hbm", "textures/models/machines/boiler.png"));
		model.renderAll();
	}
}
