package com.hbm.render.entity.mob;

import com.hbm.lib.RefStrings;

import net.minecraft.client.renderer.entity.RenderChicken;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.util.ResourceLocation;

/**
 * The duck borrows the chicken's skeleton and tail-wag animation, one reskin.
 */
public class RenderDuck extends RenderChicken {

	private static final ResourceLocation DUCK_TEXTURE = new ResourceLocation(RefStrings.MODID + ":textures/entity/duck.png");

	public RenderDuck(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityChicken entity) {
		return DUCK_TEXTURE;
	}
}
