package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
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
 * The plasma blast: a ground-hugging wave ring, growing on an exponential curve while fading.
 *
 * Self-drawn (layer 3): additive SRC_ALPHA/ONE, depth writes off, culling off, plus pitch/facing correction —
 * beyond the shared buffer's fixed blend + GL_QUADS. The shockwave.png texture binds directly, not through the atlas.
 *
 * The rotationPitch/rotationYaw here are **genuinely used** (posing this wave), and since Particle isn't an Entity
 * and lacks both fields, it carries its own — don't confuse this with ParticleGiblet's "written but never read" dead pair.
 * The old setMaxAge is semantically identical to the parent's public same-named method here (both set particleMaxAge); the duplicate override is removed.
 * The center takes absolute world coordinates (the engine stack is already translated by -viewerPos); the old ending never restored culling — restored per family convention here.
 */
@SideOnly(Side.CLIENT)
public class ParticlePlasmaBlast extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/shockwave.png");

	private final TextureManager theRenderEngine;

	private float rotationPitch;
	private float rotationYaw;

	public ParticlePlasmaBlast(TextureManager texman, World world, double x, double y, double z, float r, float g, float b, float pitch, float yaw) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 20;
		this.particleRed = r;
		this.particleGreen = g;
		this.particleBlue = b;
		this.rotationPitch = pitch;
		this.rotationYaw = yaw;
		HbmParticleEngine.add(this);
	}

	public void setScale(float scale) {
		this.particleScale = scale;
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		this.theRenderEngine.bindTexture(texture);

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.disableCull();
		RenderHelper.disableStandardItemLighting();

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		GlStateManager.translate(pX, pY, pZ);
		GlStateManager.rotate(this.rotationYaw, 0, 1, 0);
		GlStateManager.rotate(this.rotationPitch, 1, 0, 0);

		this.particleAlpha = 1 - (((float) this.particleAge + partial) / (float) this.particleMaxAge);
		// fast-then-slow expansion; never linear — linear reads as a slab being shoved across
		float scale = (1 - (float) Math.pow(Math.E, (this.particleAge + partial) * -0.125)) * this.particleScale;

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		vertex(buffer, -scale, 0, -scale, 1, 1);
		vertex(buffer, -scale, 0, scale, 1, 0);
		vertex(buffer, scale, 0, scale, 0, 0);
		vertex(buffer, scale, 0, -scale, 0, 1);
		Tessellator.getInstance().draw();

		if(fog) GlStateManager.enableFog();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
		GlStateManager.depthMask(true);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.popMatrix();
	}

	private void vertex(BufferBuilder buffer, double x, double y, double z, double u, double v) {
		buffer.pos(x, y, z).tex(u, v).lightmap(240, 0).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex();
	}
}
