package com.hbm.inventory.gui.element;

import java.util.List;

// Only its integer constants are borrowed (primitive modes, shading models); GL state always goes through GlStateManager
import org.lwjgl.opengl.GL11;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.RefStrings;
import com.hbm.util.ColorUtil;
import com.hbm.util.Vec3NT;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class GUIElements {

	@Deprecated public static enum Gauge {
		ROUND_SMALL("small_round", 18, 18, 13);
		ResourceLocation texture;
		int width, height, count;
		private Gauge(String texture, int width, int height, int count) {
			this.texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/gauges/" + texture + ".png");
			this.width = width;
			this.height = height;
			this.count = count;
		}
	}

	@Deprecated public static void renderGauge(Gauge gauge, double x, double y, double z, double progress) {
		Minecraft.getMinecraft().renderEngine.bindTexture(gauge.texture);
		int frameNum = (int) Math.round((gauge.count - 1) * progress);
		double singleFrame = 1D / (double)gauge.count;
		double frameOffset = singleFrame * frameNum;
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(x, 				y + gauge.height, 	z).tex(0, 	frameOffset + singleFrame).endVertex();
		buf.pos(x + gauge.width, 	y + gauge.height, 	z).tex(1, 	frameOffset + singleFrame).endVertex();
		buf.pos(x + gauge.width, 	y, 					z).tex(1, 	frameOffset).endVertex();
		buf.pos(x, 				y, 					z).tex(0, 	frameOffset).endVertex();
		Tessellator.getInstance().draw();
	}

	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color) {
		drawSmoothGauge(x, y, z, progress, tipLength, backLength, backSide, color, 0x000000);
	}

	//these reused vectors are static on purpose; saves newing them every frame
	private static Vec3NT tip = new Vec3NT();
	private static Vec3NT left = new Vec3NT();
	private static Vec3NT right = new Vec3NT();

	public static void drawSmoothGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, int color, int colorOuter) {
		GlStateManager.disableTexture2D();

		progress = MathHelper.clamp(progress, 0, 1);

		float angle = (float) Math.toRadians(-progress * 270 - 45);
		tip.setComponents(0, tipLength, 0);
		left.setComponents(backSide, -backLength, 0);
		right.setComponents(-backSide, -backLength, 0);

		tip.rotateAroundZRad(angle);
		left.rotateAroundZRad(angle);
		right.rotateAroundZRad(angle);

		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
		double mult = 1.5;
		vertex(buf, x + tip.x * mult, y + tip.y * mult, z, colorOuter);
		vertex(buf, x + left.x * mult, y + left.y * mult, z, colorOuter);
		vertex(buf, x + right.x * mult, y + right.y * mult, z, colorOuter);
		vertex(buf, x + tip.x, y + tip.y, z, color);
		vertex(buf, x + left.x, y + left.y, z, color);
		vertex(buf, x + right.x, y + right.y, z, color);
		Tessellator.getInstance().draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
	}

	/** The old setColorOpaque_I took 0xRRGGBB; now it splits into four 0~1 components by hand */
	private static void vertex(BufferBuilder buf, double x, double y, double z, int rgb) {
		buf.pos(x, y, z).color(ColorUtil.fr(rgb), ColorUtil.fg(rgb), ColorUtil.fb(rgb), 1F).endVertex();
	}

	public static void drawSmoothLinearGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, double scale, float rotation, int color) {
		drawSmoothLinearGauge(x, y, z, progress, tipLength, backLength, backSide, scale, rotation, color, 0x000000);
	}

	private static Vec3NT Bleft = new Vec3NT();
	private static Vec3NT Bright = new Vec3NT();

	public static void drawSmoothLinearGauge(int x, int y, double z, double progress, double tipLength, double backLength, double backSide, double scale, float rotation, int color, int colorOuter) {
		GlStateManager.disableTexture2D();

		scale = Math.max(scale, 1);
		progress = MathHelper.clamp(progress, 0, 1) * scale;

		tip.setComponents(0, -tipLength, 0);
		right.setComponents(-backSide, 0, 0);
		Bright.setComponents(-backSide, backLength, 0);
		Bleft.setComponents(backSide, backLength, 0);
		left.setComponents(backSide, 0, 0);

		float angle = (float) Math.toRadians(-rotation);

		tip.rotateAroundZRad(angle);
		right.rotateAroundZRad(angle);
		Bright.rotateAroundZRad(angle);
		Bleft.rotateAroundZRad(angle);
		left.rotateAroundZRad(angle);

		double deltaX = progress * MathHelper.cos(angle);
		double deltaY = progress * MathHelper.sin(angle);

		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		double mult = 1.5;
		//it was GL_POLYGON, which BufferBuilder doesn't support; the five points are convex, so a triangle fan draws the identical shape
		buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		vertex(buf, x + deltaX + tip.x * mult, y + deltaY + tip.y * mult, z, colorOuter);
		vertex(buf, x + deltaX + right.x * mult, y + deltaY + right.y * mult, z, colorOuter);
		vertex(buf, x + deltaX + Bright.x * mult, y + deltaY + Bright.y, z, colorOuter);
		vertex(buf, x + deltaX + Bleft.x * mult, y + deltaY + Bleft.y, z, colorOuter);
		vertex(buf, x + deltaX + left.x * mult, y + deltaY + left.y * mult, z, colorOuter);
		Tessellator.getInstance().draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		buf.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
		vertex(buf, x + deltaX + tip.x, y + deltaY + tip.y, z, color);
		vertex(buf, x + deltaX + right.x, y + deltaY + right.y, z, color);
		vertex(buf, x + deltaX + Bright.x, y + deltaY + Bright.y, z, color);
		vertex(buf, x + deltaX + Bleft.x, y + deltaY + Bleft.y, z, color);
		vertex(buf, x + deltaX + left.x, y + deltaY + left.y, z, color);
		Tessellator.getInstance().draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
	}

	public static void drawSmoothTextureModalCircle(int xDraw, int yDraw, float zDraw, int xStart, int yStart, int xDelta, int yDelta, double progress) {
		float var7 = 0.00390625F;
		float var8 = 0.00390625F;

		progress = MathHelper.clamp(progress, 0, 1);
		float angle = (float) (-progress * 270.0);
		double theta = Math.toRadians(angle - 135);
		int addons = 0;
		double xTarget = 0;
		double yTarget = 0;

		// addons is just a number flag deciding how many fixed points to add to complete the triangles
		if (angle >= -180 && angle < -90) {
			addons = 1;
		} else if (angle >= -270 && angle < -180) {
			addons = 2;
		}

		// The hideous branch below only decides where the last point lands.
		// The whole function draws this shape:
		// + - - - - - - - - - - - +
		// | \ * * * * * * * * * / |
		// | * \ * * * * * * * / * |
		// | * * \ * * * * * / * * |
		// | * * * \ * * * / * * * |
		// | * * * * \ * / * * * * |
		// | * * * * * + * * * * * |
		// | * * * * /   \ * * * * |
		// | * * * /       \ * * * |
		// | * * /           \ * * |
		// | * /               \ * |
		// | /                   \ |
		// + - - - - - - - - - - - +
		// "/" and "\\" are edges, "+" is the anchor, "*" is what actually draws (the lower triangle doesn't show)
		if (angle >= -90) {
			xTarget = -1;
			yTarget = -Math.tan(theta);
		} else if (angle > -135 && angle < -90) {
			xTarget = Math.tan(Math.PI/2 - theta);
			yTarget = 1;
		} else if (angle > -180 && angle < -135) {
			xTarget = Math.tan(Math.PI/2 - theta);
			yTarget = 1;
		} else if (angle <= -180) {
			xTarget = 1;
			yTarget = Math.tan(theta);
		} else if (angle == -135) {
			xTarget = 0;
			yTarget = 1;
		}

		double xMid = (double) xDelta / 2;
		double yMid = (double) yDelta / 2;

		xTarget *= xMid;
		yTarget *= yMid;

		BufferBuilder buf = Tessellator.getInstance().getBuffer();

		buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX); // yes, GL_POLYGON was the right call; couldn't make it work, sadly
		texVertex(buf, xDraw, yDraw + yDelta, zDraw, xStart * var7, (yStart + yDelta) * var8);
		texVertex(buf, xDraw + xMid, yDraw + yMid, zDraw, ((float) (xStart + xMid) * var7), ((float) (yStart + yMid) * var8));
		if (addons == 2 || addons == 1) {
			texVertex(buf, xDraw, yDraw, zDraw, xStart * var7, yStart * var8);
			Tessellator.getInstance().draw();
			buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
			texVertex(buf, xDraw, yDraw, zDraw, xStart * var7, yStart * var8);
			texVertex(buf, xDraw + xMid, yDraw + yMid, zDraw, ((float) (xStart + xMid) * var7), ((float) (yStart + yMid) * var8));
		}
		if (addons == 2) {
			texVertex(buf, xDraw + xDelta, yDraw, zDraw, (xStart + xDelta) * var7, yStart * var8);
			Tessellator.getInstance().draw();
			buf.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
			texVertex(buf, xDraw + xDelta, yDraw, zDraw, (xStart + xDelta) * var7, yStart * var8);
			texVertex(buf, xDraw + xMid, yDraw + yMid, zDraw, ((float) (xStart + xMid) * var7), ((float) (yStart + yMid) * var8));
		}
		texVertex(buf, xDraw + xTarget + xMid, yDraw - yTarget + yMid, zDraw, ((float) (xStart + xTarget + xMid) * var7), ((float) (yStart - yTarget + yMid) * var8));
		Tessellator.getInstance().draw();
	}

	private static void texVertex(BufferBuilder buf, double x, double y, double z, float u, float v) {
		buf.pos(x, y, z).tex(u, v).endVertex();
	}

	public static final int STANDARD_COLOR_BACKGROUND = -0xFEFFFF0;
	public static final int STANDARD_COLOR_LINE0 = 0x505000FF;
	public static final int STANDARD_COLOR_LINE1 = (STANDARD_COLOR_LINE0 & 0xFEFEFE) >> 1 | STANDARD_COLOR_LINE0 & -0xFEFEFE;
	public static final int RECIPE_COLOR_LINE0 = 0xFFFF8000;
	public static final int RECIPE_COLOR_LINE1 = 0xFFFFFF00;
	public static final int STANDARD_HEADER_OFFSET = 2;
	public static final int STANDARD_LINE_DIST = 10;
	public static void drawHoveringText(List<String> lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight) {
		drawHoveringText(lines, x, y, font, itemRender, guiWidth, guiHeight, STANDARD_HEADER_OFFSET, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_LINE0, STANDARD_COLOR_LINE1);
	}
	public static void drawHoveringTextRecipe(List<String> lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight) {
		drawHoveringText(lines, x, y, font, itemRender, guiWidth, guiHeight, 6, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, RECIPE_COLOR_LINE0, RECIPE_COLOR_LINE1);
	}

	public static void drawHoveringTextFluid(List<String> lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight, FluidType type) {
		int color0 = type.getColor();
		int r = ColorUtil.ir(color0);
		int g = ColorUtil.ig(color0);
		int b = ColorUtil.ib(color0);
		int add = (r + g + b) / 3 > 0x80 ? -0x40 : 0x40;
		int color1 = ColorUtil.color(MathHelper.clamp(r + add, 0, 255), MathHelper.clamp(g + add, 0, 255), MathHelper.clamp(b + add, 0, 255));
		color0 |= 0xff000000;
		color1 |= 0xff000000;
		drawHoveringText(lines, x, y, font, itemRender, guiWidth, guiHeight, 6, STANDARD_LINE_DIST, STANDARD_COLOR_BACKGROUND, STANDARD_COLOR_BACKGROUND, color0, color1);
	}

	public static void drawHoveringText(List<String> lines, int x, int y, FontRenderer font, RenderItem itemRender, int guiWidth, int guiHeight, int headerOffset, int lineDist, int colBG0, int colBG1, int colLine0, int colLine1) {

		if(!lines.isEmpty()) {
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int width = 0;

			for(String line : lines) {
				int lineLength = font.getStringWidth(line);

				if(lineLength > width) {
					width = lineLength;
				}
			}

			int boundX = x + 12;
			int boundY = y - 12;
			int height = 6 + headerOffset;

			if(lines.size() > 1) {
				height += 2 + (lines.size() - 1) * lineDist;
			}

			// nudged inward when it's about to poke past the bottom-right edge
			if(boundX + width + 4 > guiWidth) boundX -= 28 + width;
			if(boundY + height + 6 > guiHeight) boundY = guiHeight - height - 6;

			// then check the top-left overhang — the worse one, which vanilla somehow never handled
			if(boundX < 4) boundX = 4;
			if(boundY < 4) boundY = 4;

			itemRender.zLevel = 300.0F;
			drawGradientRect(boundX - 3, boundY - 4, boundX + width + 3, boundY - 3, colBG0, colBG0);
			drawGradientRect(boundX - 3, boundY + height + 3, boundX + width + 3, boundY + height + 4, colBG1, colBG1);
			drawGradientRect(boundX - 3, boundY - 3, boundX + width + 3, boundY + height + 3, colBG0, colBG1);
			drawGradientRect(boundX - 4, boundY - 3, boundX - 3, boundY + height + 3, colBG0, colBG1);
			drawGradientRect(boundX + width + 3, boundY - 3, boundX + width + 4, boundY + height + 3, colBG0, colBG1);

			drawGradientRect(boundX - 3, boundY - 3 + 1, boundX - 3 + 1, boundY + height + 3 - 1, colLine0, colLine1);
			drawGradientRect(boundX + width + 2, boundY - 3 + 1, boundX + width + 3, boundY + height + 3 - 1, colLine0, colLine1);
			drawGradientRect(boundX - 3, boundY - 3, boundX + width + 3, boundY - 3 + 1, colLine0, colLine0);
			drawGradientRect(boundX - 3, boundY + height + 2, boundX + width + 3, boundY + height + 3, colLine1, colLine1);

			for(int i = 0; i < lines.size(); ++i) {
				String line = lines.get(i);
				font.drawStringWithShadow(line, boundX, boundY, -1);

				if(i == 0) boundY += headerOffset;
				boundY += lineDist;
			}

			itemRender.zLevel = 0.0F;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

	/** Colors are ARGB, not RGBA (the cursed route) */
	protected static void drawGradientRect(int x0, int y0, int x1, int y1, int col0, int col1) {
		float a0 = (float) (col0 >> 24	& 255) / 255F;
		float r0 = (float) (col0 >> 16	& 255) / 255F;
		float g0 = (float) (col0 >> 8	& 255) / 255F;
		float b0 = (float) (col0		& 255) / 255F;
		float a1 = (float) (col1 >> 24	& 255) / 255F;
		float r1 = (float) (col1 >> 16	& 255) / 255F;
		float g1 = (float) (col1 >> 8	& 255) / 255F;
		float b1 = (float) (col1		& 255) / 255F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		GlStateManager.shadeModel(7425); // GL_SMOOTH
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		buf.pos(x1, y0, 300D).color(r0, g0, b0, a0).endVertex();
		buf.pos(x0, y0, 300D).color(r0, g0, b0, a0).endVertex();
		buf.pos(x0, y1, 300D).color(r1, g1, b1, a1).endVertex();
		buf.pos(x1, y1, 300D).color(r1, g1, b1, a1).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.shadeModel(7424); // GL_FLAT
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}
}
