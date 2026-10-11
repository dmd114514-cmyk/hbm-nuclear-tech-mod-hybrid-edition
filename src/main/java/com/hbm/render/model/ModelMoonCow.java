package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelCow;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

/**
 * The moon cow: vanilla's cow model wholesale, plus one glass visor over the head.
 *
 * The visor is "draw a 1×1×1 cube under the current matrix, all six faces covered by the held glass texture",
 * called in the old version via {@code RenderUtil.renderBlock(Tessellator.instance)}; that utility doesn't exist here,
 * so the 24 points are copied verbatim into this class (coords ±0.5, UV 0~1, not one changed).
 *
 * Three convention changes:
 * - Calves must first shift the matrix onto the "child tier" pair of offsets before drawing the visor: the old version read ModelCow's two private fields,
 *   this version lifted them into the base {@code ModelQuadruped} as {@code childYOffset}/{@code childZOffset},
 *   and it uses them inside its own push/pop in {@code render} — by the time super returns, the matrix is restored, so the same translation is added here once, as the old version did.
 * - the {@code GL11.glXxx} string becomes {@code GlStateManager}, with GL11 keeping only the primitive-mode constant.
 * - {@code Tessellator.instance} becomes {@code getInstance()}, with {@code draw()} called after.
 *
 * No blending: the old version drew it plain, the glass texture's transparent parts didn't show through, and the look stays that way — changing the look is another matter.
 */
public class ModelMoonCow extends ModelCow {

	private static final ResourceLocation GLASS = new ResourceLocation(RefStrings.MODID, "textures/blocks/glass_boron.png");

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.pushMatrix();

		Minecraft.getMinecraft().getTextureManager().bindTexture(GLASS);

		if(this.isChild) {
			GlStateManager.translate(0.0F, this.childYOffset * scale, this.childZOffset * scale);
		}

		GlStateManager.translate(this.head.offsetX, this.head.offsetY, this.head.offsetZ);
		GlStateManager.translate(this.head.rotationPointX * scale, this.head.rotationPointY * scale, this.head.rotationPointZ * scale);
		GlStateManager.rotate(this.head.rotateAngleZ * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(this.head.rotateAngleY * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(this.head.rotateAngleX * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);

		GlStateManager.translate(0, 0, -0.25F);
		GlStateManager.scale(0.7F, 0.7F, 0.7F);

		renderBlock();

		GlStateManager.popMatrix();
	}

	/** The 24 points of the old RenderUtil.renderBlock: an edge-1 cube, each face tiled once with the current texture (UV 0~1) */
	private void renderBlock() {

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(-0.5, +0.5, -0.5).tex(1, 1).endVertex();
		buffer.pos(+0.5, +0.5, -0.5).tex(0, 1).endVertex();
		buffer.pos(+0.5, -0.5, -0.5).tex(0, 0).endVertex();
		buffer.pos(-0.5, -0.5, -0.5).tex(1, 0).endVertex();

		buffer.pos(-0.5, +0.5, +0.5).tex(1, 1).endVertex();
		buffer.pos(-0.5, +0.5, -0.5).tex(0, 1).endVertex();
		buffer.pos(-0.5, -0.5, -0.5).tex(0, 0).endVertex();
		buffer.pos(-0.5, -0.5, +0.5).tex(1, 0).endVertex();

		buffer.pos(+0.5, +0.5, +0.5).tex(1, 1).endVertex();
		buffer.pos(-0.5, +0.5, +0.5).tex(0, 1).endVertex();
		buffer.pos(-0.5, -0.5, +0.5).tex(0, 0).endVertex();
		buffer.pos(+0.5, -0.5, +0.5).tex(1, 0).endVertex();

		buffer.pos(+0.5, +0.5, -0.5).tex(1, 1).endVertex();
		buffer.pos(+0.5, +0.5, +0.5).tex(0, 1).endVertex();
		buffer.pos(+0.5, -0.5, +0.5).tex(0, 0).endVertex();
		buffer.pos(+0.5, -0.5, -0.5).tex(1, 0).endVertex();

		buffer.pos(-0.5, -0.5, -0.5).tex(1, 1).endVertex();
		buffer.pos(+0.5, -0.5, -0.5).tex(0, 1).endVertex();
		buffer.pos(+0.5, -0.5, +0.5).tex(0, 0).endVertex();
		buffer.pos(-0.5, -0.5, +0.5).tex(1, 0).endVertex();

		buffer.pos(+0.5, +0.5, -0.5).tex(1, 1).endVertex();
		buffer.pos(-0.5, +0.5, -0.5).tex(0, 1).endVertex();
		buffer.pos(-0.5, +0.5, +0.5).tex(0, 0).endVertex();
		buffer.pos(+0.5, +0.5, +0.5).tex(1, 0).endVertex();

		Tessellator.getInstance().draw();
	}
}
