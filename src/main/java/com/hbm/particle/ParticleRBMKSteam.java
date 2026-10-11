package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The RBMK steam jet: a 20-frame horizontal strip, alive for 10 ticks, pushed out of the vent at an angle.
 *
 * Self-drawn (layer 3), with the same additive blending and state toggles as ParticleRBMKFlame; in the old version the two were
 * one render block copied twice with different texture/frame order/quad shape — the port keeps both copies for easy 1.7.10 diffing.
 *
 * The frame math differs from the flame tier: `(age/maxAge)*20 % 20 - 1`, and a -1 lets U drift slightly past the cell's left edge —
 * that's how the old version was, unclamped, copied unchanged. Only the flame tier fades in/out; alpha is fixed at the constructor's 0.25 here.
 */
@SideOnly(Side.CLIENT)
public class ParticleRBMKSteam extends Particle {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/rbmk_jet_steam.png");

	private final TextureManager theRenderEngine;

	public ParticleRBMKSteam(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 10;
		this.particleAlpha = 0.25F;
		this.particleScale = 4F;
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		this.theRenderEngine.bindTexture(getTexture());

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderHelper.disableStandardItemLighting();

		if(this.particleAge > this.particleMaxAge) this.particleAge = this.particleMaxAge;

		int texIndex = (int) (((double) this.particleAge / (double) this.particleMaxAge) * 20) % 20 - 1;
		float f0 = 1F / 20F;

		float uMin = texIndex * f0;
		float uMax = uMin + f0;
		float vMin = 0;
		float vMax = 1;

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		GlStateManager.translate(pX + rotX, pY + rotZ, pZ + rotYZ);
		GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		vertex(buffer, this.particleScale * -0.25 - 0.9375, -0.25, 0, uMax, vMax);
		vertex(buffer, this.particleScale * -0.25 - 0.9375, this.particleScale - 0.25, 0, uMax, vMin);
		vertex(buffer, this.particleScale * 0.25 - 0.9375, this.particleScale - 0.25, 0, uMin, vMin);
		vertex(buffer, this.particleScale * 0.25 - 0.9375, -0.25, 0, uMin, vMax);
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.popMatrix();

		if(fog) GlStateManager.enableFog();
	}

	// the texture enters through this door (as the old version wrote it)
	protected ResourceLocation getTexture() {
		return texture;
	}

	private void vertex(BufferBuilder buffer, double x, double y, double z, double u, double v) {
		buffer.pos(x, y, z).tex(u, v).lightmap(240, 0).color(1F, 1F, 1F, this.particleAlpha).endVertex();
	}
}
