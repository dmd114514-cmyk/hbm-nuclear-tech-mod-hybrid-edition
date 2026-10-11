package com.hbm.particle;

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
 * A debug line, dimming with age.
 * Self-drawn (layer 3): line segments don't fit the ParticleManager's quad shared buffer,
 * so it begins/draws itself, drawn through HbmParticleEngine's channel. Glow rides POSITION_TEX_LMAP_COLOR's lightmap.
 */
@SideOnly(Side.CLIENT)
public class ParticleDebugLine extends Particle {

	int color;

	public ParticleDebugLine(World world, double x, double y, double z, double lx, double ly, double lz, int color) {
		super(world, x, y, z, lx, ly, lz);
		this.color = color;
		this.particleMaxAge = 60;
		HbmParticleEngine.add(this);
	}

	@Override
	public void onUpdate() {

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if(this.particleAge++ >= this.particleMaxAge) this.setExpired();
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entity, float partial, float rotX, float rotZ, float rotYZ, float rotXY, float rotXZ) {

		// The engine stack is already translated by -viewerPos; absolute world coordinates are mandatory here, or one more subtraction lands on the other side of the camera
		double pX = this.prevPosX + (this.posX - this.prevPosX) * (double) partial;
		double pY = this.prevPosY + (this.posY - this.prevPosY) * (double) partial;
		double pZ = this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial;

		double mX = pX + this.motionX;
		double mY = pY + this.motionY;
		double mZ = pZ + this.motionZ;

		GlStateManager.pushMatrix();
		GlStateManager.disableColorMaterial();
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.disableDepth();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		// dimmer with age; the old setBrightness(N) splits into block light N, sky light 0, hence the sky slot pinned at 0
		int brightness = (int) (240 - (240 * (this.particleAge + partial) / this.particleMaxAge));

		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		emit(buffer, pX, pY, pZ, brightness);
		emit(buffer, mX, mY, mZ, brightness);
		Tessellator.getInstance().draw();

		GlStateManager.enableColorMaterial();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}

	private void emit(BufferBuilder buffer, double x, double y, double z, int brightness) {
		buffer.pos(x, y, z).tex(0D, 0D).lightmap(brightness, 0)
			.color((this.color >> 16) & 0xFF, (this.color >> 8) & 0xFF, this.color & 0xFF, 255).endVertex();
	}
}
