package com.hbm.render.util;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Draws a beam from origin to skeleton (spiral or jitter); SOLID is layered cylinder shells, LINE is a polyline.
 * Vertex format is POSITION_COLOR throughout, colors written per vertex — glColor4f no longer feeds vertex data in 1.12.2.
 */
public class BeamPronter {

	public static Random rand = new Random();

	public enum EnumWaveType {
		RANDOM, SPIRAL
	}

	public enum EnumBeamType {
		SOLID, LINE
	}

	private static boolean depthMask = false;

	public static void prontBeamwithDepth(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
		prontBeamwithDepth(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness, 1F);
	}

	public static void prontBeamwithDepth(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness, float alpha) {
		depthMask = true;
		prontBeam(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness, alpha);
		depthMask = false;
	}

	public static void prontBeam(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
		prontBeam(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness, 1F);
	}

	public static void prontBeam(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness, float alpha) {

		GlStateManager.pushMatrix();
		GlStateManager.depthMask(depthMask);

		float sYaw = (float) (Math.atan2(skeleton.x, skeleton.z) * 180F / Math.PI);
		float sqrt = MathHelper.sqrt(skeleton.x * skeleton.x + skeleton.z * skeleton.z);
		float sPitch = (float) (Math.atan2(skeleton.y, sqrt) * 180F / Math.PI);

		GlStateManager.rotate(180, 0, 1F, 0);
		GlStateManager.rotate(sYaw, 0, 1F, 0);
		GlStateManager.rotate(sPitch - 90, 1F, 0, 0);

		GlStateManager.pushMatrix();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();

		if(beam == EnumBeamType.SOLID) {
			GlStateManager.disableCull();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		}

		BufferBuilder buffer = Tessellator.getInstance().getBuffer();

		double length = skeleton.lengthVector();
		double segLength = length / segments;
		double lastX = 0;
		double lastY = 0;
		double lastZ = 0;

		rand.setSeed(start);

		for(int i = 0; i <= segments; i++) {

			// Jitter radius: random around the Y axis, or advancing in a spiral
			double spinX = size;
			double spinZ = 0;

			if(wave == EnumWaveType.SPIRAL) {
				float a = (float) Math.PI * (float) start / 180F + (float) Math.PI * 45F / 180F * i;
				spinX = size * MathHelper.cos(a);
				spinZ = size * MathHelper.sin(a);
			} else if(wave == EnumWaveType.RANDOM) {
				float a = (float) (Math.PI * 2 * rand.nextFloat() + Math.PI * 2 * rand.nextFloat());
				spinX = size * MathHelper.cos(a);
				spinZ = size * MathHelper.sin(a);
			}

			double pX = spinX;
			double pY = segLength * i;
			double pZ = spinZ;

			if(beam == EnumBeamType.LINE && i > 0) {
				buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
				color(buffer, outerColor, alpha);
				buffer.pos(pX, pY, pZ).endVertex();
				buffer.pos(lastX, lastY, lastZ).endVertex();
				Tessellator.getInstance().draw();
			}

			if(beam == EnumBeamType.SOLID && i > 0) {

				float radius = thickness / layers;

				int r1 = (outerColor & 0xFF0000) >> 16;
				int g1 = (outerColor & 0x00FF00) >> 8;
				int b1 = (outerColor & 0x0000FF);
				int r2 = (innerColor & 0xFF0000) >> 16;
				int g2 = (innerColor & 0x00FF00) >> 8;
				int b2 = (innerColor & 0x0000FF);

				for(int j = 1; j <= layers; j++) {

					// Color interpolates outer to inner; with layers == 1, (j-1)/(layers-1) is 0/0 and the NaN turns the beam black
					float inter = layers > 1 ? (float) (j - 1) / (float) (layers - 1) : 0F;

					int r = ((int) (r1 + (r2 - r1) * inter)) << 16;
					int g = ((int) (g1 + (g2 - g1) * inter)) << 8;
					int b = ((int) (b1 + (b2 - b1) * inter));
					int color = r | g | b;

					float o = radius * j;

					// Four side walls, making up a square column
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
					color(buffer, color, alpha);
					buffer.pos(lastX + o, lastY, lastZ + o).endVertex();
					buffer.pos(lastX + o, lastY, lastZ - o).endVertex();
					buffer.pos(pX + o, pY, pZ - o).endVertex();
					buffer.pos(pX + o, pY, pZ + o).endVertex();
					Tessellator.getInstance().draw();

					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
					color(buffer, color, alpha);
					buffer.pos(lastX - o, lastY, lastZ + o).endVertex();
					buffer.pos(lastX - o, lastY, lastZ - o).endVertex();
					buffer.pos(pX - o, pY, pZ - o).endVertex();
					buffer.pos(pX - o, pY, pZ + o).endVertex();
					Tessellator.getInstance().draw();

					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
					color(buffer, color, alpha);
					buffer.pos(lastX + o, lastY, lastZ + o).endVertex();
					buffer.pos(lastX - o, lastY, lastZ + o).endVertex();
					buffer.pos(pX - o, pY, pZ + o).endVertex();
					buffer.pos(pX + o, pY, pZ + o).endVertex();
					Tessellator.getInstance().draw();

					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
					color(buffer, color, alpha);
					buffer.pos(lastX + o, lastY, lastZ - o).endVertex();
					buffer.pos(lastX - o, lastY, lastZ - o).endVertex();
					buffer.pos(pX - o, pY, pZ - o).endVertex();
					buffer.pos(pX + o, pY, pZ - o).endVertex();
					Tessellator.getInstance().draw();
				}
			}

			lastX = pX;
			lastY = pY;
			lastZ = pZ;
		}

		if(beam == EnumBeamType.LINE) {
			buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
			color(buffer, innerColor, alpha);
			buffer.pos(0, 0, 0).endVertex();
			buffer.pos(0, length, 0).endVertex();
			Tessellator.getInstance().draw();
		}

		if(beam == EnumBeamType.SOLID) {
			GlStateManager.disableBlend();
			GlStateManager.enableTexture2D();
		}

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();

		GlStateManager.popMatrix();
		GlStateManager.depthMask(true);

		GlStateManager.popMatrix();
	}

	private static void color(BufferBuilder buffer, int color, float alpha) {
		buffer.color(((color >> 16) & 0xFF) / 255F, ((color >> 8) & 0xFF) / 255F, (color & 0xFF) / 255F, alpha);
	}
}
