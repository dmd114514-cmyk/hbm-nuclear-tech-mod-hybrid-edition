package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelArmorTrenchmaster extends ModelArmorBase {

	ModelRendererObj light;

	public ModelArmorTrenchmaster(int type) {
		super(type);

		this.head = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "Helmet");
		this.light = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "Light");
		this.body = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "Chest");
		this.leftArm = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightArm = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftLeg = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightLeg = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		this.leftFoot = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "LeftBoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightFoot = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_trenchmaster), "RightBoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
		this.head.copyTo(this.light);

		GlStateManager.pushMatrix();

		if(this.type == 0) {
			bindTexture(ResourceManager.trenchmaster_helmet);
			GlStateManager.enableBlend();
			OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
			this.head.render(scaleFactor);
			GlStateManager.disableBlend();

			/// START GLOW ///
			float lastX = OpenGlHelper.lastBrightnessX;
			float lastY = OpenGlHelper.lastBrightnessY;
			GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
			GlStateManager.disableLighting();
			this.light.render(scaleFactor);
			GlStateManager.enableLighting();
			GL11.glPopAttrib();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastX, lastY);
			/// END GLOW ///
		}
		if(this.type == 1) {
			bindTexture(ResourceManager.trenchmaster_chest);
			this.body.render(scaleFactor);
			bindTexture(ResourceManager.trenchmaster_arm);
			this.leftArm.render(scaleFactor);
			this.rightArm.render(scaleFactor);
		}
		if(this.type == 2) {
			bindTexture(ResourceManager.trenchmaster_leg);
			GL11.glTranslated(-0.01, 0, 0);
			this.leftLeg.render(scaleFactor);
			GL11.glTranslated(0.02, 0, 0);
			this.rightLeg.render(scaleFactor);
		}
		if(this.type == 3) {
			bindTexture(ResourceManager.trenchmaster_leg);
			GL11.glTranslated(-0.01, 0, 0);
			this.leftFoot.render(scaleFactor);
			GL11.glTranslated(0.02, 0, 0);
			this.rightFoot.render(scaleFactor);
		}

		GlStateManager.popMatrix();
	}
}
