package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityDummy;
import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The training dummy's look: the player's humanoid model in a solid-color skin.
 *
 * The old constructor's 1.0F was RenderBiped's scale; this version defaults it to 1.0, so it's no change at all and the parameter goes.
 */
public class RenderDummy extends RenderBiped<EntityDummy> {

	public RenderDummy(RenderManager renderManager) {
		super(renderManager, new ModelBiped(0.0F), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDummy entity) {
		return new ResourceLocation(RefStrings.MODID + ":textures/entity/dummy.png");
	}
}
