package com.hbm.render.entity.projectile;

import com.hbm.entity.projectile.EntityCoin;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * How the coin draws: a small disc, laid flat by the entity's facing, then spun about itself over its lifetime (45 degrees per tick).
 *
 * The model still goes through this tree's unified ResourceManager.getModel (the old one was an HFRWavefrontObject static field + asVBO;
 * here parsing defers to first draw and survives resource pack reloads); GL all GlStateManager,
 * the old glRotated double call becomes the float version.
 */
public class RenderCoin extends Render<EntityCoin> {

	private static final String MODEL = "models/trinkets/chip.obj";
	private static final ResourceLocation TEX = new ResourceLocation(RefStrings.MODID, "textures/models/trinkets/chip_gold.png");

	public RenderCoin(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCoin coin, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.rotate(coin.prevRotationYaw + (coin.rotationYaw - coin.prevRotationYaw) * partialTicks - 90.0F, 0.0F, -1.0F, 0.0F);
		GlStateManager.rotate((float) ((coin.ticksExisted + partialTicks) * 45D), 0F, 0F, 1F);

		float scale = 0.125F;
		GlStateManager.scale(scale, scale, scale);

		this.bindEntityTexture(coin);
		ResourceManager.getModel(MODEL).renderAll();

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCoin coin) {
		return TEX;
	}
}
