package com.hbm.render.util;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.hbm.interfaces.Spaghetti;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;

/**
 * Draws a centered mini block in the world (5/16 square); multiblock visualization TESRs use it to lay out components.
 *
 * The old version took face textures from IIcon by (Block, meta); 1.12.2 has no meta, face textures belong to the baked model,
 * so the signature takes an IBlockState, pulling the first sprite from getQuads per face (empty falls back to the particle texture).
 * Vertex coords match the old version point for point: the cube spans 0.34375..0.65625, the whole texture squashed into each small face.
 */
public class SmallBlockPronter {

	static float pixel = 1F/16F;

	/** The old door kept: texture bound by the caller, UV still spread over the whole image */
	@Deprecated @Spaghetti("this is horseshit")
	public static void renderSmolBlockAt(float x, float y, float z) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x - 1, y - 1, z);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();

		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 0).endVertex();
			buf.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 0).endVertex();
			buf.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buf.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
		tess.draw();

			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buf.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 0).endVertex();
			buf.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buf.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 1).endVertex();
		tess.draw();

			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buf.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buf.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(0, 1).endVertex();
			buf.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 1).endVertex();
		tess.draw();

			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 0).endVertex();
			buf.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buf.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(0, 1).endVertex();
			buf.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
		tess.draw();

			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buf.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buf.pos(11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buf.pos(1 - 11 * pixel / 2, 1 - 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
		tess.draw();

			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(1, 0).endVertex();
			buf.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 11 * pixel / 2).tex(0, 0).endVertex();
			buf.pos(1 - 11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(0, 1).endVertex();
			buf.pos(11 * pixel / 2, 11 * pixel / 2, 1 - 11 * pixel / 2).tex(1, 1).endVertex();
		tess.draw();

		GlStateManager.popMatrix();
	}

	public static void startDrawing() {
		GlStateManager.enableBlend();
		GlStateManager.enableCull();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.75F);
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		Tessellator.getInstance().getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
	}

	public static void draw() {
		Tessellator.getInstance().draw();
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.depthMask(true);
		GlStateManager.enableLighting();
	}

	public static void drawSmolBlockAt(IBlockState state, float x, float y, float z) {

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buf = tess.getBuffer();

		TextureAtlasSprite spriteTop = spriteFor(state, EnumFacing.UP);
		TextureAtlasSprite spriteBottom = spriteFor(state, EnumFacing.DOWN);
		TextureAtlasSprite spriteNorth = spriteFor(state, EnumFacing.NORTH);
		TextureAtlasSprite spriteSouth = spriteFor(state, EnumFacing.SOUTH);
		TextureAtlasSprite spriteEast = spriteFor(state, EnumFacing.EAST);
		TextureAtlasSprite spriteWest = spriteFor(state, EnumFacing.WEST);

		buf.pos(x + 1 - 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteSouth.getMaxU(), spriteSouth.getMinV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteSouth.getMinU(), spriteSouth.getMinV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteSouth.getMinU(), spriteSouth.getMaxV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteSouth.getMaxU(), spriteSouth.getMaxV()).endVertex();

		buf.pos(x + 1 - 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteEast.getMaxU(), spriteEast.getMinV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteEast.getMinU(), spriteEast.getMinV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteEast.getMinU(), spriteEast.getMaxV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteEast.getMaxU(), spriteEast.getMaxV()).endVertex();

		buf.pos(x + 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteNorth.getMaxU(), spriteNorth.getMinV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteNorth.getMinU(), spriteNorth.getMinV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteNorth.getMinU(), spriteNorth.getMaxV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteNorth.getMaxU(), spriteNorth.getMaxV()).endVertex();

		buf.pos(x + 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteWest.getMaxU(), spriteWest.getMinV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteWest.getMinU(), spriteWest.getMinV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteWest.getMinU(), spriteWest.getMaxV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteWest.getMaxU(), spriteWest.getMaxV()).endVertex();

		buf.pos(x + 1 - 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteTop.getMaxU(), spriteTop.getMinV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteTop.getMinU(), spriteTop.getMinV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteTop.getMinU(), spriteTop.getMaxV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 1 - 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteTop.getMaxU(), spriteTop.getMaxV()).endVertex();

		buf.pos(x + 11 * pixel / 2, y + 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteBottom.getMaxU(), spriteBottom.getMinV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 11 * pixel / 2, z + 11 * pixel / 2).tex(spriteBottom.getMinU(), spriteBottom.getMinV()).endVertex();
		buf.pos(x + 1 - 11 * pixel / 2, y + 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteBottom.getMinU(), spriteBottom.getMaxV()).endVertex();
		buf.pos(x + 11 * pixel / 2, y + 11 * pixel / 2, z + 1 - 11 * pixel / 2).tex(spriteBottom.getMaxU(), spriteBottom.getMaxV()).endVertex();
	}

	/** Face texture comes from the baked model's first quad for that direction; no quad, fall back to the particle texture */
	private static TextureAtlasSprite spriteFor(IBlockState state, EnumFacing face) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		List<BakedQuad> quads = model.getQuads(state, face, 0L);
		if(!quads.isEmpty()) return quads.get(0).getSprite();
		return model.getParticleTexture();
	}
}
