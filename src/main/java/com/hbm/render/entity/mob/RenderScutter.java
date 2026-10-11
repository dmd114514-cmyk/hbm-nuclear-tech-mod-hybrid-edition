package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityScutterfish;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelScutter;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The scutterfish's look: the model is the home-grown named-parts set (ModelScutter), texture still the old single one.
 *
 * The old constructor handed model and shadow to the caller (ClientProxy did new RenderScutter(new ModelScutter(), 0.3F));
 * this version's RenderLiving takes a RenderManager first and the renderer owns its model — registration shrinks to a factory reference.
 * Shadow radius 0.3F, same spot. Generic narrowed to the concrete type, getEntityTexture no longer casts from Entity.
 *
 * The texture is textures/entity/scutterfish.png, matching the old renderer;
 * the old ResourceManager also had a scutter_tex pointing at lob2.png — that belongs to some other part and has nothing to do with this fish.
 */
public class RenderScutter extends RenderLiving<EntityScutterfish> {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/entity/scutterfish.png");

	public RenderScutter(RenderManager renderManager) {
		super(renderManager, new ModelScutter(), 0.3F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityScutterfish entity) {
		return texture;
	}
}
