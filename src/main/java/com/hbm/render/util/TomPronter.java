package com.hbm.render.util;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HmfController;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

/**
 * The Tom (lunar rock bomb) draw door: the body drawn once at a hundred times size, then twenty flame shells stacked on, each rotated by a random angle
 * and squashed a little horizontally — producing that "flame tongue wrapping the body" look.
 *
 * Shape changes: models moved from old static fields to ResourceManager.getModel (lazy + cached),
 * GL through GlStateManager except texture management; HmfController.setMod/resetMod as before.
 * One honest note: the old models loaded with noSmooth, and this version goes through getModel (smooth shading) uniformly,
 * so the faceted look differs slightly — restoring it means bypassing the cache with a hand-made loader, not worth it; noted.
 */
public class TomPronter {

	private static final String MODEL_MAIN = "models/weapons/tom_main.obj";
	private static final String MODEL_FLAME = "models/weapons/tom_flame.hmf";

	private static final ResourceLocation TEX_MAIN = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/tom_main.png");
	private static final ResourceLocation TEX_FLAME = new ResourceLocation(RefStrings.MODID, "textures/models/weapons/tom_flame.png");

	public static void prontTom() {

		GlStateManager.pushMatrix();

		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.scale(100F, 100F, 100F);

		TextureManager tex = Minecraft.getMinecraft().getTextureManager();

		tex.bindTexture(TEX_MAIN);
		ResourceManager.getModel(MODEL_MAIN).renderAll();

		HmfController.setMod(50000D, 2500D);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		GlStateManager.disableAlpha();

		float rot = -System.currentTimeMillis() / 10 % 360;
		GlStateManager.scale(0.8F, 5F, 0.8F);

		Random rand = new Random(0);

		for(int i = 0; i < 20; i++) {
			tex.bindTexture(TEX_FLAME);

			int r = rand.nextInt(90);

			GlStateManager.rotate(rot + r, 0F, 1F, 0F);
			ResourceManager.getModel(MODEL_FLAME).renderAll();
			GlStateManager.rotate(rot, 0F, -1F, 0F);

			GlStateManager.scale(-1.015F, 0.9F, 1.015F);
		}

		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableCull();
		GlStateManager.enableLighting();
		HmfController.resetMod();

		GlStateManager.popMatrix();
	}
}
