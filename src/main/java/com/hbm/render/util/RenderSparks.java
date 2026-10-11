package com.hbm.render.util;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

/** A cluster of random polyline sparks; two line widths stack the core and the halo */
public class RenderSparks {

	public static void renderSpark(int seed, double x, double y, double z, float length, int min, int max, int color1, int color2) {

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.glLineWidth(3F);

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		Random rand = new Random(seed);
		Vec3d vec = new Vec3d(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5).normalize();

		int segments = min + rand.nextInt(max);

		for(int i = 0; i < segments; i++) {

			double prevX = x;
			double prevY = y;
			double prevZ = z;

			Vec3d dir = vec.normalize();
			x = prevX + dir.x * length * rand.nextFloat();
			y = prevY + dir.y * length * rand.nextFloat();
			z = prevZ + dir.z * length * rand.nextFloat();

			GlStateManager.glLineWidth(5F);
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			buffer.color(((color1 >> 16) & 0xFF) / 255F, ((color1 >> 8) & 0xFF) / 255F, (color1 & 0xFF) / 255F, 1F);
			buffer.pos(prevX, prevY, prevZ).endVertex();
			buffer.pos(x, y, z).endVertex();
			Tessellator.getInstance().draw();

			GlStateManager.glLineWidth(2F);
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			buffer.color(((color2 >> 16) & 0xFF) / 255F, ((color2 >> 8) & 0xFF) / 255F, (color2 & 0xFF) / 255F, 1F);
			buffer.pos(prevX, prevY, prevZ).endVertex();
			buffer.pos(x, y, z).endVertex();
			Tessellator.getInstance().draw();
		}

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}
}
