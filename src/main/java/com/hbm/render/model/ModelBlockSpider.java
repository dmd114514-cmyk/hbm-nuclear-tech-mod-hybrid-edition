package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityBlockSpider;
import com.hbm.main.ResourceManager;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * The block spider: eight legs from its own OBJ (blockspider.obj, part names Leg1…Leg8, checked line by line against the obj's o lines),
 * with the block on its back fetched and drawn live from what the entity synced over.
 *
 * Four shape deltas this version:
 *   · the old RenderBlocks.getInstance().renderBlockAsItem(block, meta, brightness) doesn't exist here,
 *     replaced by BlockRendererDispatcher.renderBlockBrightness(IBlockState, float). The old door centered the cube
 *     on the origin; this one draws from a 0..1 origin ⇒ add one -0.5 translation so the block's center still lands where it used to.
 *   · brightness was getBrightness(scaleFactor) before; this version's getBrightness() takes no args.
 *   · the old version read dataWatcher's raw slots 12/13; this version goes through the entity's typed getter pair (slot numbers are retired).
 *   · the old version bound TextureMap.locationBlocksTexture for the block atlas; that constant isn't in these mappings,
 *     so it binds getTextureMapBlocks().getGlTextureId() and hands the previous binding back when done — the renderer still needs
 *     its own body texture afterward; skip the return and the legs come out wearing the block atlas's colors.
 *
 * The GL11 calls all move to GlStateManager, so this version's state cache doesn't get bypassed.
 */
public class ModelBlockSpider extends ModelBase {

	private static final String MODEL = "models/mobs/blockspider.obj";

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

		EntityBlockSpider spider = (EntityBlockSpider) entity;
		Block block = spider.getBlockType();

		if(block == null) {
			return;
		}

		float rot = -(MathHelper.cos(limbSwing * 0.6662F * 2.0F + 0.0F) * 0.4F) * limbSwingAmount * 57.3F;

		GlStateManager.pushMatrix();

		GlStateManager.rotate(90, 0, -1, 0);
		GlStateManager.rotate(180, 0, 0, 1);
		GlStateManager.translate(0, -1.5F, 0);

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, rot * 0.005, 0);
		GlStateManager.rotate(rot, 0, 1, 0);
		ResourceManager.getModel(MODEL).renderPart("Leg1");
		ResourceManager.getModel(MODEL).renderPart("Leg3");
		ResourceManager.getModel(MODEL).renderPart("Leg5");
		ResourceManager.getModel(MODEL).renderPart("Leg7");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0, rot * -0.005, 0);
		GlStateManager.rotate(rot, 0, -1, 0);
		ResourceManager.getModel(MODEL).renderPart("Leg2");
		ResourceManager.getModel(MODEL).renderPart("Leg4");
		ResourceManager.getModel(MODEL).renderPart("Leg6");
		ResourceManager.getModel(MODEL).renderPart("Leg8");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		int previousTexture = GlStateManager.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GlStateManager.bindTexture(Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());
		GlStateManager.translate(0, 0.75, 0);
		GlStateManager.translate(-0.5, -0.5, -0.5);
		Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlockBrightness(
				block.getStateFromMeta(spider.getBlockMeta()), spider.getBrightness());
		GlStateManager.bindTexture(previousTexture);
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}
}
