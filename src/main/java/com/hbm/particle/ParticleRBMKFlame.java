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
 * The RBMK flame: a 5×14 vertical strip, paging with age, fading in and out at both ends.
 *
 * Self-drawn (layer 3): additive SRC_ALPHA/ONE (glow), depth writes off, yaw-corrected toward the view, fog off —
 * beyond the shared buffer. The rbmk_fire.png texture binds directly, not through the atlas.
 *
 * Three writing deltas, all equivalent to the old version:
 *   · the old RenderManager.instance for playerViewY is gone (verified: RenderManager keeps only instance fields),
 *     replaced by Minecraft.getMinecraft().getRenderManager();
 *   · vertices take absolute world coordinates (the engine stack is already translated by -viewerPos); the old interpPos subtraction is dropped;
 *   · setBrightness(240) splits into per-vertex lightmap as block light 240, sky light 0 — not both components maxed.
 * The quad center is nudged by each component of the x/y/z basis vectors — the old version's offsets, copied.
 */
@SideOnly(Side.CLIENT)
public class ParticleRBMKFlame extends Particle {

	public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/rbmk_fire.png");

	private final TextureManager theRenderEngine;

	public ParticleRBMKFlame(TextureManager texman, World world, double x, double y, double z, int maxAge) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = maxAge;
		this.particleScale = this.rand.nextFloat() + 1F;
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

		// the strip pages vertically, U sliced by frame number
		int texIndex = this.particleAge * 5 % 14;
		float f0 = 1F / 14F;

		float uMin = texIndex % 5 * f0;
		float uMax = uMin + f0;
		float vMin = 0;
		float vMax = 1;

		this.particleAlpha = 1F;

		if(this.particleAge < 20) {
			this.particleAlpha = this.particleAge / 20F;
		}

		if(this.particleAge > this.particleMaxAge - 20) {
			this.particleAlpha = (this.particleMaxAge - this.particleAge) / 20F;
		}

		this.particleAlpha *= 0.5F;

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		GlStateManager.translate(pX + rotX, pY + rotZ, pZ + rotYZ);
		GlStateManager.rotate(-Minecraft.getMinecraft().getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		vertex(buffer, -this.particleScale - 1, -this.particleScale * 2, 0, uMax, vMax);
		vertex(buffer, -this.particleScale - 1, this.particleScale * 2, 0, uMax, vMin);
		vertex(buffer, this.particleScale - 1, this.particleScale * 2, 0, uMin, vMin);
		vertex(buffer, this.particleScale - 1, -this.particleScale * 2, 0, uMin, vMax);
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
