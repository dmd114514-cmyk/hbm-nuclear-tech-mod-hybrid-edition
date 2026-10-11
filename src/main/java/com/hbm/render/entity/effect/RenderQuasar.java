package com.hbm.render.entity.effect;

import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The quasar's drawing: the same posing as the plain black hole, only the disk texture swaps and each disk layer's colorway becomes "bluer the farther out".
 *
 * Three overrides copied from the old version: disk texture, per-layer colorway (setColorFromIteration), layer count (steps still 15).
 */
public class RenderQuasar extends RenderBlackHole {

	protected ResourceLocation quasar = new ResourceLocation(RefStrings.MODID, "textures/entity/bholeD.png");

	public RenderQuasar(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityBlackHole entity, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();

		float size = entity.getSize();

		GlStateManager.scale(size, size, size);

		this.bindTexture(hole);
		ResourceManager.getModel(MODEL).renderAll();

		this.renderDisc(entity, partialTicks);
		this.renderJets(entity, partialTicks);

		GlStateManager.enableCull();
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation discTex() {
		return this.quasar;
	}

	@Override
	protected void setColorFromIteration(BufferBuilder buffer, int iteration, float alpha) {
		float r = 1.0F;
		float g = (float) Math.pow(iteration / 15F, 2);
		float b = (float) Math.pow(iteration / 15F, 2);

		buffer.color(r, g, b, alpha);
	}

	@Override
	protected int steps() {
		return 15;
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlackHole entity) {
		return super.getEntityTexture(entity);
	}
}
