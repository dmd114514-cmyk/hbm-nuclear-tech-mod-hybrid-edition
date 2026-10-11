package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityCog;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * How the flywheel draws: a spinning cog that, once stuck, settles against the face it bit into and stops turning.
 *
 * The old version read raw datawatcher ordinals 10/11; this version's ordinals are auto-assigned by createKey,
 * so this goes through the entity's own getOrientation()/getMeta() — the render layer shouldn't know watcher ids.
 *
 * The model stays on the home-grown named-parts set (the Cog group in stirling.obj), fetched via ResourceManager.getModel,
 * parsing deferred to first draw; the texture picks between the iron and steel pair by meta, as before.
 * GL all GlStateManager; the old glRotated double-width call becomes the float version — the spin is a millisecond-scale modulo,
 * the precision loss is invisible.
 */
public class RenderCog extends Render<EntityCog> {

	private static final String MODEL = "models/machines/stirling.obj";

	private static final ResourceLocation TEX_IRON = new ResourceLocation(RefStrings.MODID, "textures/models/machines/stirling.png");
	private static final ResourceLocation TEX_STEEL = new ResourceLocation(RefStrings.MODID, "textures/models/machines/stirling_steel.png");

	public RenderCog(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCog cog, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);

		// orientation's low 6 bits are the face it bit into, converted to the matching facing; +6 means permanently stuck
		int orientation = cog.getOrientation();
		switch(orientation % 6) {
		case 3: GlStateManager.rotate(0, 0F, 1F, 0F); break;
		case 5: GlStateManager.rotate(90, 0F, 1F, 0F); break;
		case 2: GlStateManager.rotate(180, 0F, 1F, 0F); break;
		case 4: GlStateManager.rotate(270, 0F, 1F, 0F); break;
		}

		GlStateManager.translate(0, 0, -1);

		if(orientation < 6) {
			GlStateManager.rotate((float) (System.currentTimeMillis() % (360 * 3) / 3D), 0.0F, 0.0F, -1.0F);
		}

		GlStateManager.translate(0, -1.375, 0);

		this.bindEntityTexture(cog);
		IModelCustomNamed stirling = ResourceManager.getModel(MODEL);
		stirling.renderPart("Cog");

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCog cog) {
		return cog.getMeta() == 0 ? TEX_IRON : TEX_STEEL;
	}
}
