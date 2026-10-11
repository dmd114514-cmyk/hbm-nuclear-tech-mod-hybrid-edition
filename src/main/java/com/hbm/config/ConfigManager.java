package com.hbm.config;

import java.io.File;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;

/**
 * Config entry point. 1.12.2's @Config is scanned by Forge; this does two things:
 * remember the mod config directory (the recipe system wants it) and get config values synced before the FML phases,
 * so preInit doesn't read bare field defaults.
 */
public final class ConfigManager {

	/** config/hbm — the mod's private config directory; recipe JSON lives here too */
	public static File configDir;

	private ConfigManager() { }

	/** hbmConfigDir is config/hbm itself, computed by the main class and passed in */
	public static void load(File hbmConfigDir) {
		configDir = hbmConfigDir;
		if(!configDir.exists()) configDir.mkdirs();

		net.minecraftforge.common.config.ConfigManager.sync(RefStrings.MODID, Config.Type.INSTANCE);

		// file values only reach the fields at this step, so the old divisor guard has to follow the sync, not sit in a field initializer
		WorldConfig.clampRandomizers();
		// One more thing on the structure side: fold "true|false|flag" into the int callers read, and catch min>max while at it
		StructureConfig.sanitize();

		// The HE↔FE ratio lives on the api side (a default method needs it); config pushes the value over here
		api.hbm.energymk2.EnergyConversion.setRatio(EnergyConfig.HE_PER_FE);
	}
}
