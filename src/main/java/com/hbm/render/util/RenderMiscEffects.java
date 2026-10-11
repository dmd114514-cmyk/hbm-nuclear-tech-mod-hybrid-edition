package com.hbm.render.util;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * Enchantment glint. In 1.12.2 all GL state goes through GlStateManager,
 * but the texture matrix's matrixMode switch has no GlStateManager counterpart — the GL11 constant is still passed to GlStateManager.matrixMode.
 */
public class RenderMiscEffects {

	public static ResourceLocation glint = new ResourceLocation(RefStrings.MODID + ":textures/misc/glint.png");
	public static ResourceLocation glintBF = new ResourceLocation(RefStrings.MODID + ":textures/misc/glintBF.png");

	public static void renderClassicGlint(World world, float interpol, IModelCustomNamed model, String part, float colorMod, float r, float g, float b, float speed, float scale) {

		GlStateManager.pushMatrix();

		float offset = Minecraft.getMinecraft().player.ticksExisted + interpol;

		GlStateManager.enableBlend();
		GlStateManager.color(colorMod, colorMod, colorMod, 1.0F);
		GlStateManager.depthFunc(GL11.GL_EQUAL);
		GlStateManager.depthMask(false);

		for(int k = 0; k < 2; ++k) {

			GlStateManager.disableLighting();

			float glintColor = 0.76F;

			GlStateManager.color(r * glintColor, g * glintColor, b * glintColor, 1.0F);
			GlStateManager.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);

			// Scrolls the texture matrix; the same geometry drawn twice forms crossing glints
			GlStateManager.matrixMode(GL11.GL_TEXTURE);
			GlStateManager.loadIdentity();

			float movement = offset * (0.001F + (float) k * 0.003F) * speed;

			GlStateManager.scale(scale, scale, scale);
			GlStateManager.rotate(30.0F - (float) k * 60.0F, 0.0F, 0.0F, 1.0F);
			GlStateManager.translate(0.0F, movement, 0.0F);

			GlStateManager.matrixMode(GL11.GL_MODELVIEW);

			if("all".equals(part)) {
				model.renderAll();
			} else {
				model.renderPart(part);
			}
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.depthMask(true);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.depthFunc(GL11.GL_LEQUAL);

		GlStateManager.popMatrix();
	}

	public static void renderClassicGlint(World world, float interpol, IModelCustomNamed model, String part, float r, float g, float b, float speed, float scale) {
		renderClassicGlint(world, interpol, model, part, 0.5F, r, g, b, speed, scale);
	}

	public static void renderClassicGlint(World world, float interpol, IModelCustomNamed model, String part) {
		renderClassicGlint(world, interpol, model, part, 0.5F, 0.25F, 0.8F, 20.0F, 1F / 3F);
	}
}
