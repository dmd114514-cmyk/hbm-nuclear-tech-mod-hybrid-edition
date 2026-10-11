package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityGhost;
import com.hbm.lib.RefStrings;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * The ghost's look: the player's humanoid model in a translucent skin, blending turned on and the alpha test dropped to 0 before drawing
 * (vanilla defaults to GREATER 0.1, which discards translucent pixels outright and leaves the ghost in flickering chunks); restored after, as usual.
 *
 * Three conventions this version:
 * - the old constructor's third argument 1.0F was RenderBiped's renderDistanceWeight; that slot is gone here
 *   (this version's third arg is translationY, default 0), and the old value was the default anyway, so it doesn't come over.
 * - GL all through GlStateManager: {@code glEnable(GL_BLEND)} → {@code enableBlend()};
 *   {@code OpenGlHelper.glBlendFunc(770, 771, 1, 0)} → those four constants are SRC_ALPHA / ONE_MINUS_SRC_ALPHA / ONE / ZERO.
 * - generic narrowed to the concrete type, {@code getEntityTexture} no longer casts from Entity; {@code doRender}'s parameter list is this version's six.
 */
public class RenderGhost extends RenderBiped<EntityGhost> {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/entity/ghost.png");

	public RenderGhost(RenderManager renderManager) {
		super(renderManager, new ModelBiped(0.0F), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityGhost entity) {
		return texture;
	}

	@Override
	public void doRender(EntityGhost entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
	}
}
