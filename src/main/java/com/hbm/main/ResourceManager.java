package com.hbm.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.lib.RefStrings;
import com.hbm.render.loader.HbmModelLoader;
import com.hbm.render.loader.IModelCustomNamed;

import net.minecraft.util.ResourceLocation;

/**
 * The model resource manager.
 *
 * The original's 469 static fields parsed every OBJ at class init, padding startup for nothing;
 * here it's load-on-demand + cache: whoever uses it triggers the parse, repeat takes hit the cache.
 *
 * Note: OBJ model objects only make sense client-side; don't touch them from server code.
 */
public final class ResourceManager {

	private static final Map<String, IModelCustomNamed> CACHE = new HashMap<>();

	/** The handful of high-frequency models worth preheating (first screen / common machines); everything else stays lazy */
	private static final List<String> PRELOAD = new ArrayList<>();

	private ResourceManager() { }

	/** The single model entry point: names like "models/xxx.obj", the hbm domain gets prepended inside */
	public static IModelCustomNamed getModel(String name) {
		IModelCustomNamed cached = CACHE.get(name);
		if(cached != null) return cached;

		IModelCustomNamed loaded = HbmModelLoader.loadModel(new ResourceLocation("hbm", name));
		CACHE.put(name, loaded);
		return loaded;
	}

	/** Registers a model for preheating; the actual parse still happens in loadPreloaded(), not at registration */
	public static void registerPreload(String name) {
		if(!PRELOAD.contains(name)) PRELOAD.add(name);
	}

	public static void loadPreloaded() {
		for(String name : PRELOAD) getModel(name);
	}

	/** Clears the cache on resource reload so the next access re-parses from the new resources */
	public static void clear() {
		CACHE.clear();
	}

	/* ---------- armor model roster ----------
	 *
	 * This is the lazy-style roster: only the OBJ's resource path is registered, parsing happens on the first
	 * getModel(...) call, so the fields are Strings rather than model objects — class init reads nothing from disk.
	 * Usage: ResourceManager.getModel(ResourceManager.armor_envsuit).
	 *
	 * Some model filenames are capitalized (AJR.obj / BJ.obj), matching the actual names in the resource tree; don't touch.
	 * Also note several armors share one model and differ only in texture: AJRO uses armor_ajr,
	 * Digamma uses armor_fau, Desh uses armor_steamsuit, Diesel uses armor_dieselsuit
	 *(pointing at bnuuy.obj), RPA uses armor_remnant, and the NCR patrol armor uses armor_ncr.
	 */
	public static final String armor_envsuit = "models/armor/envsuit.obj";
	public static final String armor_hev = "models/armor/hev.obj";
	public static final String armor_ncr = "models/armor/ncrpa.obj";
	public static final String armor_dnt = "models/armor/dnt.obj";
	public static final String armor_remnant = "models/armor/remnant.obj";
	public static final String armor_ajr = "models/armor/AJR.obj";
	public static final String armor_bj = "models/armor/BJ.obj";
	public static final String armor_t51 = "models/armor/t51.obj";
	public static final String armor_fau = "models/armor/fau.obj";
	public static final String armor_dieselsuit = "models/armor/bnuuy.obj";
	public static final String armor_steamsuit = "models/armor/steamsuit.obj";
	public static final String armor_trenchmaster = "models/armor/trenchmaster.obj";
	public static final String armor_taurun = "models/armor/taurun.obj";
	public static final String armor_bismuth = "models/armor/bismuth.obj";
	public static final String armor_hat = "models/armor/hat.obj";

	/* ---------- armor textures ----------
	 *
	 * A ResourceLocation is just a name; nothing is read at registration; the render side is what actually fetches the texture.
	 * Names likewise follow the old version, so each slot can be checked line by line.
	 */
	private static ResourceLocation armorTex(String file) {
		return new ResourceLocation(RefStrings.MODID, "textures/armor/" + file);
	}

	public static final ResourceLocation envsuit_helmet = armorTex("envsuit_helmet.png");
	public static final ResourceLocation envsuit_chest = armorTex("envsuit_chest.png");
	public static final ResourceLocation envsuit_arm = armorTex("envsuit_arm.png");
	public static final ResourceLocation envsuit_leg = armorTex("envsuit_leg.png");
	/** The tail isn't one of the four regular slots; it gets its own */
	public static final ResourceLocation envsuit_tail = armorTex("envsuit_tail.png");

	public static final ResourceLocation hev_helmet = armorTex("hev_helmet.png");
	public static final ResourceLocation hev_chest = armorTex("hev_chest.png");
	public static final ResourceLocation hev_arm = armorTex("hev_arm.png");
	public static final ResourceLocation hev_leg = armorTex("hev_leg.png");

	public static final ResourceLocation ncrpa_helmet = armorTex("ncrpa_helmet.png");
	public static final ResourceLocation ncrpa_chest = armorTex("ncrpa_chest.png");
	public static final ResourceLocation ncrpa_arm = armorTex("ncrpa_arm.png");
	public static final ResourceLocation ncrpa_leg = armorTex("ncrpa_leg.png");

	public static final ResourceLocation dnt_helmet = armorTex("dnt_helmet.png");
	public static final ResourceLocation dnt_chest = armorTex("dnt_chest.png");
	public static final ResourceLocation dnt_arm = armorTex("dnt_arm.png");
	public static final ResourceLocation dnt_leg = armorTex("dnt_leg.png");

	public static final ResourceLocation rpa_helmet = armorTex("rpa_helmet.png");
	public static final ResourceLocation rpa_chest = armorTex("rpa_chest.png");
	public static final ResourceLocation rpa_arm = armorTex("rpa_arm.png");
	public static final ResourceLocation rpa_leg = armorTex("rpa_leg.png");

	public static final ResourceLocation ajr_helmet = armorTex("ajr_helmet.png");
	public static final ResourceLocation ajr_chest = armorTex("ajr_chest.png");
	public static final ResourceLocation ajr_arm = armorTex("ajr_arm.png");
	public static final ResourceLocation ajr_leg = armorTex("ajr_leg.png");

	public static final ResourceLocation ajro_helmet = armorTex("ajro_helmet.png");
	public static final ResourceLocation ajro_chest = armorTex("ajro_chest.png");
	public static final ResourceLocation ajro_arm = armorTex("ajro_arm.png");
	public static final ResourceLocation ajro_leg = armorTex("ajro_leg.png");

	public static final ResourceLocation bj_chest = armorTex("bj_chest.png");
	public static final ResourceLocation bj_arm = armorTex("bj_arm.png");
	public static final ResourceLocation bj_leg = armorTex("bj_leg.png");
	/** The eyepatch and jetpack are extra attachments, one each */
	public static final ResourceLocation bj_eyepatch = armorTex("bj_eyepatch.png");
	public static final ResourceLocation bj_jetpack = armorTex("bj_jetpack.png");

	public static final ResourceLocation t51_helmet = armorTex("t51_helmet.png");
	public static final ResourceLocation t51_chest = armorTex("t51_chest.png");
	public static final ResourceLocation t51_arm = armorTex("t51_arm.png");
	public static final ResourceLocation t51_leg = armorTex("t51_leg.png");

	public static final ResourceLocation fau_helmet = armorTex("fau_helmet.png");
	public static final ResourceLocation fau_chest = armorTex("fau_chest.png");
	public static final ResourceLocation fau_arm = armorTex("fau_arm.png");
	public static final ResourceLocation fau_leg = armorTex("fau_leg.png");
	/** The cassette: the texture of the sound-making gadget on the Digamma armor's shoulder */
	public static final ResourceLocation fau_cassette = armorTex("fau_cassette.png");

	/* The diesel armor's files are called bnuuy in the resource tree; field names keep the old dieselsuit_* */
	public static final ResourceLocation dieselsuit_helmet = armorTex("bnuuy_helmet.png");
	public static final ResourceLocation dieselsuit_chest = armorTex("bnuuy_chest.png");
	public static final ResourceLocation dieselsuit_arm = armorTex("bnuuy_arm.png");
	public static final ResourceLocation dieselsuit_leg = armorTex("bnuuy_leg.png");

	public static final ResourceLocation steamsuit_helmet = armorTex("steamsuit_helmet.png");
	public static final ResourceLocation steamsuit_chest = armorTex("steamsuit_chest.png");
	public static final ResourceLocation steamsuit_arm = armorTex("steamsuit_arm.png");
	public static final ResourceLocation steamsuit_leg = armorTex("steamsuit_leg.png");

	public static final ResourceLocation taurun_helmet = armorTex("taurun_helmet.png");
	public static final ResourceLocation taurun_chest = armorTex("taurun_chest.png");
	public static final ResourceLocation taurun_arm = armorTex("taurun_arm.png");
	public static final ResourceLocation taurun_leg = armorTex("taurun_leg.png");

	public static final ResourceLocation trenchmaster_helmet = armorTex("trenchmaster_helmet.png");
	public static final ResourceLocation trenchmaster_chest = armorTex("trenchmaster_chest.png");
	public static final ResourceLocation trenchmaster_arm = armorTex("trenchmaster_arm.png");
	public static final ResourceLocation trenchmaster_leg = armorTex("trenchmaster_leg.png");

	/* These two aren't slot-named, kept as in the old version */
	public static final ResourceLocation armor_bismuth_tex = armorTex("bismuth.png");
	public static final ResourceLocation hat = armorTex("hat.png");
}
