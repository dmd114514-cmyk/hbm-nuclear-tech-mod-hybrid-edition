package com.hbm.render.util;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

/**
 * The Soyuz launch pad draw door: six models each with their own texture assembled together; the tower and support arm rotate about the horizontal axis by the passed angle.
 *
 * Shape changes: models moved from old static fields (noSmooth + asVBO) to ResourceManager.getModel (lazy + cached),
 * drawn smooth-shaded across the board this version — the old faceted look will differ slightly, noted;
 * GL through GlStateManager except texture management; the old glTranslated/glRotated double-precision doors use the float versions here.
 */
public class SoyuzLauncherPronter {

	private static final String LEGS = "models/launch_table/soyuz_launcher_legs.obj";
	private static final String TABLE = "models/launch_table/soyuz_launcher_table.obj";
	private static final String TOWER_BASE = "models/launch_table/soyuz_launcher_tower_base.obj";
	private static final String TOWER = "models/launch_table/soyuz_launcher_tower.obj";
	private static final String SUPPORT_BASE = "models/launch_table/soyuz_launcher_support_base.obj";
	private static final String SUPPORT = "models/launch_table/soyuz_launcher_support.obj";

	private static final ResourceLocation LEGS_TEX = tex("launcher_leg");
	private static final ResourceLocation TABLE_TEX = tex("launcher_table");
	private static final ResourceLocation TOWER_BASE_TEX = tex("launcher_tower_base");
	private static final ResourceLocation TOWER_TEX = tex("launcher_tower");
	private static final ResourceLocation SUPPORT_BASE_TEX = tex("launcher_support_base");
	private static final ResourceLocation SUPPORT_TEX = tex("launcher_support");

	private static ResourceLocation tex(String name) {
		return new ResourceLocation(RefStrings.MODID, "textures/models/soyuz_launcher/" + name + ".png");
	}

	public static void prontLauncher(double rot) {

		GlStateManager.pushMatrix();
		GlStateManager.disableCull();

		TextureManager tex = Minecraft.getMinecraft().getTextureManager();

		tex.bindTexture(LEGS_TEX);
		ResourceManager.getModel(LEGS).renderAll();

		tex.bindTexture(TABLE_TEX);
		ResourceManager.getModel(TABLE).renderAll();

		tex.bindTexture(TOWER_BASE_TEX);
		ResourceManager.getModel(TOWER_BASE).renderAll();

		GlStateManager.pushMatrix();
		tex.bindTexture(TOWER_TEX);
		GlStateManager.translate(0F, 5.5F, 5.5F);
		GlStateManager.rotate((float) rot, 1F, 0F, 0F);
		GlStateManager.translate(0F, -5.5F, -5.5F);
		ResourceManager.getModel(TOWER).renderAll();
		GlStateManager.popMatrix();

		tex.bindTexture(SUPPORT_BASE_TEX);
		ResourceManager.getModel(SUPPORT_BASE).renderAll();

		GlStateManager.pushMatrix();
		tex.bindTexture(SUPPORT_TEX);
		GlStateManager.translate(0F, 5.5F, -6.5F);
		GlStateManager.rotate((float) rot, -1F, 0F, 0F);
		GlStateManager.translate(0F, -5.5F, 6.5F);
		ResourceManager.getModel(SUPPORT).renderAll();
		GlStateManager.popMatrix();

		GlStateManager.enableCull();
		GlStateManager.popMatrix();
	}
}
