package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityMoonCow;
import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The moon cow's look: the body is the model's job; this only binds the texture.
 *
 * One delta: the old {@code RenderLiving} wasn't generic and its constructor wanted no {@code RenderManager};
 * this version wants both — the manager arrives at registration ({@code HbmEntityRenderers}),
 * and model plus shadow size were decided there too (as before: ModelMoonCow with 0.7F).
 */
public class RenderMoonCow extends RenderLiving<EntityMoonCow> {

	public static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID, "textures/entity/moon_cow.png");

	public RenderMoonCow(RenderManager renderManager, ModelBase model, float shadowSize) {
		super(renderManager, model, shadowSize);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMoonCow entity) {
		return TEXTURE;
	}
}
