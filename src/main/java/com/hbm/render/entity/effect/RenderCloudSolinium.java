package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityCloudSolinium;
import com.hbm.main.ResourceManager;
import com.hbm.util.ColorUtil;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * How the solinium cloud draws: a cyan sphere swelling open, wrapped in three slightly larger same-color shells, colors split per byte via ColorUtil.
 *
 * Two deltas from the fusion cloud, copied faithfully: culling goes off first here (back on after the inner sphere), and the shells scale by 1.025 rather than 1.05.
 */
public class RenderCloudSolinium extends Render<EntityCloudSolinium> {

	private static final String MODEL = "models/sphere_new.obj";

	public RenderCloudSolinium(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCloudSolinium cloud, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture2D();

		int color = 0x27FFDA;

		float scale = cloud.age + partialTicks;
		GlStateManager.scale(scale, scale, scale);

		GlStateManager.color(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color));
		ResourceManager.getModel(MODEL).renderAll();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		GlStateManager.color(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color), 0.125F);

		GlStateManager.enableCull();

		double outerScale = 1.025;

		for(int i = 0; i < 3; i++) {
			GlStateManager.scale((float) outerScale, (float) outerScale, (float) outerScale);
			ResourceManager.getModel(MODEL).renderAll();
		}

		GlStateManager.color(1F, 1F, 1F, 1F);

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCloudSolinium cloud) {
		return null;
	}
}
