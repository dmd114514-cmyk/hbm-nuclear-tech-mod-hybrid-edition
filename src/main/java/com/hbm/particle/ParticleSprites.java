package com.hbm.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Small utilities for drawing billboard quads into the ParticleManager's shared buffer.
 *
 * That layer is begun by ParticleManager with `begin(GL_QUADS, PARTICLE_POSITION_TEX_COLOR_LMAP)`,
 * so this only appends vertices, in the order pos → tex → color → lightmap (matching the vertex format),
 * and must **never** begin/draw or touch GL state — that bleeds into every particle on the layer.
 *
 * Layer policy (bytecode-checked against renderParticles via javap -c; an earlier note had it backwards — this is the truth):
 *   renderParticles is a two-level loop over `fxLayers[i][j]`, i = getFXLayer() (bound 3, offset 118 iconst_3),
 *   j = the depth page (bound 2, offset 131 iconst_2), while page insertion uses Particle#shouldDisableDepth (updateEffects offset 131).
 *   · which texture binds follows **i**: the lookupswitch at offset 193 → only i==1 binds LOCATION_BLOCKS_TEXTURE (the block atlas);
 *     i==0 and i==2 (default) bind PARTICLE_TEXTURES, i.e. vanilla textures/particle/particles.png;
 *     corroborating: ParticleBreaking uses an atlas sprite and its getFXLayer measurably returns 1; vanilla's textureIndex-driven ParticleFlame
 *     doesn't override the door and takes the default 2, matching particles.png.
 *   · whether depth writes follows **j**, independent of layer: offset 153 → j==0 is depthMask(false), j==1 is depthMask(true),
 *     meaning you pick your side by overriding shouldDisableDepth().
 *   · i only reaches 2 ⇒ anything whose getFXLayer() returns 3 is "tick only, never drawn", routed to HbmParticleEngine's self-drawn channel.
 * So the three things to verify when triaging remain: texture source (atlas / particles.png / self-bound), depth writes, blending;
 * but the combination "atlas texture + depth writes" IS expressible (layer 1 + shouldDisableDepth=false) — don't fall back to self-drawing per the old note.
 *
 * Textures ride the block atlas (A2 registers `hbm:particle/*` in TextureStitchEvent.Pre),
 * and the u/v passed in are 0~1 coordinates within the texture itself, mapped into the atlas cell here.
 */
final class ParticleSprites {

	private ParticleSprites() { }

	static TextureAtlasSprite sprite(String name) {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("hbm:particle/" + name);
	}

	/**
	 * The door for the icons the old ModEventHandlerClient registered.
	 * 1.7.10's block atlas auto-prefixed icon names with textures/blocks/, so those pngs actually live in
	 * assets/hbm/textures/blocks/particle/; this version's atlas name is the path itself, so the prefix must be spelled out.
	 */
	static TextureAtlasSprite legacySprite(String name) {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("hbm:blocks/particle/" + name);
	}

	// Defaults when no lighting arguments are passed: the old particles called tess.setBrightness(240) per vertex,
	// which splits as "block light 240, sky light 0" (240%65536, 240/65536) — self-glowing but only through the block-light half,
	// hence the warm look. Don't "fix" it to (240, 240): that maxes sky light too, washing the color cold and white, unlike the original.
	static void emitQuad(BufferBuilder buffer, TextureAtlasSprite sprite,
			float pX, float pY, float pZ,
			float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ,
			float scale, float u0, float v0, float u1, float v1,
			float red, float green, float blue, float alpha) {

		emitQuad(buffer, sprite, pX, pY, pZ, rotX, rotZ, rotYZ, rotXY, rotXZ,
				scale, u0, v0, u1, v1, red, green, blue, alpha, 240, 0);
	}

	/**
	 * For particles that follow ambient light: lighting comes from Particle#getBrightnessForRender,
	 * split into (blockLight, skyLight) and written per vertex — the only way to express lighting in the shared buffer.
	 */
	static void emitQuad(BufferBuilder buffer, TextureAtlasSprite sprite,
			float pX, float pY, float pZ,
			float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ,
			float scale, float u0, float v0, float u1, float v1,
			float red, float green, float blue, float alpha,
			int blockLight, int skyLight) {

		float minU = sprite.getMinU();
		float minV = sprite.getMinV();
		float spanU = sprite.getMaxU() - minU;
		float spanV = sprite.getMaxV() - minV;

		float texU0 = minU + u0 * spanU;
		float texU1 = minU + u1 * spanU;
		float texV0 = minV + v0 * spanV;
		float texV1 = minV + v1 * spanV;

		vertex(buffer, pX - rotX * scale - rotXY * scale, pY - rotZ * scale, pZ - rotYZ * scale - rotXZ * scale, texU1, texV1, red, green, blue, alpha, blockLight, skyLight);
		vertex(buffer, pX - rotX * scale + rotXY * scale, pY + rotZ * scale, pZ - rotYZ * scale + rotXZ * scale, texU1, texV0, red, green, blue, alpha, blockLight, skyLight);
		vertex(buffer, pX + rotX * scale + rotXY * scale, pY + rotZ * scale, pZ + rotYZ * scale + rotXZ * scale, texU0, texV0, red, green, blue, alpha, blockLight, skyLight);
		vertex(buffer, pX + rotX * scale - rotXY * scale, pY - rotZ * scale, pZ + rotYZ * scale - rotXZ * scale, texU0, texV1, red, green, blue, alpha, blockLight, skyLight);
	}

	private static void vertex(BufferBuilder buffer, double x, double y, double z, float u, float v,
			float r, float g, float b, float a, int blockLight, int skyLight) {
		buffer.pos(x, y, z).tex(u, v).color(r, g, b, a).lightmap(blockLight, skyLight).endVertex();
	}
}
