package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * How the fusion cloud draws: a sphere swelling with its lifetime, wrapped in three same-color shells, finished with a gray shock ring.
 *
 * The model moved from the old static field to ResourceManager.getModel (lazy, reloadable),
 * GL goes through GlStateManager; texturing is off for the whole pass, so getEntityTexture keeps returning null.
 */
public class RenderCloudFleija extends Render<EntityCloudFleija> {

	private static final String MODEL = "models/sphere_new.obj";

	public RenderCloudFleija(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCloudFleija cloud, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture2D();

		double baseScale = (cloud.age + partialTicks) * 2;
		double ageScale = baseScale / cloud.getMaxAge();

		GlStateManager.pushMatrix();

		double scale = ageScale * 1.2;
		if(scale > 1) scale = Math.max(1 - (scale - 1) * 5, 0);
		scale *= 2 * baseScale;
		GlStateManager.scale((float) scale, (float) scale, (float) scale);

		GlStateManager.color(0F, 1F, 1F);
		ResourceManager.getModel(MODEL).renderAll();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

		GlStateManager.color(0F, 0.125F, 0.125F);
		double outerScale = 1.05;

		for(int i = 0; i < 3; i++) {
			GlStateManager.scale((float) outerScale, (float) outerScale, (float) outerScale);
			ResourceManager.getModel(MODEL).renderAll();
		}

		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();

		double shockwave = 5 * baseScale;
		GlStateManager.scale((float) shockwave, (float) shockwave, (float) shockwave);
		float shockTint = (1F - (float) ageScale) * 0.75F;
		GlStateManager.color(shockTint, shockTint, shockTint);
		ResourceManager.getModel(MODEL).renderAll();

		GlStateManager.popMatrix();

		GlStateManager.disableBlend();
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCloudFleija cloud) {
		return null;
	}
}
