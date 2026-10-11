package com.hbm.render.util;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/** Hazard diamond: one texture cut into a base diamond + three digit cells + one hazard cell, laid onto the +X face quad by quad */
public class DiamondPronter {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/models/misc/danger_diamond.png");

	public static void pront(int poison, int flammability, int reactivity, EnumSymbol symbol) {

		GlStateManager.pushMatrix();

		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);

		float p = 1F / 256F;
		float s = 1F / 139F;

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		quad(buffer, 0.0, 0.5, -0.5, p * 144, p * 45, 0.0, 0.5, 0.5, p * 5, p * 45, 0.0, -0.5, 0.5, p * 5, p * 184, 0.0, -0.5, -0.5, p * 144, p * 184);
		Tessellator.getInstance().draw();

		float width = 10F * s;
		float height = 14F * s;

		if(poison >= 0 && poison < 6) {
			int x = poison == 0 ? 125 : 5 + (poison - 1) * 24;
			int y = 5;
			quadBox(buffer, height, 0, 33 * s, width, x, y, 20, 28, p);
		}

		if(flammability >= 0 && flammability < 6) {
			int x = flammability == 0 ? 125 : 5 + (flammability - 1) * 24;
			int y = 5;
			quadBox(buffer, height, 33 * s, 0, width, x, y, 20, 28, p);
		}

		if(reactivity >= 0 && reactivity < 6) {
			int x = reactivity == 0 ? 125 : 5 + (reactivity - 1) * 24;
			int y = 5;
			quadBox(buffer, height, 0, -33 * s, width, x, y, 20, 28, p);
		}

		float symSize = 59F / 2F * s;

		if(symbol != EnumSymbol.NONE) {
			int x = symbol.x;
			int y = symbol.y;
			quadBox(buffer, symSize, -33 * s, 0, symSize, x, y, 59, 59, p);
		}

		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	// Digit cells and the hazard cell share one shape, differing only in texture offset — factored out so it isn't copied four times
	private static void quadBox(BufferBuilder buffer, float height, float oY, float oZ, float width, int x, int y, int w, int h, float p) {
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		quad(buffer,
				0.01, height + oY, -width + oZ, (x + w) * p, y * p,
				0.01, height + oY, width + oZ, x * p, y * p,
				0.01, -height + oY, width + oZ, x * p, (y + h) * p,
				0.01, -height + oY, -width + oZ, (x + w) * p, (y + h) * p);
		Tessellator.getInstance().draw();
	}

	private static void quad(BufferBuilder buffer,
			double x1, double y1, double z1, double u1, double v1,
			double x2, double y2, double z2, double u2, double v2,
			double x3, double y3, double z3, double u3, double v3,
			double x4, double y4, double z4, double u4, double v4) {
		buffer.pos(x1, y1, z1).tex(u1, v1).endVertex();
		buffer.pos(x2, y2, z2).tex(u2, v2).endVertex();
		buffer.pos(x3, y3, z3).tex(u3, v3).endVertex();
		buffer.pos(x4, y4, z4).tex(u4, v4).endVertex();
	}
}
