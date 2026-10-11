package com.hbm.render.util;

import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.S_Face;
import com.hbm.render.loader.S_GroupObject;
import com.hbm.render.loader.TextureCoordinate;
import com.hbm.render.loader.Vertex;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;

/**
 * Draws OBJ geometry with texture icons (not the model's own materials).
 *
 * Calling convention: the caller opens the BufferBuilder with DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL,
 * and this class only appends vertices — no begin, no draw. In 1.12.2 color is a vertex attribute and must be written per vertex.
 *
 * Note: the **sign convention** of vertex rotation is hand-written to match the old Vec3.rotateAround* semantics (Vec3 and its
 * rotateAround* are gone in 1.12.2, no source to check against). If a model faces the wrong way in-game,
 * just flip the sin term of the offending axis; all three live in the rotate local function.
 */
public class ObjUtil {

	public static void renderWithIcon(HFRWavefrontObject model, TextureAtlasSprite icon, BufferBuilder buf, float rot, boolean shadow) {
		renderWithIcon(model, icon, buf, rot, 0, 0, shadow);
	}

	public static void renderWithIcon(HFRWavefrontObject model, TextureAtlasSprite icon, BufferBuilder buf, float rot, float pitch, boolean shadow) {
		renderWithIcon(model, icon, buf, rot, pitch, 0, shadow);
	}

	public static void renderWithIcon(HFRWavefrontObject model, TextureAtlasSprite icon, BufferBuilder buf, float rot, float pitch, float roll, boolean shadow) {

		for(int g = 0; g < model.groupObjects.size(); g++) {
			S_GroupObject go = model.groupObjects.get(g);

			for(int fi = 0; fi < go.faces.size(); fi++) {
				S_Face f = go.faces.get(fi);

				Vertex n = f.faceNormal;

				float[] normal = rotate(ROT_NORMAL, n.x, n.y, n.z, roll, pitch, rot);

				float brightness = 1F;

				if(shadow) {
					brightness = (normal[1] + 0.7F) * 0.9F - Math.abs(normal[0]) * 0.1F + Math.abs(normal[2]) * 0.1F;
					if(brightness < 0.45F) brightness = 0.45F;
				}

				for(int i = 0; i < f.vertices.length; i++) {

					Vertex v = f.vertices[i];
					float[] pos = rotate(ROT_POS, v.x, v.y, v.z, roll, pitch, rot);

					TextureCoordinate t = f.textureCoordinates[i];
					float u = icon.getInterpolatedU(t.u * 16D);
					float vv = icon.getInterpolatedV(t.v * 16D);

					emit(buf, pos, u, vv, brightness, brightness, brightness, normal);

					// The old hack of drawing triangle models through the quad tessellator: repeat the third point to round out four
					if(i % 3 == 2) emit(buf, pos, u, vv, brightness, brightness, brightness, normal);
				}
			}
		}
	}

	public static void renderPartWithIcon(HFRWavefrontObject model, String name, TextureAtlasSprite icon, BufferBuilder buf, float rot, boolean shadow) {
		renderPartWithIcon(model, name, icon, buf, rot, 0, 0, shadow);
	}

	public static void renderPartWithIcon(HFRWavefrontObject model, String name, TextureAtlasSprite icon, BufferBuilder buf, float rot, float pitch, boolean shadow) {
		renderPartWithIcon(model, name, icon, buf, rot, pitch, 0, shadow);
	}

	public static void renderPartWithIcon(HFRWavefrontObject model, String name, TextureAtlasSprite icon, BufferBuilder buf, float rot, float pitch, float roll, boolean shadow) {

		S_GroupObject go = null;

		for(int i = 0; i < model.groupObjects.size(); i++) {
			S_GroupObject obj = model.groupObjects.get(i);
			if(obj.name.equals(name)) go = obj;
		}

		if(go == null) return;

		for(int fi = 0; fi < go.faces.size(); fi++) {
			S_Face f = go.faces.get(fi);

			Vertex n = f.faceNormal;

			float[] normal = rotate(ROT_NORMAL, n.x, n.y, n.z, roll, pitch, rot);

			float r = 1F;
			float g = 1F;
			float b = 1F;

			if(shadow || hasColor) {

				float brightness = 1.0F;

				if(shadow) {
					brightness = (normal[1] * 0.3F + 0.7F) - Math.abs(normal[0]) * 0.1F + Math.abs(normal[2]) * 0.1F;
					if(brightness < 0.45F) brightness = 0.45F;
				}

				if(hasColor) {
					r = red * brightness / 255F;
					g = green * brightness / 255F;
					b = blue * brightness / 255F;
				} else {
					r = g = b = brightness;
				}
			}

			for(int i = 0; i < f.vertices.length; i++) {

				Vertex v = f.vertices[i];
				float[] pos = rotate(ROT_POS, v.x, v.y, v.z, roll, pitch, rot);

				TextureCoordinate t = f.textureCoordinates[i];
				float u = icon.getInterpolatedU(t.u * 16D);
				float vv = icon.getInterpolatedV(t.v * 16D);

				emit(buf, pos, u, vv, r, g, b, normal);

				if(f.vertices.length == 3 && i % 3 == 2) emit(buf, pos, u, vv, r, g, b, normal);
			}
		}
	}

	private static void emit(BufferBuilder buf, float[] pos, float u, float v, float r, float g, float b, float[] normal) {
		buf.pos(pos[0], pos[1], pos[2]).tex(u, v).color(r, g, b, 1F).normal(normal[0], normal[1], normal[2]).endVertex();
	}

	// Rotates around X(roll), Z(pitch), Y(rot) in that order, matching the old call order
	// Two scratch arrays hold face normals and vertices, avoiding per-vertex allocation (rendering is main-thread, static reuse is safe)
	private static final float[] ROT_NORMAL = new float[3];
	private static final float[] ROT_POS = new float[3];

	private static float[] rotate(float[] out, float x, float y, float z, float roll, float pitch, float rot) {

		float cosX = MathHelper.cos(roll);
		float sinX = MathHelper.sin(roll);
		float cosZ = MathHelper.cos(pitch);
		float sinZ = MathHelper.sin(pitch);
		float cosY = MathHelper.cos(rot);
		float sinY = MathHelper.sin(rot);

		double y1 = y * cosX + z * sinX;
		double z1 = z * cosX - y * sinX;

		double x2 = x * cosZ + y1 * sinZ;
		double y2 = y1 * cosZ - x * sinZ;

		double x3 = x2 * cosY + z1 * sinY;
		double z3 = z1 * cosY - x2 * sinY;

		out[0] = (float) x3;
		out[1] = (float) y2;
		out[2] = (float) z3;
		return out;
	}

	private static int red;
	private static int green;
	private static int blue;
	private static boolean hasColor = false;

	public static void setColor(int color) {
		red = (color & 0xff0000) >> 16;
		green = (color & 0x00ff00) >> 8;
		blue = color & 0x0000ff;
		hasColor = true;
	}

	public static void setColor(int r, int g, int b) {
		red = r;
		green = g;
		blue = b;
		hasColor = true;
	}

	public static void clearColor() {
		hasColor = false;
	}

	// Both methods assume the model faces +X (EAST); authored toward +Z (NORTH), pitch's turn direction gets unintuitive
	public static float getPitch(EnumFacing dir) {
		if(dir == EnumFacing.UP) return (float) Math.PI * -0.5F;
		if(dir == EnumFacing.DOWN) return (float) Math.PI * 0.5F;
		return 0;
	}

	public static float getYaw(EnumFacing dir) {
		if(dir == EnumFacing.NORTH) return (float) Math.PI * 0.5f;
		if(dir == EnumFacing.SOUTH) return (float) Math.PI * -0.5f;
		if(dir == EnumFacing.WEST) return (float) Math.PI;
		return 0;
	}
}
