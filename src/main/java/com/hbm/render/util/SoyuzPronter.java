package com.hbm.render.util;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

/**
 * The Soyuz draw door: the vehicle draws in part groups (stack, boosters, capsule); the three liveries are just a different set of textures.
 *
 * Shape changes:
 *   · models moved from old static fields (asVBO) to ResourceManager.getModel (lazy + cached, likewise skipping noSmooth);
 *   · the three liveries were thirty-three explicit fields in the old version; with the path rule unified (textures/models/<livery dir>/<part>.png),
 *     they're generated in the enum by that one rule — every path checked against the old fields, every asset verified present;
 *   · GL through GlStateManager, all of it.
 */
public class SoyuzPronter {

	private static final String MODEL = "models/soyuz.obj";
	private static final String MODEL_MODULE = "models/soyuz_module.obj";

	private static final ResourceLocation TEX_MEMENTO = new ResourceLocation(RefStrings.MODID, "textures/items/polaroid_memento.png");
	private static final ResourceLocation TEX_MODULE_DOME = new ResourceLocation(RefStrings.MODID, "textures/models/soyuz_capsule/module_dome.png");
	private static final ResourceLocation TEX_MODULE_LANDER = new ResourceLocation(RefStrings.MODID, "textures/models/soyuz_capsule/module_lander.png");
	private static final ResourceLocation TEX_MODULE_PROPULSION = new ResourceLocation(RefStrings.MODID, "textures/models/soyuz_capsule/module_propulsion.png");
	private static final ResourceLocation TEX_MODULE_SOLAR = new ResourceLocation(RefStrings.MODID, "textures/models/soyuz_capsule/module_solar.png");

	public static enum SoyuzSkin {

		SOYUZ("soyuz"),
		LUNA("soyuz_luna"),
		AUTHENTIC("soyuz_authentic");

		public final ResourceLocation engineblock;
		public final ResourceLocation bottomstage;
		public final ResourceLocation topstage;
		public final ResourceLocation payload;
		public final ResourceLocation payloadblocks;
		public final ResourceLocation les;
		public final ResourceLocation lesthrusters;
		public final ResourceLocation mainengines;
		public final ResourceLocation sideengines;
		public final ResourceLocation booster;
		public final ResourceLocation boosterside;

		SoyuzSkin(String dir) {
			this.engineblock = part(dir, "engineblock");
			this.bottomstage = part(dir, "bottomstage");
			this.topstage = part(dir, "topstage");
			this.payload = part(dir, "payload");
			this.payloadblocks = part(dir, "payloadblocks");
			this.les = part(dir, "les");
			this.lesthrusters = part(dir, "lesthrusters");
			this.mainengines = part(dir, "mainengines");
			this.sideengines = part(dir, "sideengines");
			this.booster = part(dir, "booster");
			this.boosterside = part(dir, "boosterside");
		}

		private static ResourceLocation part(String dir, String name) {
			return new ResourceLocation(RefStrings.MODID, "textures/models/" + dir + "/" + name + ".png");
		}
	}

	public static void prontSoyuz(int type) {

		if(type >= SoyuzSkin.values().length || type < 0)
			return;

		prontMain(type);
		prontBoosters(type);
	}

	public static void prontMain(int type) {

		SoyuzSkin skin = SoyuzSkin.values()[type];

		GlStateManager.pushMatrix();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		TextureManager tex = Minecraft.getMinecraft().getTextureManager();
		IModelCustomNamed model = ResourceManager.getModel(MODEL);

		tex.bindTexture(skin.engineblock);
		model.renderOnly("EngineBlock");

		tex.bindTexture(skin.bottomstage);
		model.renderOnly("BottomStage");

		tex.bindTexture(skin.topstage);
		model.renderOnly("TopStage");

		tex.bindTexture(skin.payload);
		model.renderOnly("Payload");

		tex.bindTexture(TEX_MEMENTO);
		model.renderOnly("Memento");

		tex.bindTexture(skin.payloadblocks);
		model.renderOnly("PayloadBlocks");

		tex.bindTexture(skin.les);
		model.renderOnly("LES");

		tex.bindTexture(skin.lesthrusters);
		model.renderOnly("LESThrusters");

		tex.bindTexture(skin.mainengines);
		model.renderOnly("MainEngines");

		tex.bindTexture(skin.sideengines);
		model.renderOnly("SideEngines");

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	public static void prontBoosters(int type) {

		SoyuzSkin skin = SoyuzSkin.values()[type];

		GlStateManager.pushMatrix();
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		TextureManager tex = Minecraft.getMinecraft().getTextureManager();
		IModelCustomNamed model = ResourceManager.getModel(MODEL);

		tex.bindTexture(skin.booster);
		model.renderOnly("Booster.000");
		model.renderOnly("Booster.001");
		model.renderOnly("Booster.002");
		model.renderOnly("Booster.003");

		tex.bindTexture(skin.mainengines);
		model.renderOnly("BoosterEngines.000");
		model.renderOnly("BoosterEngines.001");
		model.renderOnly("BoosterEngines.002");
		model.renderOnly("BoosterEngines.003");

		tex.bindTexture(skin.boosterside);
		model.renderOnly("BoosterSide.000");
		model.renderOnly("BoosterSide.001");
		model.renderOnly("BoosterSide.002");
		model.renderOnly("BoosterSide.003");

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}

	public static void prontCapsule() {

		GlStateManager.pushMatrix();

		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.enableCull();
		GlStateManager.enableLighting();

		TextureManager tex = Minecraft.getMinecraft().getTextureManager();
		IModelCustomNamed model = ResourceManager.getModel(MODEL_MODULE);

		tex.bindTexture(TEX_MODULE_DOME);
		model.renderPart("Dome");
		tex.bindTexture(TEX_MODULE_LANDER);
		model.renderPart("Capsule");
		tex.bindTexture(TEX_MODULE_PROPULSION);
		model.renderPart("Propulsion");
		tex.bindTexture(TEX_MODULE_SOLAR);
		model.renderPart("Solar");

		GlStateManager.shadeModel(GL11.GL_FLAT);

		GlStateManager.popMatrix();
	}
}
