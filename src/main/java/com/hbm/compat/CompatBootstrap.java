package com.hbm.compat;

import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.common.Loader;

/**
 * The compat module registry: the core names which optional mods' modules to install in exactly one place.
 * <br><br>
 * Going by class-name strings instead of imports keeps the compat package entirely out of the core's class graph —
 * in a clean environment those classes never even load, so there's no path from "a missing mod" to "the game crashes".
 * Prerequisite checks likewise happen only at the {@link Loader} level; modules never ask again.
 */
public final class CompatBootstrap {

	private CompatBootstrap() { }

	public static void registerModules() {

		if(Loader.isModLoaded("jei"))
			addModule("com.hbm.compat.jei.HbmJeiModule");
	}

	private static void addModule(String className) {

		try {
			Object module = Class.forName(className).newInstance();

			if(module instanceof ICompatModule) {
				CompatManager.register((ICompatModule) module);
				/* Whether it mounted must be checkable in the log: when an optional mod's display stays silent, look for this line first */
				MainRegistry.logger.info("[hbm] compat modules registered: {}", className);
			} else {
				MainRegistry.logger.error("Compat class {} is not an ICompatModule, ignored", className);
			}
		} catch(Throwable ex) {
			/* An optional mod being present but its module failing to install is its own affair; it must not take the main mod's startup down */
			MainRegistry.logger.error("Failed to load compat module {}", className, ex);
		}
	}
}
