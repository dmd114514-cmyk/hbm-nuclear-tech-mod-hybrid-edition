package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityCloudTom;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

/**
 * The Tom cloud's drawing: sixteen lobes, five layers each, a cylindrical sheet whose texture scrolls down via the texture matrix, producing the "fire pillar rising" look.
 *
 * What changed in the move is this version's shape: 1.12.2 quads run the full begin/pos/tex/color/endVertex set
 * (the old one was Tessellator.instance.startDrawingQuads + addVertexWithUV);
 * vertex color swaps setColorRGBA_F for buffer.color; vector becomes the immutable Vec3d,
 * the old rotateAroundY mutated in place, here the return value must be caught; GL through GlStateManager.
 * One honest note: the old version used doRender's **fifth parameter** (yaw) as the tween fraction (names f0, f1 mixed in),
 * and this takes this version's partialTicks, or the pillar never tweens and just jumps once per tick.
 */
public class RenderCloudTom extends Render<EntityCloudTom> {

	private static final ResourceLocation TEX = new ResourceLocation("hbm", "textures/models/explosion/tomblast.png");

	public RenderCloudTom(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityCloudTom blast, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.disableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.depthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		double scale = blast.age + partialTicks;

		int segments = 16;
		float angle = (float) Math.toRadians(360D / segments);
		int height = 20;
		int depth = 20;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		this.bindTexture(TEX);

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();

		float movement = -(Minecraft.getMinecraft().player.ticksExisted + partialTicks) * 0.005F * 10;
		GlStateManager.translate(0F, movement, 0F);

		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		for(int i = 0; i < segments; i++) {

			for(int j = 0; j < 5; j++) {

				double mod = 1 - j * 0.025;
				double h = height + j * 10;
				double off = 1D / j;

				Vec3d vec = new Vec3d(scale, 0, 0).rotateYaw(angle * i);
				double x0 = vec.x * mod;
				double z0 = vec.z * mod;

				buffer.pos(x0, h, z0).tex(0, 1 + off).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
				buffer.pos(x0, -depth, z0).tex(0, 0 + off).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

				vec = vec.rotateYaw(angle);
				x0 = vec.x * mod;
				z0 = vec.z * mod;

				buffer.pos(x0, -depth, z0).tex(1, 0 + off).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
				buffer.pos(x0, h, z0).tex(1, 1 + off).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
			}
		}

		Tessellator.getInstance().draw();

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.depthMask(true);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.depthMask(true);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlpha();
		GlStateManager.enableCull();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityCloudTom blast) {
		return TEX;
	}
}
