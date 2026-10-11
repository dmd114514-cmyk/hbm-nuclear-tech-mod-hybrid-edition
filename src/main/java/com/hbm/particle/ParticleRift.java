package com.hbm.particle;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The rift: a ring of inverted-color shells, swelling with age.
 *
 * Stays self-drawn (layer 3): its blending is the inverting ONE_MINUS_DST_COLOR / ONE_MINUS_SRC_COLOR mode,
 * plus textures off, culling on, and a 3D mesh sphere — beyond the shared buffer's GL_QUADS + atlas + fixed blend.
 *
 * The old version bound hadron.png on entry, but immediately disabled GL_TEXTURE_2D before drawing the sphere,
 * so that texture never actually participated; its real user is ParticleHadron.
 *
 * The sphere center uses absolute world coordinates: the engine stack is already translated by -viewerPos, so don't subtract the camera again.
 * The old ending never re-enabled GL_CULL_FACE, leaking the cull state downstream; restored per the same batch's convention.
 */
@SideOnly(Side.CLIENT)
public class ParticleRift extends Particle {

	/*The outer four shells' radius multiplier over the previous layer, growing outward for that thick inverted edge */
	private static final float[] SHELL_STEPS = { 1.02F, 1.05F, 1.02F, 1.05F };

	private static final String SPHERE = "models/sphere_uv.obj";

	public ParticleRift(TextureManager texman, World world, double x, double y, double z) {
		super(world, x, y, z);
		this.particleMaxAge = 10;
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.depthMask(false);
		OpenGlHelper.glBlendFunc(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
		RenderHelper.disableStandardItemLighting();

		double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) partial;
		double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) partial;
		double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial;

		float scale = (this.particleAge + partial) * 0.5F;

		IModelCustomNamed sphere = ResourceManager.getModel(SPHERE);

		GlStateManager.disableTexture2D();

		GlStateManager.pushMatrix();
		GlStateManager.translate(pX, pY, pZ);
		GlStateManager.enableCull();

		// the multipliers compound, matching the old glScalef chain word for word: 1, then 1.02, then 1.05...
		GlStateManager.scale(scale, scale, scale);
		sphere.renderAll();

		for(float step : SHELL_STEPS) {
			GlStateManager.scale(step, step, step);
			sphere.renderAll();
		}

		GlStateManager.popMatrix();

		GlStateManager.enableTexture2D();
		GlStateManager.disableCull();
		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
	}
}
