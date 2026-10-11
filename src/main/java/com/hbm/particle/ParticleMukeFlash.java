package com.hbm.particle;

import java.util.Random;

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
 * The muke flash: the detonation instant's 24 flares; on tick 15 it spawns the whole mushroom cloud as a bonus.
 *
 * Self-drawn (layer 3): additive SRC_ALPHA/ONE + depth writes off + fog off; flare.png binds directly, not through the atlas.
 * The center takes absolute world coordinates (the engine stack is already translated by -viewerPos).
 *
 * The 24 sheets' offsets come from `setSeed(i * 31 + 1)`, unrelated to particle identity by design, so they're frame-stable and
 * need no stored seed like the others — kept exactly as the old version.
 * The old version left depth writes off at the end; restored per family convention here (blending too, so nothing downstream inherits it).
 */
@SideOnly(Side.CLIENT)
public class ParticleMukeFlash extends Particle {

	private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/flare.png");

	private static final int FLARES = 24;

	private final TextureManager theRenderEngine;

	boolean bf;

	public ParticleMukeFlash(TextureManager texman, World world, double x, double y, double z, boolean bf) {
		super(world, x, y, z);
		this.theRenderEngine = texman;
		this.particleMaxAge = 20;
		this.bf = bf;
		HbmParticleEngine.add(this);
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(this.particleAge != 15) return;

		// the column: stacked layer by layer
		for(double d = 0.0D; d <= 1.8D; d += 0.1) {
			double mx1 = this.rand.nextGaussian() * 0.05;
			double my1 = d + this.rand.nextGaussian() * 0.02;
			double mz1 = this.rand.nextGaussian() * 0.05;
			spawnCloud(mx1, my1, mz1);
		}

		// the ground-hugging ring
		for(int i = 0; i < 100; i++) {
			double mx2 = this.rand.nextGaussian() * 0.5;
			double my2 = this.rand.nextInt(5) == 0 ? 0.02 : 0;
			double mz2 = this.rand.nextGaussian() * 0.5;
			spawnCloud(mx2, my2, mz2);
		}

		// the cap: pressed lower the further off-axis
		for(int i = 0; i < 75; i++) {

			double x = this.rand.nextGaussian() * 0.5;
			double z = this.rand.nextGaussian() * 0.5;

			if(x * x + z * z > 1.5) {
				x *= 0.5;
				z *= 0.5;
			}

			double y = 1.8 + (this.rand.nextDouble() * 3 - 1.5) * (0.75 - (x * x + z * z)) * 0.5;

			spawnCloud(x, y + this.rand.nextGaussian() * 0.02, z);
		}
	}

	// the three components pass in the old draw order — don't swap: they're points on one random sequence, and the whole cloud shape changes
	private void spawnCloud(double mx, double my, double mz) {

		Particle cloud = this.bf
				? new ParticleMukeCloudBF(this.theRenderEngine, this.world, this.posX, this.posY, this.posZ, mx, my, mz)
				: new ParticleMukeCloud(this.theRenderEngine, this.world, this.posX, this.posY, this.posZ, mx, my, mz);

		Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		this.theRenderEngine.bindTexture(texture);

		boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
		if(fog) GlStateManager.disableFog();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		RenderHelper.disableStandardItemLighting();

		this.particleAlpha = 1 - (((float) this.particleAge + partial) / (float) this.particleMaxAge);
		float scale = (this.particleAge + partial) * 3F + 1F;

		float alpha = this.particleAlpha * 0.5F;

		double baseX = this.prevPosX + (this.posX - this.prevPosX) * (double) partial;
		double baseY = this.prevPosY + (this.posY - this.prevPosY) * (double) partial;
		double baseZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial;

		Random rand = new Random();

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

		for(int i = 0; i < FLARES; i++) {

			rand.setSeed(i * 31 + 1);

			float pX = (float) (baseX + rand.nextDouble() * 15 - 7.5);
			float pY = (float) (baseY + rand.nextDouble() * 7.5 - 3.75);
			float pZ = (float) (baseZ + rand.nextDouble() * 15 - 7.5);

			// ParticleSprites.emitQuad can't be used here: that path is for atlas sprites (remapping by the sprite's
			// min/max UV), while this particle binds flare.png itself, its UV being the whole image's 0~1
			vertex(buffer, pX - rotX * scale - rotXY * scale, pY - rotZ * scale, pZ - rotYZ * scale - rotXZ * scale, 1, 1, alpha);
			vertex(buffer, pX - rotX * scale + rotXY * scale, pY + rotZ * scale, pZ - rotYZ * scale + rotXZ * scale, 1, 0, alpha);
			vertex(buffer, pX + rotX * scale + rotXY * scale, pY + rotZ * scale, pZ + rotYZ * scale + rotXZ * scale, 0, 0, alpha);
			vertex(buffer, pX + rotX * scale - rotXY * scale, pY - rotZ * scale, pZ + rotYZ * scale - rotXZ * scale, 0, 1, alpha);
		}

		Tessellator.getInstance().draw();

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);

		if(fog) GlStateManager.enableFog();
	}

	private void vertex(BufferBuilder buffer, double x, double y, double z, double u, double v, float alpha) {
		// the old setBrightness(240) splits as block light 240, sky light 0
		buffer.pos(x, y, z).tex(u, v).lightmap(240, 0).color(1.0F, 0.9F, 0.75F, alpha).endVertex();
	}
}
