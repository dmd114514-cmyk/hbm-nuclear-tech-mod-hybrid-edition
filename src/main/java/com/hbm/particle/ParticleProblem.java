package com.hbm.particle;

import java.util.Random;

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
 * A green glow drifting with an entity, marking "something's wrong here".
 *
 * Self-drawn channel (layer 3): binds one whole image, no atlas, plus lights off and depth writes off,
 * beyond the shared buffer's fixed format and fixed blend.
 *
 * The jitter uses a per-frame new Random(50): fixed seed, so the offsets are identical every frame,
 * reading as one steady blob rather than snow — don't swap in this.rand, it starts flickering immediately.
 *
 * The texture path moved to textures/particle/: the old one said textures/entity/, but that image never existed in that directory in the asset pack,
 * meaning the old particle always drew the missing texture.
 */
@SideOnly(Side.CLIENT)
public class ParticleProblem extends Particle {

	private static final ResourceLocation TEXTURE = new ResourceLocation(RefStrings.MODID + ":textures/particle/theproblem.png");

	private final TextureManager renderEngine;
	private int age;
	private int maxAge;

	public ParticleProblem(TextureManager renderEngine, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.renderEngine = renderEngine;
		this.maxAge = 100 + this.rand.nextInt(40);

		this.particleRed = 0F;
		this.particleGreen = 0F;
		this.particleBlue = 0F;
		this.particleScale = 4.5F;

		HbmParticleEngine.add(this);
	}

	public ParticleProblem(TextureManager renderEngine, World world, double x, double y, double z, float red, float green, float blue, float scale) {
		super(world, x, y, z);
		this.renderEngine = renderEngine;
		this.maxAge = 100 + this.rand.nextInt(40);

		this.particleRed = red;
		this.particleGreen = green;
		this.particleBlue = blue;
		this.particleScale = scale;

		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		// The old version hard-coded "under 400 becomes 400", so the constructor's 100+rand never took effect; copied unchanged
		if(this.maxAge < 400) {
			this.maxAge = 400;
		}

		this.age++;

		if(this.age >= this.maxAge) {
			this.setExpired();
		}

		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if(this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float interp, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		this.renderEngine.bindTexture(TEXTURE);

		GlStateManager.color(0.85F, 0.9F, 0.5F, 1F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.depthMask(false);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		RenderHelper.disableStandardItemLighting();

		// the old tess.setBrightness(240) just set a global sample coordinate on the second texture stage (block light 240, sky light 0)
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 0F);

		Random rand = new Random(50);

		for(int i = 0; i < 1; i++) {

			double dX = (rand.nextGaussian() - 1D) * 2.5D;
			double dY = (rand.nextGaussian() - 1D) * 0.15D;
			double dZ = (rand.nextGaussian() - 1D) * 2.5D;
			double size = rand.nextDouble() * this.particleScale;

			GlStateManager.translate(dX, dY, dZ);

			// the engine stack is already translated to the camera origin; absolute world coordinates here, no extra interpPos subtraction
			double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) interp + rand.nextGaussian() * 0.5D;
			double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) interp + rand.nextGaussian() * 0.5D;
			double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) interp + rand.nextGaussian() * 0.5D;

			// the old setNormal(0,1,0) was for the unlit fixed pipeline; POSITION_TEX has no such element and the lights are off
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(pX - rotX * size - rotYZ * size, pY - rotZ * size, pZ - rotXY * size - rotXZ * size).tex(1D, 1D).endVertex();
			buffer.pos(pX - rotX * size + rotYZ * size, pY + rotZ * size, pZ - rotXY * size + rotXZ * size).tex(1D, 0D).endVertex();
			buffer.pos(pX + rotX * size + rotYZ * size, pY + rotZ * size, pZ + rotXY * size + rotXZ * size).tex(0D, 0D).endVertex();
			buffer.pos(pX + rotX * size - rotYZ * size, pY - rotZ * size, pZ + rotXY * size - rotXZ * size).tex(0D, 1D).endVertex();
			Tessellator.getInstance().draw();

			GlStateManager.translate(-dX, -dY, -dZ);
		}

		GlStateManager.disableRescaleNormal();
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.color(1F, 1F, 1F, 1F);
	}
}
