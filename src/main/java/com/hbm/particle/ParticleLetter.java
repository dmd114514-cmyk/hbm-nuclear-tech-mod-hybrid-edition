package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A single floating letter: springs from tiny to full size while fading (drawn from the glyph set into the world like ParticleText).
 *
 * Self-drawn (layer 3): blending SRC_ALPHA/ONE_MINUS_SRC_ALPHA, depth writes off, brightness pinned at (240, 0),
 * and it never touches the atlas — the FontRenderer glyph set directly.
 * Opacity is **folded into the color integer's high 8 bits** and handed to drawString (not per-vertex color), kept verbatim from the old version;
 * the scale uses the 1 - e^-t spring-in curve, not linear.
 *
 * Same relocations as ParticleText: Particle doesn't extend Entity, so rotationYaw/Pitch ride along (genuinely used here);
 * mc.thePlayer → mc.player; the center takes absolute world coordinates (the engine stack is already translated by -viewerPos).
 * The old disabled depth writes are restored here; the long commented-out foreign damage-float implementation at the file's end (not even this mod's DIConfig) isn't ported.
 */
@SideOnly(Side.CLIENT)
public class ParticleLetter extends Particle {

	private final int color;
	private final char c;

	private float rotationYaw;
	private float rotationPitch;

	public ParticleLetter(World world, double x, double y, double z, int color, char c) {
		super(world, x, y, z);
		this.particleMaxAge = 30;
		this.color = color;
		this.c = c;
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		GlStateManager.pushMatrix();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.0F);
		RenderHelper.disableStandardItemLighting();

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;

		this.rotationYaw = -mc.player.rotationYaw;
		this.rotationPitch = mc.player.rotationPitch;

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		GlStateManager.translate(pX, pY, pZ);
		GlStateManager.rotate(this.rotationYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(this.rotationPitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

		float time = (this.particleAge + partial) * 4F / this.particleMaxAge;

		double scale = 1 - (1D / Math.pow(Math.E, time));

		this.particleAlpha = 1 - (((float) this.particleAge + partial) / (float) this.particleMaxAge);

		if(this.particleAlpha < 0) this.particleAlpha = 0;

		int alpha = (int) (this.particleAlpha * 255);

		if(alpha > 255) alpha = 255;
		// a floor so nearly-gone letters stay visible
		if(alpha < 10) alpha = 10;

		int col = this.color + (alpha << 24);

		GlStateManager.scale(scale, scale, scale);

		String s = String.valueOf(this.c);
		font.drawString(s, -(int) (font.getStringWidth(s) * 0.5F), -(int) (font.FONT_HEIGHT * 0.5F), col);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);

		GlStateManager.popMatrix();
	}
}
