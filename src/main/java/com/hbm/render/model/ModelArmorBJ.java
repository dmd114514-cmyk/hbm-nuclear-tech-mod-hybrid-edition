package com.hbm.render.model;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class ModelArmorBJ extends ModelArmorBase {

	ModelRendererObj jetpack;

	public ModelArmorBJ(int type) {
		super(type);

		this.head = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "Head");
		this.body = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "Body");
		this.jetpack = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "Jetpack");
		this.leftArm = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "LeftArm").setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightArm = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "RightArm").setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftLeg = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "LeftLeg").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightLeg = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "RightLeg").setRotationPoint(-1.9F, 12.0F, 0.0F);
		this.leftFoot = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "LeftFoot").setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightFoot = new ModelRendererObj(ResourceManager.getModel(ResourceManager.armor_bj), "RightFoot").setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
		this.body.copyTo(this.jetpack);

		GlStateManager.pushMatrix();

		if(this.type == 0) {
			bindTexture(ResourceManager.bj_eyepatch);
			this.head.render(scaleFactor);
		}
		if(this.type == 1 || this.type == 5) {
			bindTexture(ResourceManager.bj_chest);
			this.body.render(scaleFactor);

			if(this.type == 5) {
				bindTexture(ResourceManager.bj_jetpack);
				this.jetpack.render(scaleFactor);
			}

			bindTexture(ResourceManager.bj_arm);
			this.leftArm.render(scaleFactor);
			this.rightArm.render(scaleFactor);
		}
		if(this.type == 2) {
			bindTexture(ResourceManager.bj_leg);
			this.leftLeg.render(scaleFactor);
			this.rightLeg.render(scaleFactor);
		}
		if(this.type == 3) {
			bindTexture(ResourceManager.bj_leg);
			this.leftFoot.render(scaleFactor);
			this.rightFoot.render(scaleFactor);
		}

		GlStateManager.popMatrix();
	}
}
