package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityBlockSpider;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelBlockSpider;

import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The block spider's renderer: one body texture; legs and the back block are all drawn inside the model.
 *
 * The old no-arg constructor (renderer built its own model, texture from a ResourceManager static) became
 * the house style of "constructor takes RenderManager, texture is a local constant"; shadow radius 1.0F unchanged.
 * The old getEntityTexture took a bare Entity and cast; RenderLiving is generic now, so that shell goes.
 */
public class RenderBlockSpider extends RenderLiving<EntityBlockSpider> {

	private static final ResourceLocation TEX = new ResourceLocation(RefStrings.MODID, "textures/entity/blockspider.png");

	public RenderBlockSpider(RenderManager renderManager) {
		super(renderManager, new ModelBlockSpider(), 1.0F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlockSpider entity) {
		return TEX;
	}
}
