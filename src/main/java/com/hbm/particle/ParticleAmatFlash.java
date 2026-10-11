package com.hbm.particle;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The antimatter blast's light spike: a hundred fan triangles in random orientations, bright at the center, transparent at the tips, fading with age.
 *
 * Self-drawn (layer 3): no textures, additive SRC_ALPHA/ONE, alpha test off, smooth shading on —
 * not one item of the shared buffer's set (atlas + GL_QUADS + fixed blend) matches.
 *
 * Unlike the particles that must bake accumulated displacement into coordinates, this one **can** keep the old incremental glRotatef writing:
 * each fan begins and draws on the spot, so it captures the matrix as it stands.
 * Vertex color/brightness was "current color" in the old version; here it must be per-vertex (the center alpha=inverse, tip alpha=0 gradient rides on it).
 * The camera baseline also became absolute world coordinates (the engine stack is already translated by -viewerPos); the old player-interpolation block is retired.
 * The old file also kept a whole commented-out 300-fan draft at the end; not ported.
 */
@SideOnly(Side.CLIENT)
public class ParticleAmatFlash extends Particle {

	private static final int SPIKES = 100;

	private static final long PATTERN_SEED = 432L;

	private final Random random = new Random(PATTERN_SEED);

	public ParticleAmatFlash(World world, double x, double y, double z, float scale) {
		super(world, x, y, z);
		this.particleMaxAge = 10;
		this.particleScale = scale;
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		GlStateManager.pushMatrix();
		GlStateManager.translate(pX, pY, pZ);
		GlStateManager.scale(0.2F * this.particleScale, 0.2F * this.particleScale, 0.2F * this.particleScale);

		double intensity = (double) (this.particleAge + partial) / (double) this.particleMaxAge;
		float inverse = (float) (1.0D - intensity);

		RenderHelper.disableStandardItemLighting();

		GlStateManager.disableTexture2D();
		// GlStateManager offers no smooth-shading door; raw GL11 it is
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GlStateManager.disableAlpha();
		GlStateManager.enableCull();
		GlStateManager.depthMask(false);

		GlStateManager.pushMatrix();

		// The old version newed Random(432) per frame, so the whole frame's orientation was fixed; equivalently the seed resets here and values draw in the original order
		random.setSeed(PATTERN_SEED);

		float scale = 0.5F;

		for(int i = 0; i < SPIKES; i++) {

			GlStateManager.rotate(this.random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(this.random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(this.random.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.rotate(this.random.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(this.random.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);

			float vert1 = (this.random.nextFloat() * 20.0F + 5.0F + 10.0F) * (float) (intensity * scale);
			float vert2 = (this.random.nextFloat() * 2.0F + 1.0F + 2.0F) * (float) (intensity * scale);

			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

			buffer.pos(0.0D, 0.0D, 0.0D).color(1.0F, 1.0F, 1.0F, inverse).endVertex();
			buffer.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(0.866D * vert2, vert1, -0.5F * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(0.0D, vert1, 1.0F * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();
			buffer.pos(-0.866D * vert2, vert1, -0.5F * vert2).color(1.0F, 1.0F, 1.0F, 0.0F).endVertex();

			Tessellator.getInstance().draw();
		}

		GlStateManager.popMatrix();
		GlStateManager.popMatrix();

		GlStateManager.depthMask(true);
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GL11.glShadeModel(GL11.GL_FLAT);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableTexture2D();
		GlStateManager.enableAlpha();
		RenderHelper.enableStandardItemLighting();
	}

}
