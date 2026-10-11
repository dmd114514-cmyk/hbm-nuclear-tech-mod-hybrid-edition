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
 * The RBMK mushroom: a 30-frame vertical strip, paging downward with age.
 * A "self-drawn" particle (custom additive blending + depth writes off + its own texture),
 * riding HbmParticleEngine's channel, hence keeping its own bindTexture + begin/draw writing.
 */
@SideOnly(Side.CLIENT)
public class ParticleRBMKMush extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/rbmk_mush.png");
	private final TextureManager theRenderEngine;
	private int age;
	private int maxAge;

	public ParticleRBMKMush(TextureManager texManager, World world, double x, double y, double z, float scale) {
		super(world, x, y, z);
		this.theRenderEngine = texManager;
		this.maxAge = 50;

		this.particleRed = this.particleGreen = this.particleBlue = 0;
		this.particleScale = scale;

		HbmParticleEngine.add(this);
	}

	public ParticleRBMKMush(TextureManager texManager, World world, double x, double y, double z, float red, float green, float blue, float scale) {
		super(world, x, y, z);
		this.theRenderEngine = texManager;
		this.maxAge = 50;

		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;
		this.particleScale = scale;

		HbmParticleEngine.add(this);
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		++this.age;

		if(this.age == this.maxAge) this.setExpired();
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		this.theRenderEngine.bindTexture(texture);

		int segs = 30;

		// one frame's share of the whole image
		double frame = 1D / (double) segs;
		// which frame we're on
		int prog = age * segs / maxAge;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0F);
		GlStateManager.depthMask(false);
		RenderHelper.disableStandardItemLighting();

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

		// the engine stack is already translated by -viewerPos; absolute world coordinates here
		float scale = this.particleScale;
		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial) + this.particleScale;
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		quad(buffer, pX - rotX * scale - rotXY * scale, pY - rotZ * scale, pZ - rotYZ * scale - rotXZ * scale, 1, (prog + 1) * frame);
		quad(buffer, pX - rotX * scale + rotXY * scale, pY + rotZ * scale, pZ - rotYZ * scale + rotXZ * scale, 1, prog * frame);
		quad(buffer, pX + rotX * scale + rotXY * scale, pY + rotZ * scale, pZ + rotYZ * scale + rotXZ * scale, 0, prog * frame);
		quad(buffer, pX + rotX * scale - rotXY * scale, pY - rotZ * scale, pZ + rotYZ * scale - rotXZ * scale, 0, (prog + 1) * frame);

		Tessellator.getInstance().draw();

		if(fog) GlStateManager.enableFog();
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.enableLighting();
	}

	private void quad(BufferBuilder buffer, double x, double y, double z, double u, double v) {
		buffer.pos(x, y, z).tex(u, v).color(1F, 1F, 1F, 1F).endVertex();
	}
}
