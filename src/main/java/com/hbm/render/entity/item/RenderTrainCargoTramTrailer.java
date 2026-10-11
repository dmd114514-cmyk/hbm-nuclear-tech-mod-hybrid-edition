package com.hbm.render.entity.item;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.train.EntityRailCarBase;
import com.hbm.entity.train.TrainCargoTramTrailer;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * How the trailer draws: same pose as the locomotive (each carries its own copy, as before), body swapped for trailer.obj,
 * and the cargo bay stacks one crate per loaded slot.
 *
 * The stack count comes from the entity-synced "occupied slots", never the entity's internal slot array — the client's copy is empty.
 *
 * Two honest deviations:
 *   · the old version drew crates via EntityItem + RenderManager.renderEntityWithPosYaw; this version uses this version's
 *     renderEntity(entity, x, y, z, yaw, partialTicks, bobbing). Verified it hands coordinates to
 *     doRender untouched (no more viewerPos subtraction), so the old "body-local offsets" copy over as-is, no viewer delta needed.
 *     The old RenderItem.renderInFrame switch doesn't exist here; item entity rendering pushes/pops its own state now,
 *     nothing leaks out, so it isn't needed.
 *   · crates use the iron crate: the old wooden crate isn't in this tree; the crate block lives on the block side, so once it lands,
 *     swap the one reference below — how many, and where, already line up with the old version.
 */
public class RenderTrainCargoTramTrailer extends Render<EntityRailCarBase> {

	private static final ResourceLocation TEX = new ResourceLocation(RefStrings.MODID, "textures/models/trains/tram_trailer.png");
	private static final String MODEL = "models/vehicles/tram_trailer.obj";

	public RenderTrainCargoTramTrailer(RenderManager renderManager) {
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

		if(train instanceof TrainCargoTramTrailer) {
			this.renderCrates((TrainCargoTramTrailer) train);
		}

		GlStateManager.popMatrix();
	}

	/** One crate per occupied slot, position table carried over entry by entry */
	private void renderCrates(TrainCargoTramTrailer train) {

		int slots = train.getOccupiedSlotCount();
		if(slots <= 0) return;

		EntityItem dummy = new EntityItem(train.getEntityWorld(), 0, 0, 0, new ItemStack(ModBlocks.crate_iron));
		dummy.hoverStart = 0.0F;

		GlStateManager.scale(2, 2, 2);

		if(slots <= 5) {
			this.put(dummy, 0.0D, 0.375D, 0.0D);
		} else if(slots <= 10) {
			this.put(dummy, 0.1D, 0.375D, 0.25D);
			this.put(dummy, -0.1D, 0.375D, -0.25D);
		} else if(slots <= 15) {
			this.put(dummy, 0.1D, 0.375D, 0.0D);
			this.put(dummy, -0.1D, 0.375D, 0.375D);
			this.put(dummy, -0.1D, 0.375D, -0.375D);
		} else if(slots <= 20) {
			this.put(dummy, 0.2D, 0.375D, 0.3D);
			this.put(dummy, 0.2D, 0.375D, -0.2D);
			this.put(dummy, -0.2D, 0.375D, 0.2D);
			this.put(dummy, -0.2D, 0.375D, -0.3D);
		} else if(slots <= 25) {
			this.put(dummy, 0.2D, 0.375D, 0.6D);
			this.put(dummy, 0.2D, 0.375D, 0.0D);
			this.put(dummy, 0.2D, 0.375D, -0.5D);
			this.put(dummy, -0.2D, 0.375D, 0.2D);
			this.put(dummy, -0.2D, 0.375D, -0.3D);
		} else if(slots <= 30) {
			this.put(dummy, 0.2D, 0.375D, 0.6D);
			this.put(dummy, 0.2D, 0.375D, 0.0D);
			this.put(dummy, 0.2D, 0.375D, -0.5D);
			this.put(dummy, -0.2D, 0.375D, 0.5D);
			this.put(dummy, -0.2D, 0.375D, -0.1D);
			this.put(dummy, -0.2D, 0.375D, -0.6D);
		} else if(slots <= 35) {
			this.put(dummy, 0.2D, 0.375D, 0.4D);
			this.put(dummy, 0.2D, 0.375D, 0.0D);
			this.put(dummy, 0.2D, 0.375D, -0.4D);
			this.put(dummy, -0.2D, 0.375D, 0.3D);
			this.put(dummy, -0.2D, 0.375D, -0.1D);
			this.put(dummy, -0.2D, 0.375D, -0.5D);
			this.put(dummy, 0.0D, 0.6875D, -0.25D);
		} else if(slots <= 40) {
			this.put(dummy, 0.2D, 0.375D, 0.4D);
			this.put(dummy, 0.2D, 0.375D, 0.0D);
			this.put(dummy, 0.2D, 0.375D, -0.4D);
			this.put(dummy, -0.2D, 0.375D, 0.3D);
			this.put(dummy, -0.2D, 0.375D, -0.1D);
			this.put(dummy, -0.2D, 0.375D, -0.5D);
			this.put(dummy, 0.0D, 0.6875D, -0.25D);
			this.put(dummy, 0.0D, 0.6875D, 0.15D);
		} else {
			this.put(dummy, 0.2D, 0.375D, 0.4D);
			this.put(dummy, 0.2D, 0.375D, 0.0D);
			this.put(dummy, 0.2D, 0.375D, -0.4D);
			this.put(dummy, -0.2D, 0.375D, 0.3D);
			this.put(dummy, -0.2D, 0.375D, -0.1D);
			this.put(dummy, -0.2D, 0.375D, -0.5D);
			this.put(dummy, 0.0D, 0.6875D, -0.25D);
			this.put(dummy, 0.0D, 0.6875D, 0.15D);
			this.put(dummy, -0.1D, 0.375D, 0.8D);
		}
	}

	private void put(EntityItem dummy, double x, double y, double z) {
		this.renderManager.renderEntity(dummy, x, y, z, 0F, 0F, false);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRailCarBase train) {
		return TEX;
	}
}
