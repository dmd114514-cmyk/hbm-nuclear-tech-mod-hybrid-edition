package com.hbm.render.model;

import com.hbm.render.loader.ModelRendererObj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * The bedrock of armor models: a worn armor piece is eight parts (head/body/two arms/two legs/two feet) plus any number of optional parts,
 * each part carrying its own rotation point and offset, with poses always copied from "whichever biped model this entity currently runs".
 *
 * The old version also had a stretch of hand-rolled arm-swing/arm-press/bow-aim math; those fields (onGround, heldItemLeft/Right, aimedBow)
 * no longer exist this version; rather than copy a set that no longer matches, let vanilla finish the pose math itself (super) and copy the results into the parts —
 * it runs vanilla's own path, the look matches the old version, and it never has to chase vanilla's internal churn.
 */
public class ModelArmorBase extends ModelBiped {

	public int type;
	public ModelRendererObj head;
	public ModelRendererObj body;
	public ModelRendererObj leftArm;
	public ModelRendererObj rightArm;
	public ModelRendererObj leftLeg;
	public ModelRendererObj rightLeg;
	public ModelRendererObj leftFoot;
	public ModelRendererObj rightFoot;

	public ModelArmorBase(int type) {
		this.type = type;

		// Seed with empties first, so one armor missing an entry doesn't blow up the whole hand
		this.head = new ModelRendererObj(null);
		this.body = new ModelRendererObj(null);
		this.leftArm = new ModelRendererObj(null).setRotationPoint(5.0F, 2.0F, 0.0F);
		this.rightArm = new ModelRendererObj(null).setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.leftLeg = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightLeg = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
		this.leftFoot = new ModelRendererObj(null).setRotationPoint(1.9F, 12.0F, 0.0F);
		this.rightFoot = new ModelRendererObj(null).setRotationPoint(-1.9F, 12.0F, 0.0F);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {

		ModelBiped worn = getWornBiped(entity);

		if(worn != null) {
			copyPropertiesFromBiped(worn);
			this.isSneak = worn.isSneak;
			this.isRiding = worn.isRiding;
			return;
		}

		/// Fallback: if the renderer can't produce a biped model (non-humanoid, or no renderer attached yet), compute it once here ///
		this.isSneak = entity.isSneaking();
		this.isRiding = entity.isRiding();

		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
		copyPropertiesFromBiped(this);
	}

	/** The biped model this entity currently runs; null if none can be found */
	protected ModelBiped getWornBiped(Entity entity) {

		if(entity == null) return null;

		// The old version went through RenderManager.instance; that field is no longer public, so the renderer is fetched from Minecraft
		Render<?> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entity);

		if(render instanceof RenderPlayer) return ((RenderPlayer) render).getMainModel();
		if(render instanceof RenderBiped) return (ModelBiped) ((RenderBiped) render).getMainModel();

		return null;
	}

	/** Texture names all live on that named table (A2's ResourceManager); binding is all this does */
	protected static void bindTexture(ResourceLocation location) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(location);
	}

	private void copyPropertiesFromBiped(ModelBiped modelBiped) {

		if(modelBiped == null) return;

		this.head.copyRotationFrom(modelBiped.bipedHead);
		this.body.copyRotationFrom(modelBiped.bipedBody);
		this.leftArm.copyRotationFrom(modelBiped.bipedLeftArm);
		this.rightArm.copyRotationFrom(modelBiped.bipedRightArm);
		this.leftLeg.copyRotationFrom(modelBiped.bipedLeftLeg);
		this.rightLeg.copyRotationFrom(modelBiped.bipedRightLeg);
		this.leftFoot.copyRotationFrom(modelBiped.bipedLeftLeg);
		this.rightFoot.copyRotationFrom(modelBiped.bipedRightLeg);
	}
}
