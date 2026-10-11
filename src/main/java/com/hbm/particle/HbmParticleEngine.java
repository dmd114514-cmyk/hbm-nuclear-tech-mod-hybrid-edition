package com.hbm.particle;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The drawing channel for self-drawn particles.
 *
 * Why not leave it to ParticleManager: every layer is a uniform `begin(GL_QUADS, PARTICLE_POSITION_TEX_COLOR_LMAP)`
 * then a uniform draw, all particles sharing one buffer with blending/depth/texture pinned. Half of HBM's particles want
 * custom additive blending, depth writes off, and their own textures (measured: 25 of the 52 particle files are that kind).
 *
 * Those particles return 3 from getFXLayer(): 1.12.2's ParticleManager iterates 4 layers in updateEffectLayer
 * but renderParticles draws only the first 3, so layer 3 is exactly "tick only, never drawn" — matching the old RenderGlobal's
 * third render pass semantics. So the lifecycle stays vanilla while drawing is taken over here; particles keep binding their own textures
 * and running their own begin/draw.
 *
 * Draw timing is RenderWorldLastEvent (the world render's last pass). Vertices use absolute world coordinates, so the whole stack translates to the camera origin.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = "hbm", value = Side.CLIENT)
public class HbmParticleEngine {

	private static final List<Particle> custom = new ArrayList<Particle>();

	public static void add(Particle particle) {
		custom.add(particle);
	}

	@SubscribeEvent
	public static void onRenderWorldLast(RenderWorldLastEvent event) {

		if(custom.isEmpty()) return;

		Minecraft mc = Minecraft.getMinecraft();
		Entity view = mc.getRenderViewEntity();

		if(view == null) {
			custom.clear();
			return;
		}

		RenderManager rm = mc.getRenderManager();
		float partial = event.getPartialTicks();

		float rotX = ActiveRenderInfo.getRotationX();
		float rotZ = ActiveRenderInfo.getRotationZ();
		float rotYZ = ActiveRenderInfo.getRotationYZ();
		float rotXY = ActiveRenderInfo.getRotationXY();
		float rotXZ = ActiveRenderInfo.getRotationXZ();

		GlStateManager.pushMatrix();
		GlStateManager.translate(-rm.viewerPosX, -rm.viewerPosY, -rm.viewerPosZ);

		// iterated in reverse, pruning finished particles along the way
		for(int i = custom.size() - 1; i >= 0; i--) {
			Particle particle = custom.get(i);

			if(!particle.isAlive()) {
				custom.remove(i);
				continue;
			}

			particle.renderParticle(Tessellator.getInstance().getBuffer(), view, partial, rotX, rotZ, rotYZ, rotXY, rotXZ);
		}

		GlStateManager.popMatrix();

		// this pass runs last, but state still goes back, or the next frame's HUD and everything after suffers
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.depthMask(true);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.color(1F, 1F, 1F, 1F);
	}
}
