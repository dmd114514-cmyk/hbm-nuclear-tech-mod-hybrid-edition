package com.hbm.particle;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The spark: a segmented tracer path, bouncing once on the ground.
 *
 * Self-drawn (layer 3): it draws GL_LINE_STRIP segments with line width and textures off — the shared buffer only knows GL_QUADS.
 * Vertices take absolute world coordinates (the engine stack is already translated by -viewerPos).
 *
 * One equivalence per this version's abilities: the old `setBrightness(240) + setColorOpaque_I(0xffffff)` with
 * `glDisable(GL_TEXTURE_2D)` already in effect meant that brightness never reached a pixel (no texture to multiply), leaving pure white vertex color on screen,
 * so this uses POSITION_COLOR with per-vertex white, no lightmap slot.
 * The path's incremental writing (previous segment's endpoint + that step's velocity giving the next) is kept verbatim; don't refactor it into stored coordinates.
 */
@SideOnly(Side.CLIENT)
public class ParticleSpark extends Particle {

	private final List<double[]> steps = new ArrayList<double[]>();
	private int thresh;

	public ParticleSpark(World world, double x, double y, double z, double mX, double mY, double mZ) {
		super(world, x, y, z, mX, mY, mZ);
		this.thresh = 4 + this.rand.nextInt(3);
		this.steps.add(new double[] { this.motionX, this.motionY, this.motionZ });
		this.particleMaxAge = 20 + this.rand.nextInt(10);
		this.particleGravity = 0.5F;
		HbmParticleEngine.add(this);
	}

	public ParticleSpark makeSmall(boolean small) {
		if(!small) return this;
		this.setSize(0.01F, 0.01F);
		this.thresh = 3;
		this.particleMaxAge = 2 + this.rand.nextInt(3);
		this.motionY = -Math.abs(this.motionY);
		return this;
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) this.setExpired();

		this.steps.add(new double[] { this.motionX, this.motionY, this.motionZ });

		while(this.steps.size() > this.thresh) {
			this.steps.remove(0);
		}

		this.motionY -= 0.04D * (double) this.particleGravity;
		double lastY = this.motionY;
		this.move(this.motionX, this.motionY, this.motionZ);

		if(this.onGround) {
			this.onGround = false;
			this.motionY = -lastY * 0.8D;
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		if(this.steps.size() < 2) return;

		GlStateManager.pushMatrix();

		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.glLineWidth(3F);

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);

		double[] prev = new double[] { pX, pY, pZ };

		for(int i = 1; i < this.steps.size(); i++) {

			double[] curr = new double[] { prev[0] + this.steps.get(i)[0], prev[1] + this.steps.get(i)[1], prev[2] + this.steps.get(i)[2] };

			buffer.pos(prev[0], prev[1], prev[2]).color(1F, 1F, 1F, 1F).endVertex();

			prev = curr;
		}

		Tessellator.getInstance().draw();

		GlStateManager.enableLighting();
		GlStateManager.enableTexture2D();
		GlStateManager.popMatrix();
	}
}
