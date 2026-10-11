package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

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

/** The hadron burst: one glowing image scaling up while fading. Self-drawn (layer 3); additive blending under its own control */
@SideOnly(Side.CLIENT)
public class ParticleHadron extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/hadron.png");

	private final TextureManager theRenderEngine;

	public ParticleHadron(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 10;
		this.particleScale = 1F;
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	public ParticleHadron makeSmall(boolean small) {
		if(!small) return this;
		this.particleScale = 0.5F;
		this.particleMaxAge = 5;
		return this;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		this.theRenderEngine.bindTexture(texture);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderHelper.disableStandardItemLighting();

		this.particleAlpha = 1 - (((float) this.particleAge + partial) / (float) this.particleMaxAge);
		float scale = (this.particleAge + partial) * 0.15F * this.particleScale;

		// the engine stack is already translated by -viewerPos, so absolute world coordinates here; the old version hand-computed the player's interpolated position as camera baseline
		double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) partial;
		double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) partial;
		double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial;

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		vertex(buffer, pX - rotX * scale - rotXY * scale, pY - rotZ * scale, pZ - rotYZ * scale - rotXZ * scale, 1, 1);
		vertex(buffer, pX - rotX * scale + rotXY * scale, pY + rotZ * scale, pZ - rotYZ * scale + rotXZ * scale, 1, 0);
		vertex(buffer, pX + rotX * scale + rotXY * scale, pY + rotZ * scale, pZ + rotYZ * scale + rotXZ * scale, 0, 0);
		vertex(buffer, pX + rotX * scale - rotXY * scale, pY - rotZ * scale, pZ + rotYZ * scale - rotXZ * scale, 0, 1);
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
	}

	private void vertex(BufferBuilder buffer, double x, double y, double z, double u, double v) {
		buffer.pos(x, y, z).tex(u, v).lightmap(240, 0).color(1F, 1F, 1F, this.particleAlpha).endVertex();
	}
}
