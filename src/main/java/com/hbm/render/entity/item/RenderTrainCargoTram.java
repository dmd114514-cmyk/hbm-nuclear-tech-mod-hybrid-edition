package com.hbm.render.entity.item;

import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * How the flatbed cargo tram draws: the whole vehicle is one OBJ (tram.obj holds a single Plane group), wearing one body texture.
 *
 * Position doesn't follow the entity's pos but the rail contact point the cart core computes (renderX/Y/Z),
 * with "entity position minus contact point" added back — on curves the two never coincide.
 * GL all GlStateManager; the old glRotated double call becomes the float version.
 */
public class RenderTrainCargoTram extends Render<EntityRailCarBase> {

	protected static final ResourceLocation TEX = new ResourceLocation(RefStrings.MODID, "textures/models/trains/tram.png");
	protected static final String MODEL = "models/vehicles/tram.obj";

	public RenderTrainCargoTram(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityRailCarBase train, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();

		double iX = train.prevPosX + (train.posX - train.prevPosX) * partialTicks;
		double iY = train.prevPosY + (train.posY - train.prevPosY) * partialTicks;
		double iZ = train.prevPosZ + (train.posZ - train.prevPosZ) * partialTicks;
		double rX = train.lastRenderX + (train.renderX - train.lastRenderX) * partialTicks;
		double rY = train.lastRenderY + (train.renderY - train.lastRenderY) * partialTicks;
		double rZ = train.lastRenderZ + (train.renderZ - train.lastRenderZ) * partialTicks;
		x -= iX - rX;
		y -= iY - rY;
		z -= iZ - rZ;

		GlStateManager.translate(x, y, z);

		// Facing takes the shortest arc: the old version folded to ±180 before interpolating, and that final -720 is a cosmetic fix so it spins the extra two turns
		float yaw = train.rotationYaw;
		float prevYaw = train.prevRotationYaw;

		if(yaw - prevYaw > 180) yaw -= 360;
		if(prevYaw - yaw > 180) prevYaw -= 360;

		float yawInterp = prevYaw + (yaw - prevYaw) * partialTicks - 720;

		GlStateManager.rotate(-yawInterp, 0F, 1F, 0F);

		float pitch = train.rotationPitch;
		float prevPitch = train.prevRotationPitch;
		float pitchInterp = prevPitch + (pitch - prevPitch) * partialTicks;
		GlStateManager.rotate(-pitchInterp, 1F, 0F, 0F);

		GlStateManager.disableCull();
		this.bindTexture(TEX);
		ResourceManager.getModel(MODEL).renderAll();
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRailCarBase train) {
		return TEX;
	}
}
