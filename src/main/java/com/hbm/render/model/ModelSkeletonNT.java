package com.hbm.render.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

/**
 * The spindly skeleton model: the same humanoid rig, just with limbs built from 2x12x2 sticks.
 *
 * The old parent was ModelZombie(scale, 0, 64, 32); that four-arg constructor didn't survive into this version (ModelZombie only has ()
 * and (float, boolean), hardwiring 64x64); but back then it just forwarded the args to ModelBiped anyway, so this hooks
 * ModelBiped's four-arg constructor directly — identical geometry, texture width back to 64x32.
 */
public class ModelSkeletonNT extends ModelBiped {

	public ModelSkeletonNT() {
		this(0.0F);
	}

	public ModelSkeletonNT(float scale) {
		super(scale, 0.0F, 64, 32);
		this.bipedRightArm = new ModelRenderer(this, 40, 16);
		this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, scale);
		this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.bipedLeftArm = new ModelRenderer(this, 40, 16);
		this.bipedLeftArm.mirror = true;
		this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 12, 2, scale);
		this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		this.bipedRightLeg = new ModelRenderer(this, 0, 16);
		this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, scale);
		this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		this.bipedLeftLeg = new ModelRenderer(this, 0, 16);
		this.bipedLeftLeg.mirror = true;
		this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 12, 2, scale);
		this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
	}
}
