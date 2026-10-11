package com.hbm.render.entity.effect;

import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * How the EMP shock ring draws: a ring flattened horizontally, horizontal scale following age, vertical untouched.
 *
 * Model and texture moved from the old static fields to ResourceManager.getModel and a constant on this class;
 * GL via GlStateManager. Same as the old version: the ring binds its own texture, but getEntityTexture still returns null.
 */
public class RenderEMPBlast extends Render<EntityEMPBlast> {

	private static final String MODEL = "models/Ring.obj";
	private static final ResourceLocation TEX = new ResourceLocation(RefStrings.MODID, "textures/models/EMPBlast.png");

	public RenderEMPBlast(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityEMPBlast blast, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();
		GlStateManager.scale(blast.scale, 1F, blast.scale);

		this.bindTexture(TEX);
		ResourceManager.getModel(MODEL).renderAll();

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityEMPBlast blast) {
		return null;
	}
}
