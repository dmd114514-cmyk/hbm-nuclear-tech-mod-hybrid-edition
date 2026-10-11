package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * The sifter eel's serpentine body: the head follows pitch/yaw, side fins and dorsal/ventral fins swing on staggered sine phases, the tail segments following one by one.
 *
 * The model is OBJ named parts (models/mobs/siftereel.obj: Head/Jaw/FinL/FinR/Body1..6/Dorsal1..4/Ventral1..4/Tail),
 * the old version used static asVBO fields, this version fetches through ResourceManager.getModel (cache inside);
 * GL through GlStateManager; sway phases and angles copied line for line (including those % and Math.PI mixings — change them and the sway changes).
 */
public class ModelSifterEel extends ModelBase {

	private static final String MODEL = "models/mobs/siftereel.obj";

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.pushMatrix();

		double cy0 = Math.sin(limbSwing % (Math.PI * 2));
		double cy1 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.2);
		double cy2 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.4);
		double cy3 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.6);
		double cy4 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.9);
		double cy5 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 1.2);
		double cy6 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 1.5);
		double cy7 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 1.9);

		GlStateManager.rotate(180.0F, 0F, 0F, 1F);
		GlStateManager.translate(0F, -1.5F, 0F);

		// head
		GlStateManager.pushMatrix();
		GlStateManager.translate(0F, 0F, -0.5F);
		GlStateManager.rotate(headPitch, 1F, 0F, 0F);
		GlStateManager.rotate(netHeadYaw, 0F, 1F, 0F);
		GlStateManager.translate(0F, 0F, 0.5F);
		ResourceManager.getModel(MODEL).renderPart("Head");
		ResourceManager.getModel(MODEL).renderPart("Jaw");
		GlStateManager.popMatrix();

		// Side fins
		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy0 * 20), 1F, 0F, 0F);
		ResourceManager.getModel(MODEL).renderPart("FinL");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy0 * -20), 1F, 0F, 0F);
		ResourceManager.getModel(MODEL).renderPart("FinR");
		GlStateManager.popMatrix();

		// Body and tail fin
		GlStateManager.pushMatrix();

		ResourceManager.getModel(MODEL).renderPart("Body1");

		GlStateManager.rotate((float) (cy1 * 10), 0F, 1F, 0F);
		GlStateManager.rotate((float) (cy2 * -2), 0F, 1F, 0F);
		ResourceManager.getModel(MODEL).renderPart("Dorsal1");

		ResourceManager.getModel(MODEL).renderPart("Body2");
		GlStateManager.rotate((float) (cy3 * -2), 0F, 1F, 0F);
		ResourceManager.getModel(MODEL).renderPart("Dorsal2");
		ResourceManager.getModel(MODEL).renderPart("Dorsal3");
		GlStateManager.rotate((float) (cy4 * -6), 0F, 1F, 0F);

		ResourceManager.getModel(MODEL).renderPart("Body3");

		ResourceManager.getModel(MODEL).renderPart("Ventral1");
		ResourceManager.getModel(MODEL).renderPart("Ventral2");
		ResourceManager.getModel(MODEL).renderPart("Ventral3");
		GlStateManager.rotate((float) (cy5 * -4), 0F, 1F, 0F);
		ResourceManager.getModel(MODEL).renderPart("Dorsal4");
		ResourceManager.getModel(MODEL).renderPart("Ventral4");
		ResourceManager.getModel(MODEL).renderPart("Body4");
		GlStateManager.rotate((float) (cy6 * -6), 0F, 1F, 0F);
		ResourceManager.getModel(MODEL).renderPart("Body5");

		ResourceManager.getModel(MODEL).renderPart("Body6");

		GlStateManager.rotate((float) (cy7 * -5), 0F, 1F, 0F);
		ResourceManager.getModel(MODEL).renderPart("Tail");

		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}
}
