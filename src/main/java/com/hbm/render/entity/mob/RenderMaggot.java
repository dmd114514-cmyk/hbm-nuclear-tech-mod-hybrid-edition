package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityParasiteMaggot;
import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelSilverfish;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The maggot's look: borrows the vanilla silverfish model wholesale, one reskin.
 *
 * The old constructor was `super(new ModelSilverfish(), 0.3F)`; this version's RenderLiving wants a RenderManager first,
 * supplied by the factory at registration; 0.3F is the shadow radius, same spot as before.
 * The generic narrows to the concrete type, so getDeathMaxRotation / getEntityTexture no longer cast down from Entity.
 */
public class RenderMaggot extends RenderLiving<EntityParasiteMaggot> {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/entity/parasite_maggot.png");

	public RenderMaggot(RenderManager renderManager) {
		super(renderManager, new ModelSilverfish(), 0.3F);
	}

	// Bugs lie flat; on death they flip belly-up
	@Override
	protected float getDeathMaxRotation(EntityParasiteMaggot entity) {
		return 180.0F;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityParasiteMaggot entity) {
		return texture;
	}
}
