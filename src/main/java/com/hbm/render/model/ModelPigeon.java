package com.hbm.render.model;

import com.hbm.entity.mob.EntityPigeon;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * The pigeon's rig: head, beak, body (skinny and fat sets), two legs, two wings, a rear with a tuft of tail feathers.
 *
 * The body copies the old version; the sole delta is {@code MathHelper} now living in {@code net.minecraft.util.math}.
 *
 * The two bodies ({@code body}/{@code bodyFat}) share the same region of the same texture, differing only in addBox's trailing inflate amount
 * (0 vs 1) — that's what puffs out the fat one; the wings hang off both bodies at once, so switching bodies needs no re-hanging.
 * Which set is used and how parts shift all hang on {@code isFat()} (bread eaten or not) — see the two checks in the two methods below.
 */
public class ModelPigeon extends ModelBase {

	public ModelRenderer head;
	public ModelRenderer beak;
	public ModelRenderer body;
	public ModelRenderer bodyFat;
	public ModelRenderer leftLeg;
	public ModelRenderer rightLeg;
	public ModelRenderer leftWing;
	public ModelRenderer rightWing;
	public ModelRenderer ass;
	public ModelRenderer feathers;

	public ModelPigeon() {
		this.head = new ModelRenderer(this, 0, 0);
		this.head.addBox(-2F, -6F, -2F, 4, 6, 4);
		this.head.setRotationPoint(0F, 16F, -2F);
		this.beak = new ModelRenderer(this, 14, 0);
		this.beak.addBox(-1F, -4F, -4F, 2, 2, 2);
		this.beak.setRotationPoint(0F, 16F, -2F);

		this.body = new ModelRenderer(this, 0, 10);
		this.body.addBox(-3F, -3F, -4F, 6, 6, 8, 0);
		this.body.setRotationPoint(0F, 17F, 0F);
		this.bodyFat = new ModelRenderer(this, 0, 10);
		this.bodyFat.addBox(-3F, -3F, -4F, 6, 6, 8, 1);
		this.bodyFat.setRotationPoint(0F, 17F, 0F);
		this.ass = new ModelRenderer(this, 0, 24);
		this.ass.addBox(-2F, -2F, -2F, 4, 4, 4);
		this.ass.setRotationPoint(0F, 20F, 4F);
		this.feathers = new ModelRenderer(this, 16, 24);
		this.feathers.addBox(-1F, -0.5F, -2F, 2, 1, 4);
		this.feathers.setRotationPoint(0F, 21.5F, 7.5F);

		this.leftLeg = new ModelRenderer(this, 20, 0);
		this.leftLeg.addBox(-1F, 0F, 0F, 2, 4, 2);
		this.leftLeg.setRotationPoint(1F, 20F, -1F);
		this.rightLeg = new ModelRenderer(this, 20, 0);
		this.rightLeg.addBox(-1F, 0F, 0F, 2, 4, 2);
		this.rightLeg.setRotationPoint(-1F, 20F, -1F);

		this.leftWing = new ModelRenderer(this, 28, 0);
		this.leftWing.addBox(0F, 0F, -3F, 1, 4, 6);
		this.leftWing.setRotationPoint(3F, -2F, 0F);
		this.rightWing = new ModelRenderer(this, 28, 10);
		this.rightWing.addBox(-1F, 0F, -3F, 1, 4, 6);
		this.rightWing.setRotationPoint(-3F, -2F, 0F);

		this.body.addChild(this.leftWing);
		this.body.addChild(this.rightWing);
		this.bodyFat.addChild(this.leftWing);
		this.bodyFat.addChild(this.rightWing);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {

		this.head.rotateAngleX = this.beak.rotateAngleX = headPitch / (180F / (float) Math.PI);
		this.head.rotateAngleY = this.beak.rotateAngleY = netHeadYaw / (180F / (float) Math.PI);
		this.body.rotateAngleX = this.bodyFat.rotateAngleX = this.ass.rotateAngleX = -((float) Math.PI / 4F);
		this.feathers.rotateAngleX = -((float) Math.PI / 8F);
		this.rightLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leftLeg.rotateAngleX = MathHelper.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rightWing.rotateAngleZ = ageInTicks;
		this.leftWing.rotateAngleZ = -ageInTicks;

		if(((EntityPigeon) entityIn).isFat()) {
			this.head.rotationPointZ = -4F;
			this.beak.rotationPointZ = -4F;
			this.ass.rotationPointZ = 5F;
			this.feathers.rotationPointZ = 8.5F;
			this.leftWing.rotationPointX = 4F;
			this.rightWing.rotationPointX = -4F;
		} else {
			this.head.rotationPointZ = -2F;
			this.beak.rotationPointZ = -2F;
			this.ass.rotationPointZ = 4F;
			this.feathers.rotationPointZ = 7.5F;
			this.leftWing.rotationPointX = 3F;
			this.rightWing.rotationPointX = -3F;
		}
	}

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		this.head.render(scale);
		this.beak.render(scale);

		if(((EntityPigeon) entityIn).isFat()) {
			this.bodyFat.render(scale);
		} else {
			this.body.render(scale);
		}

		this.rightLeg.render(scale);
		this.leftLeg.render(scale);
		this.ass.render(scale);
		this.feathers.render(scale);
	}
}
