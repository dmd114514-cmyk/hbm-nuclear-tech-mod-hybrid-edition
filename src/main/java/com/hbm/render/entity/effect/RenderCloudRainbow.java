package com.hbm.render.entity.effect;

import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The rainbow cloud's drawing: a sphere model swelling frame by frame with age, a fresh random color every layer, stacking up one colorful blob.
 *
 * Three shape changes: the model moved from old static fields to ResourceManager.getModel (lazy, reloadable);
 * lighting and blending all through GlStateManager; colors swap glColor3ub's byte door for 0..1 floats (same number divided by 255).
 * This renderer manages its own texture binding, so getEntityTexture keeps returning null.
 */
public class RenderCloudRainbow extends Render<EntityCloudFleijaRainbow> {

	private static final String MODEL = "models/Sphere.obj";

	public RenderCloudRainbow(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCloudFleijaRainbow cloud, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.enableCull();
		GlStateManager.disableTexture2D();

		GlStateManager.scale(cloud.age, cloud.age, cloud.age);

		this.randomColor(cloud);

		GlStateManager.scale(0.5F, 0.5F, 0.5F);
		ResourceManager.getModel(MODEL).renderAll();
		GlStateManager.scale(1 / 0.5F, 1 / 0.5F, 1 / 0.5F);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

		for(float i = 0.6F; i <= 1F; i += 0.1F) {

			this.randomColor(cloud);

			GlStateManager.scale(i, i, i);
			ResourceManager.getModel(MODEL).renderAll();
			GlStateManager.scale(1 / i, 1 / i, 1 / i);
		}

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.disableCull();

		GlStateManager.popMatrix();
	}

	/** The old version did glColor3ub(rand.nextInt(0x100) cast to byte), same value range as 0..255 then normalized */
	private void randomColor(EntityCloudFleijaRainbow cloud) {
		GlStateManager.color(cloud.getEntityWorld().rand.nextInt(0x100) / 255F,
				cloud.getEntityWorld().rand.nextInt(0x100) / 255F,
				cloud.getEntityWorld().rand.nextInt(0x100) / 255F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCloudFleijaRainbow cloud) {
		return null;
	}
}
