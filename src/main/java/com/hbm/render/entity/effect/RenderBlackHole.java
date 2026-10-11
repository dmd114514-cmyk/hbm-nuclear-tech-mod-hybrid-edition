package com.hbm.render.entity.effect;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.effect.EntityBlackHole;
import com.hbm.entity.effect.EntityRagingVortex;
import com.hbm.entity.effect.EntityVortex;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.util.ColorUtil;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

/**
 * The black hole family's drawing: a black sphere, dressed by type — the plain black hole wears an accretion disk + two jets,
 * the vortex a ring of swirl marks, the raging vortex swirl marks + jets.
 *
 * What changed in the move is this version's shape: quads/fans rewritten as begin/pos/tex/color/endVertex
 * (1.7's "setColor once, then addVertex" was stateful; every vertex must be given its color explicitly now,
 * or that vertex reads stale color bytes left in the buffer); GL through GlStateManager;
 * models go through ResourceManager.getModel; the immutable Vec3d means rotateYaw's return value must be caught
 * — the old version rotated in place, so this version must catch the new vector at every turn, with the turn count and angles unchanged to the last one.
 *
 * The old renderFlare didn't come over: nothing in the tree calls it but itself (the other same-named one lives in a different package),
 * and moving it in would just be dead code that draws nothing.
 *
 * Size reads the entity's own getSize(), no more digging through the watcher by raw index.
 */
public class RenderBlackHole extends Render<EntityBlackHole> {

	protected static final String MODEL = "models/Sphere.obj";

	protected ResourceLocation hole = new ResourceLocation(RefStrings.MODID, "textures/models/BlackHole.png");
	protected ResourceLocation swirl = new ResourceLocation(RefStrings.MODID, "textures/entity/bhole.png");
	protected ResourceLocation disc = new ResourceLocation(RefStrings.MODID, "textures/entity/bholeDisc.png");

	public RenderBlackHole(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(EntityBlackHole entity, double x, double y, double z, float entityYaw, float partialTicks) {

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.disableLighting();
		GlStateManager.disableCull();

		float size = entity.getSize();

		GlStateManager.scale(size, size, size);

		this.bindTexture(hole);
		ResourceManager.getModel(MODEL).renderAll();

		if(entity instanceof EntityVortex) {
			this.renderSwirl(entity, partialTicks);

		} else if(entity instanceof EntityRagingVortex) {
			this.renderSwirl(entity, partialTicks);
			this.renderJets(entity, partialTicks);

		} else {
			this.renderDisc(entity, partialTicks);
			this.renderJets(entity, partialTicks);
		}

		GlStateManager.enableCull();
		GlStateManager.enableLighting();

		GlStateManager.popMatrix();
	}

	protected ResourceLocation discTex() {
		return this.disc;
	}

	protected void renderDisc(EntityBlackHole entity, float interp) {

		float glow = 0.75F;

		this.bindTexture(this.discTex());

		GlStateManager.pushMatrix();
		GlStateManager.rotate(entity.getEntityId() % 90 - 45, 1F, 0F, 0F);
		GlStateManager.rotate(entity.getEntityId() % 360, 0F, 1F, 0F);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.0F);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		int count = 16;

		Vec3d vec = new Vec3d(1, 0, 0);

		for(int k = 0; k < steps(); k++) {

			GlStateManager.pushMatrix();
			GlStateManager.rotate((entity.ticksExisted + interp % 360) * -((float) Math.pow(k + 1, 1.25)), 0F, 1F, 0F);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			double s = 3 - k * 0.175D;

			for(int j = 0; j < 2; j++) {

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

				for(int i = 0; i < count; i++) {

					if(j == 0)
						this.setColorFromIteration(buffer, k, 1F);
					else
						buffer.color(1.0F, 1.0F, 1.0F, glow);

					buffer.pos(vec.x * s, 0, vec.z * s).tex(0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25).endVertex();

					this.setColorFromIteration(buffer, k, 0F);
					buffer.pos(vec.x * s * 2, 0, vec.z * s * 2).tex(0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5).endVertex();

					vec = vec.rotateYaw((float) (Math.PI * 2 / count));

					this.setColorFromIteration(buffer, k, 0F);
					buffer.pos(vec.x * s * 2, 0, vec.z * s * 2).tex(0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5).endVertex();

					if(j == 0)
						this.setColorFromIteration(buffer, k, 1F);
					else
						buffer.color(1.0F, 1.0F, 1.0F, glow);

					buffer.pos(vec.x * s, 0, vec.z * s).tex(0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25).endVertex();
				}

				tess.draw();

				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
			}

			GlStateManager.popMatrix();
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}

	protected int steps() {
		return 15;
	}

	protected void setColorFromIteration(BufferBuilder buffer, int iteration, float alpha) {

		if(iteration < 5) {
			float g = 0.125F + iteration * (1F / 10F);
			buffer.color(1.0F, g, 0.0F, alpha);
			return;
		}

		if(iteration == 5) {
			buffer.color(1.0F, 1.0F, 0.0F, alpha);
			return;
		}

		if(iteration > 5) {

			int i = iteration - 6;
			float r = 1.0F - i * (1F / 9F);
			float g = 1F - i * (1F / 9F);
			float b = i * (1F / 5F);
			buffer.color(r, g, b, alpha);
		}
	}

	protected void renderSwirl(EntityBlackHole entity, float interp) {

		float glow = 0.75F;

		if(entity instanceof EntityRagingVortex)
			glow = 0.25F;

		this.bindTexture(swirl);

		GlStateManager.pushMatrix();
		GlStateManager.rotate(entity.getEntityId() % 90 - 45, 1F, 0F, 0F);
		GlStateManager.rotate(entity.getEntityId() % 360, 0F, 1F, 0F);
		GlStateManager.rotate((entity.ticksExisted + interp % 360) * -5, 0F, 1F, 0F);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.0F);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		Vec3d vec = new Vec3d(1, 0, 0);

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		double s = 3;
		int count = 16;

		// Inner ring (solid)
		for(int j = 0; j < 2; j++) {

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			for(int i = 0; i < count; i++) {

				buffer.color(0.0F, 0.0F, 0.0F, 1.0F);
				buffer.pos(vec.x * 0.9, 0, vec.z * 0.9).tex(0.5 + vec.x * 0.25 / s * 0.9, 0.5 + vec.z * 0.25 / s * 0.9).endVertex();

				if(j == 0)
					this.setColorFull(entity, buffer);
				else
					buffer.color(1.0F, 1.0F, 1.0F, glow);

				buffer.pos(vec.x * s, 0, vec.z * s).tex(0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25).endVertex();

				vec = vec.rotateYaw((float) (Math.PI * 2 / count));

				if(j == 0)
					this.setColorFull(entity, buffer);
				else
					buffer.color(1.0F, 1.0F, 1.0F, glow);

				buffer.pos(vec.x * s, 0, vec.z * s).tex(0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25).endVertex();

				buffer.color(0.0F, 0.0F, 0.0F, 1.0F);
				buffer.pos(vec.x * 0.9, 0, vec.z * 0.9).tex(0.5 + vec.x * 0.25 / s * 0.9, 0.5 + vec.z * 0.25 / s * 0.9).endVertex();
			}

			tess.draw();

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		}

		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		// Outer ring (fading out)
		for(int j = 0; j < 2; j++) {

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);

			for(int i = 0; i < count; i++) {

				if(j == 0)
					this.setColorFull(entity, buffer);
				else
					buffer.color(1.0F, 1.0F, 1.0F, glow);

				buffer.pos(vec.x * s, 0, vec.z * s).tex(0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25).endVertex();

				this.setColorNone(entity, buffer);
				buffer.pos(vec.x * s * 2, 0, vec.z * s * 2).tex(0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5).endVertex();

				vec = vec.rotateYaw((float) (Math.PI * 2 / count));

				this.setColorNone(entity, buffer);
				buffer.pos(vec.x * s * 2, 0, vec.z * s * 2).tex(0.5 + vec.x * 0.5, 0.5 + vec.z * 0.5).endVertex();

				if(j == 0)
					this.setColorFull(entity, buffer);
				else
					buffer.color(1.0F, 1.0F, 1.0F, glow);

				buffer.pos(vec.x * s, 0, vec.z * s).tex(0.5 + vec.x * 0.25, 0.5 + vec.z * 0.25).endVertex();
			}

			tess.draw();

			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		}

		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();

		GlStateManager.popMatrix();
	}

	protected void renderJets(EntityBlackHole entity, float interp) {

		Tessellator tess = Tessellator.getInstance();
		BufferBuilder buffer = tess.getBuffer();

		GlStateManager.pushMatrix();
		GlStateManager.rotate(entity.getEntityId() % 90 - 45, 1F, 0F, 0F);
		GlStateManager.rotate(entity.getEntityId() % 360, 0F, 1F, 0F);

		GlStateManager.disableAlpha();
		GlStateManager.depthMask(false);
		GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture2D();

		for(int j = -1; j <= 1; j += 2) {

			buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

			buffer.color(1.0F, 1.0F, 1.0F, 0.35F);
			buffer.pos(0, 0, 0).endVertex();

			// The old version set the color once and every later vertex inherited it (color 0); this version gives the same color per vertex
			buffer.color(1.0F, 1.0F, 1.0F, 0.0F);

			Vec3d jet = new Vec3d(0.5, 0, 0);

			for(int i = 0; i <= 12; i++) {

				buffer.pos(jet.x, 10 * j, jet.z).endVertex();
				jet = jet.rotateYaw((float) (Math.PI / 6 * -j));
			}

			tess.draw();
		}

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.depthMask(true);
		GlStateManager.enableAlpha();
		GlStateManager.popMatrix();
	}

	protected void setColorFull(Entity e, BufferBuilder buffer) {

		if(e instanceof EntityVortex)
			this.colorInt(buffer, 0x3898b3, 1F);

		else if(e instanceof EntityRagingVortex)
			this.colorInt(buffer, 0xe8390d, 1F);

		else
			this.colorInt(buffer, 0xFFB900, 1F);
	}

	protected void setColorNone(Entity e, BufferBuilder buffer) {

		if(e instanceof EntityVortex)
			this.colorInt(buffer, 0x3898b3, 0F);

		else if(e instanceof EntityRagingVortex)
			this.colorInt(buffer, 0xe8390d, 0F);

		else
			this.colorInt(buffer, 0xFFB900, 0F);
	}

	/** The equivalent of the old setColorRGBA_I: an integer color split into three 0..1 components */
	protected void colorInt(BufferBuilder buffer, int color, float alpha) {
		buffer.color(ColorUtil.fr(color), ColorUtil.fg(color), ColorUtil.fb(color), alpha);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlackHole entity) {
		return null;
	}
}
