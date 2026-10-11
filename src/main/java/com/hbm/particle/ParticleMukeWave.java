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

/**
 * The shock wave: one ring texture, scaling up and fading over time.
 * Self-drawn (layer 3) — needs additive blending with depth writes off, beyond the ParticleManager's shared buffer.
 */
@SideOnly(Side.CLIENT)
public class ParticleMukeWave extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");
	private final TextureManager theRenderEngine;

	private float waveScale = 45F;

	public ParticleMukeWave(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 25;
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	public void setup(float scale, int maxAge) {
		this.waveScale = scale;
		this.particleMaxAge = maxAge;
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
		GlStateManager.disableCull();
		RenderHelper.disableStandardItemLighting();

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		this.particleAlpha = 1 - (((float) this.particleAge + partial) / (float) this.particleMaxAge);
		float scale = (1 - (float) Math.pow(Math.E, (this.particleAge + partial) * -0.125)) * waveScale;

		// the engine stack is already translated by -viewerPos; absolute world coordinates here
		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		// a flat plate hugging the ground, four corners lifted so it reads as a ring
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		emit(buffer, pX - scale, pY - 0.25, pZ - scale, 1, 1);
		emit(buffer, pX - scale, pY - 0.25, pZ + scale, 1, 0);
		emit(buffer, pX + scale, pY - 0.25, pZ + scale, 0, 0);
		emit(buffer, pX + scale, pY - 0.25, pZ - scale, 0, 1);
		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.enableCull();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		if(fog) GlStateManager.enableFog();
	}

	private void emit(BufferBuilder buffer, double x, double y, double z, double u, double v) {
		buffer.pos(x, y, z).tex(u, v).lightmap(240, 0)
			.color(1F, 1F, 1F, this.particleAlpha).endVertex();
	}
}
