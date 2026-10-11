package com.hbm.render.entity.mob;

import org.lwjgl.opengl.GL11;

import com.hbm.entity.mob.EntityRADBeast;
import com.hbm.lib.RefStrings;
import com.hbm.render.model.ModelM65Blaze;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.BeamPronter.EnumBeamType;
import com.hbm.render.util.BeamPronter.EnumWaveType;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBlaze;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

/**
 * The RAD beast's look: the blaze skeleton with its own texture, a M65 mask layered on top, and a green beam drawn toward the entity it drags.
 *
 * Three convention changes:
 * - the old constructor's "ask ModelBlaze for an internal random and rebuild the model when it changes" has no hook in this version: here
 *   {@code ModelBlaze} only has the constructor plus render/setRotationAngles (checked against the decompile); the tendrils' motion is computed
 *   by itself per tick inside {@code setRotationAngles}, no external model swap needed ⇒ the whole block goes.
 * - the old mask layer used multi-pass ({@code shouldRenderPass} + {@code setRenderPassModel}); that hook is deleted in this version.
 *   Here: {@code renderModel} binds the mask texture and draws the mask model first, then calls super for the body — matching the old
 *   order of "mask on pass 0, body right after".
 * - the mask texture follows the spaceflight path under {@code textures/models/}, not the identically named image under {@code textures/armor/} that the sibling implementation uses.
 *   The beam interface takes a {@code Vec3d} here and the trailing alpha is a float: the old source wrote {@code 256} (int widened implicitly to 256.0F),
 *   not a typo — keep the original value, don't "fix" it to 1F.
 */
public class RenderRADBeast extends RenderLiving<EntityRADBeast> {

	private static final ResourceLocation RADBEAST = new ResourceLocation(RefStrings.MODID, "textures/entity/radbeast.png");
	private static final ResourceLocation MASK = new ResourceLocation(RefStrings.MODID, "textures/models/ModelM65Blaze.png");

	private ModelM65Blaze modelM65;

	public RenderRADBeast(RenderManager renderManager) {
		super(renderManager, new ModelBlaze(), 0.5F);
	}

	@Override
	public void doRender(EntityRADBeast entity, double x, double y, double z, float entityYaw, float partialTicks) {

		Entity victim = entity.getUnfortunateSoul();

		if(victim != null && entity.posY > 0.1) {

			GL11.glPushMatrix();
			GL11.glTranslated(x, y + 1.25, z);

			double sx = entity.posX;
			double sy = entity.posY + 1.25;
			double sz = entity.posZ;

			double tX = victim.posX;
			double tY = victim.posY + victim.height / 2;
			double tZ = victim.posZ;

			if(victim == Minecraft.getMinecraft().player)
				tY -= 1.5;

			double length = Math.sqrt(Math.pow(tX - sx, 2) + Math.pow(tY - sy, 2) + Math.pow(tZ - sz, 2));

			if(length < 200) {
				BeamPronter.prontBeam(new Vec3d(tX - sx, tY - sy, tZ - sz), EnumWaveType.RANDOM, EnumBeamType.SOLID,
						0x004000, 0x004000, (int) (entity.world.getTotalWorldTime() % 1000 + 1), (int) (length * 5),
						0.125F, 2, 0.03125F, 256F);
			}

			GL11.glPopMatrix();
		}

		super.doRender(entity, x, y, z, entityYaw, partialTicks);
	}

	@Override
	protected void renderModel(EntityRADBeast entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor) {

		this.bindTexture(MASK);

		if(this.modelM65 == null) {
			this.modelM65 = new ModelM65Blaze();
		}

		this.modelM65.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);

		super.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityRADBeast entity) {
		return RADBEAST;
	}
}
