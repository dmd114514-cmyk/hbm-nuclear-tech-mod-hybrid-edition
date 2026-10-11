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
 * Floating world text: facing the camera, hovering slowly upward.
 *
 * Self-drawn (layer 3): it never touches the atlas — the FontRenderer's glyph set draws straight into world space.
 * Brightness keeps the old (240, 0) — confirming the setBrightness unpacking rule: block light maxed, sky light 0.
 *
 * Three relocations: the old version aimed text with Entity's rotationYaw/rotationPitch; Particle isn't an Entity,
 * so both fields are carried locally (**genuinely used here** — don't delete them like ParticleGiblet's dead pair); `mc.thePlayer` → `mc.player`;
 * the vertex baseline is rewritten to absolute world coordinates (the engine stack is already translated by -viewerPos). noClip → canCollide=false.
 */
@SideOnly(Side.CLIENT)
public class ParticleText extends Particle {

	private final int color;
	private final String text;

	private float rotationYaw;
	private float rotationPitch;

	public ParticleText(World world, double x, double y, double z, int color, String text) {
		super(world, x, y, z);
		this.particleMaxAge = 100;
		this.color = color;
		this.text = text;

		this.motionY = 0.01D;
		// the old noClip = true
		this.canCollide = false;

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
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.0F);
		RenderHelper.disableStandardItemLighting();

		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRenderer;

		// the text must always face the eye, so the current player's view angles are copied straight
		this.rotationYaw = -mc.player.rotationYaw;
		this.rotationPitch = mc.player.rotationPitch;

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		GlStateManager.translate(pX, pY, pZ);
		GlStateManager.rotate(this.rotationYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(this.rotationPitch, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);

		// the font is drawn in GUI pixels, so it scales down to a world-appropriate size first
		GlStateManager.scale(this.particleScale * 0.01, this.particleScale * 0.01, this.particleScale * 0.01);

		font.drawStringWithShadow(this.text, -(int) (font.getStringWidth(this.text) * 0.5F), -(int) (font.FONT_HEIGHT * 0.5F), this.color);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}
}
