package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntitySawblade;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * How the sawblade draws: same pose as RenderCog (rotates by the face it bit into, stops once stuck),
 * differing only in model (sawmill.obj's Blade group), a single texture, and the spin period.
 *
 * Orientation and texture id both come from the entity's own getters, no more datawatcher ordinals — ordinals are auto-assigned in this version.
 * The old version even kept the parameter name cog, borrowed from the flywheel; renamed to the actual type here for readability.
 */
public class RenderSawblade extends Render<EntitySawblade> {

	private static final String MODEL = "models/machines/sawmill.obj";

	private static final ResourceLocation TEX = new ResourceLocation(RefStrings.MODID, "textures/models/machines/sawmill.png");

	public RenderSawblade(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntitySawblade saw, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		int orientation = saw.getOrientation();
		switch(orientation % 6) {
		case 3: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 2: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		}

		GlStateManager.translate(0, 0, -1);

		if(orientation < 6) {
			GlStateManager.rotate((float) (System.currentTimeMillis() % (360 * 5) / 3D), 0.0F, 0.0F, -1.0F);
		}

		GlStateManager.translate(0, -1.375, 0);

		this.bindEntityTexture(saw);
		IModelCustomNamed sawmill = ResourceManager.getModel(MODEL);
		sawmill.renderPart("Blade");

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntitySawblade saw) {
		return TEX;
	}
}
