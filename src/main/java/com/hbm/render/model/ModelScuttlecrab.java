package com.hbm.render.model;

import com.hbm.main.ResourceManager;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * The scuttle crab's shape: body shell + two eyestalks (following pitch/yaw) + two claws + four legs per side, legs swinging on staggered sine phases.
 *
 * The model is OBJ named parts (models/mobs/scuttlecrab.obj: Body/EyeL/EyeR/ClawL/ClawR/LegL1..4/LegR1..4,
 * all thirteen parts in use); the old version used static asVBO fields, this version fetches through ResourceManager.getModel (cache inside);
 * GL through GlStateManager. The sway phase gaps (those 0.4/0.8/1.2/1.6 π fractions), the left/right sign flip,
 * and the opening rotate(180, 0, 0, 1) + translate(0, -1.5, 0) are all copied as-is — change them and the crab crawls sideways or flips over.
 */
public class ModelScuttlecrab extends ModelBase {

	private static final String MODEL = "models/mobs/scuttlecrab.obj";

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.pushMatrix();

		double cy0 = Math.sin(limbSwing * 6 % (Math.PI * 2));
		double cy1 = Math.sin(limbSwing * 6 % (Math.PI * 2) - Math.PI * 0.4);
		double cy2 = Math.sin(limbSwing * 6 % (Math.PI * 2) - Math.PI * 0.8);
		double cy3 = Math.sin(limbSwing * 6 % (Math.PI * 2) - Math.PI * 1.2);
		double cy4 = Math.sin(limbSwing * 6 % (Math.PI * 2) - Math.PI * 1.6);

		GlStateManager.rotate(180.0F, 0F, 0F, 1F);
		GlStateManager.translate(0F, -1.5F, 0F);

		ResourceManager.getModel(MODEL).renderPart("Body");

		// Eyestalks
		GlStateManager.pushMatrix();
		GlStateManager.translate(-0.125F, 0.5625F, -0.3125F);
		GlStateManager.rotate(headPitch, 1F, 0F, 0F);
		GlStateManager.rotate(netHeadYaw, 0F, 1F, 0F);
		GlStateManager.translate(0.125F, -0.5625F, 0.3125F);
		ResourceManager.getModel(MODEL).renderPart("EyeL");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.125F, 0.5625F, -0.3125F);
		GlStateManager.rotate(headPitch, 1F, 0F, 0F);
		GlStateManager.rotate(netHeadYaw, 0F, 1F, 0F);
		GlStateManager.translate(-0.125F, -0.5625F, 0.3125F);
		ResourceManager.getModel(MODEL).renderPart("EyeR");
		GlStateManager.popMatrix();

		ResourceManager.getModel(MODEL).renderPart("ClawL");
		ResourceManager.getModel(MODEL).renderPart("ClawR");

		// Left legs
		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy0 * 8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegL1");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy1 * 8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegL2");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy2 * 8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegL3");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy3 * 8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegL4");
		GlStateManager.popMatrix();

		// Right legs, one phase step off, swing amplitude sign-flipped
		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy1 * -8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegR1");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy2 * -8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegR2");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy3 * -8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegR3");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy4 * -8), 0F, 0F, 1F);
		ResourceManager.getModel(MODEL).renderPart("LegR4");
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}
}
