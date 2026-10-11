package com.hbm.render.model;

import com.hbm.main.ResourceManager;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySquid;

/**
 * The depths squid's shape: a body tube + frilled fins on both sides (swinging out of phase by limbSwing) + a ring of eight tentacles, bending with the two interpolated
 * angles (tentacleAngle/squidPitch) vanilla's squid produces.
 *
 * The model is OBJ named parts (models/mobs/depthsquid.obj: Body/FinL/FinR/T0..T7); the old version used static asVBO fields,
 * this version fetches through ResourceManager.getModel every time; GL goes through GlStateManager.
 * The tentacle ring still draws the same T0 eight times — the old version never used the obj's T1..T7 either; eight identical legs is what that ring looks like,
 * and it doesn't "fix itself" into eight distinct pieces.
 *
 * The door without partialTicks stays old-style: ModelBase.render's seven args carry no interpolation amount, so it's stashed
 * into a field inside setLivingAnimations for later use (the very line the old comment cursed).
 */
public class ModelDepthSquid extends ModelBase {

	private static final String MODEL = "models/mobs/depthsquid.obj";

	private float interp;

	@Override
	public void setLivingAnimations(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks) {
		this.interp = partialTicks;
	}

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		float tentacleAngle = 0;
		float squidPitch = 0;
		if(entity instanceof EntitySquid) {
			EntitySquid squid = (EntitySquid) entity;

			tentacleAngle = squid.lastTentacleAngle + (squid.tentacleAngle - squid.lastTentacleAngle) * interp;
			squidPitch = squid.prevSquidPitch + (squid.squidPitch - squid.prevSquidPitch) * interp;
		}

		GlStateManager.pushMatrix();

		double cy0 = Math.sin(limbSwing % (Math.PI * 2));
		double cy1 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.4);

		GlStateManager.translate(0F, 1.0F, 0F);
		GlStateManager.rotate(squidPitch, 1F, 0F, 0F);

		ResourceManager.getModel(MODEL).renderPart("Body");

		// Side fins
		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy1 * 20), 1F, 0F, 0F);
		GlStateManager.rotate((float) (cy0 * 10), 0F, 1F, 0F);
		ResourceManager.getModel(MODEL).renderPart("FinL");
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotate((float) (cy1 * 20), 1F, 0F, 0F);
		GlStateManager.rotate((float) (cy0 * -10), 0F, 1F, 0F);
		ResourceManager.getModel(MODEL).renderPart("FinR");
		GlStateManager.popMatrix();

		// The tentacle ring
		for(int i = 0; i < 8; i++) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0F, -0.9F, 0F);
			GlStateManager.rotate((float) i * -45.0F, 0F, 1F, 0F);
			GlStateManager.rotate(tentacleAngle * (180F / (float) Math.PI), 1F, 0F, 0F);
			GlStateManager.translate(0F, 0.9F, 0F);
			ResourceManager.getModel(MODEL).renderPart("T0");
			GlStateManager.popMatrix();
		}

		GlStateManager.popMatrix();
	}
}
