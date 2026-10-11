package com.hbm.particle;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The foundry's poured metal stream: a bent square column pulled from the spout, spreading on landing.
 *
 * Self-drawn (layer 3), and **only** self-drawn: it disables BLEND and CULL_FACE,
 * and scrolls texture V by system time (add) with UV leaving 0..1 — out-of-range sampling in an atlas bleeds into neighbor sprites,
 * so it must bind textures/blocks/lava_gray.png itself, never the ParticleSprites atlas path.
 * Brightness is the old setBrightness(240) ⇒ block light 240, sky light 0.
 *
 * Two mandatory relocations, neither changing geometry:
 *   · ForgeDirection → EnumFacing, and **the direction argument means an ordinal**: the old getOrientation(ord)
 *     had DOWN=1...EAST=6, while this version's getFront(ord) is DOWN=0...EAST=5 — off by one across the board ⇒ callers (the foundry machine)
 *     just pass EnumFacing.ordinal(), no manual +1.
 *   · the old rot = dir.getRotation(UP) is synonymous with this version's dir.rotateYCCW() (verified: NORTH→WEST in its table);
 *     the horizontal components offsetX/offsetZ become getFrontOffsetX/Z().
 * The camera baseline became absolute world coordinates (the engine stack is already translated by -viewerPos); the old player-interpolation math is retired.
 */
@SideOnly(Side.CLIENT)
public class ParticleFoundry extends Particle {

	public static final ResourceLocation lava = new ResourceLocation(RefStrings.MODID + ":textures/blocks/lava_gray.png");

	protected int color;
	protected EnumFacing dir;
	/* How far the metal stream runs down from the spout */
	protected double length;
	/* The section just out of the mouth, above or beside the base point */
	protected double base;
	/* How far the bottom section reaches back */
	protected double offset;

	private final TextureManager theRenderEngine;

	public ParticleFoundry(TextureManager texman, World world, double x, double y, double z, int color, int direction, double length, double base, double offset) {
		super(world, x, y, z);
		this.color = color;
		this.dir = EnumFacing.getFront(direction);
		this.theRenderEngine = texman;
		this.length = length;
		this.base = base;
		this.offset = offset;

		this.particleMaxAge = 20;
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

		float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partial);
		float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partial);
		float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partial);

		EnumFacing rot = this.dir.rotateYCCW();
		double width = 0.0625 + ((this.particleAge + partial) / this.particleMaxAge) * 0.0625;
		double girth = 0.125 * (1 - ((this.particleAge + partial) / this.particleMaxAge));

		Color tint = new Color(this.color).brighter();

		// pulled 70% toward white, or the pour reads as a lump of pig iron
		double brightener = 0.7D;

		int r = (int) (255D - (255D - tint.getRed()) * brightener);
		int g = (int) (255D - (255D - tint.getGreen()) * brightener);
		int b = (int) (255D - (255D - tint.getBlue()) * brightener);

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.translate(pX, pY, pZ);

		this.theRenderEngine.bindTexture(lava);

		// no blending set on this path, alpha goes nowhere; full color it is
		float red = r / 255F;
		float green = g / 255F;
		float blue = b / 255F;

		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);

		double dirXG = this.dir.getFrontOffsetX() * girth;
		double dirZG = this.dir.getFrontOffsetZ() * girth;
		double rotXW = rot.getFrontOffsetX() * width;
		double rotZW = rot.getFrontOffsetZ() * width;

		double uMin = 0.5 - width;
		double uMax = 0.5 + width;
		double vMin = 0;
		double vMax = length;

		// scroll amount: the whole column's texture crawls downward with time, or it won't read as flowing
		double add = (int)(System.currentTimeMillis() / 100 % 16) / 16D;

		// lower half · back face
		quad(buffer, rotXW, girth, rotZW, uMax, vMax + add + girth, red, green, blue);
		quad(buffer, -rotXW, girth, -rotZW, uMin, vMax + add + girth, red, green, blue);
		quad(buffer, -rotXW, -length, -rotZW, uMin, vMin + add, red, green, blue);
		quad(buffer, rotXW, -length, rotZW, uMax, vMin + add, red, green, blue);

		// lower half · front face
		quad(buffer, dirXG + rotXW, 0, dirZG + rotZW, uMax, vMax + add, red, green, blue);
		quad(buffer, dirXG - rotXW, 0, dirZG - rotZW, uMin, vMax + add, red, green, blue);
		quad(buffer, dirXG - rotXW, -length, dirZG - rotZW, uMin, vMin + add, red, green, blue);
		quad(buffer, dirXG + rotXW, -length, dirZG + rotZW, uMax, vMin + add, red, green, blue);

		double wMin = 0;
		double wMax = girth;

		// lower half · left face
		quad(buffer, rotXW, girth, rotZW, wMin, vMax + add + girth, red, green, blue);
		quad(buffer, dirXG + rotXW, 0, dirZG + rotZW, wMax, vMax + add, red, green, blue);
		quad(buffer, dirXG + rotXW, -length, dirZG + rotZW, wMax, vMin + add, red, green, blue);
		quad(buffer, rotXW, -length, rotZW, wMin, vMin + add, red, green, blue);

		// lower half · right face
		quad(buffer, -rotXW, girth, -rotZW, wMin, vMax + add + girth, red, green, blue);
		quad(buffer, dirXG - rotXW, 0, dirZG - rotZW, wMax, vMax + add, red, green, blue);
		quad(buffer, dirXG - rotXW, -length, dirZG - rotZW, wMax, vMin + add, red, green, blue);
		quad(buffer, -rotXW, -length, -rotZW, wMin, vMin + add, red, green, blue);

		double dirOX = this.dir.getFrontOffsetX() * offset;
		double dirOZ = this.dir.getFrontOffsetZ() * offset;

		vMax = offset;

		// upper half · back face
		quad(buffer, rotXW, 0, rotZW, uMax, vMax - add, red, green, blue);
		quad(buffer, -rotXW, 0, -rotZW, uMin, vMax - add, red, green, blue);
		quad(buffer, -rotXW - dirOX, base, -rotZW - dirOZ, uMin, vMin - add, red, green, blue);
		quad(buffer, rotXW - dirOX, base, rotZW - dirOZ, uMax, vMin - add, red, green, blue);

		// upper half · front face
		quad(buffer, rotXW, girth, rotZW, uMax, vMax - add + 0.25, red, green, blue);
		quad(buffer, -rotXW, girth, -rotZW, uMin, vMax - add + 0.25, red, green, blue);
		quad(buffer, -rotXW - dirOX, base + girth, -rotZW - dirOZ, uMin, vMin - add + 0.25, red, green, blue);
		quad(buffer, rotXW - dirOX, base + girth, rotZW - dirOZ, uMax, vMin - add + 0.25, red, green, blue);

		// upper half · left face
		quad(buffer, rotXW, 0, rotZW, wMax, vMax - add + 0.75, red, green, blue);
		quad(buffer, rotXW, girth, rotZW, wMin, vMax - add + 0.75, red, green, blue);
		quad(buffer, rotXW - dirOX, base + girth, rotZW - dirOZ, wMin, vMin - add + 0.75, red, green, blue);
		quad(buffer, rotXW - dirOX, base, rotZW - dirOZ, wMax, vMin - add + 0.75, red, green, blue);

		// upper half · right face
		quad(buffer, -rotXW, 0, -rotZW, wMax, vMax - add + 0.75, red, green, blue);
		quad(buffer, -rotXW, girth, -rotZW, wMin, vMax - add + 0.75, red, green, blue);
		quad(buffer, -rotXW - dirOX, base + girth, -rotZW - dirOZ, wMin, vMin - add + 0.75, red, green, blue);
		quad(buffer, -rotXW - dirOX, base, -rotZW - dirOZ, wMax, vMin - add + 0.75, red, green, blue);

		vMax = 0.125F;

		// the bend
		quad(buffer, dirXG + rotXW, 0, dirZG + rotZW, uMax, vMin + add + 0.75, red, green, blue);
		quad(buffer, dirXG - rotXW, 0, dirZG - rotZW, uMin, vMin + add + 0.75, red, green, blue);
		quad(buffer, -rotXW, girth, -rotZW, uMin, vMax + add + 0.75, red, green, blue);
		quad(buffer, rotXW, girth, rotZW, uMax, vMax + add + 0.75, red, green, blue);

		Tessellator.getInstance().draw();

		GlStateManager.color(1F, 1F, 1F);
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.popMatrix();
	}

	private void quad(BufferBuilder buffer, double x, double y, double z, double u, double v, float r, float g, float b) {
		buffer.pos(x, y, z).tex(u, v).lightmap(240, 0).color(r, g, b, 1F).endVertex();
	}
}
