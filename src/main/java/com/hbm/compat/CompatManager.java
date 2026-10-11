package com.hbm.compat;

import java.util.ArrayList;
import java.util.List;

import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.common.Loader;

/**
 * The compat layer's dispatcher. Modules register themselves; this filters once by "is the mod loaded",
 * guaranteeing that in a clean environment not one of their classes is ever touched.
 */
public final class CompatManager {

	private static final List<ICompatModule> modules = new ArrayList<>();

	private CompatManager() { }

	public static void register(ICompatModule module) {
		modules.add(module);
	}

	public static void preInit() {
		for(ICompatModule module : modules) {
			if(isLoaded(module)) {
				try {
					module.preInit();
				} catch(Throwable ex) {
					MainRegistry.logger.error("Compat module {} failed in preInit", module.modid(), ex);
				}
			}
		}
	}

	public static void init() {
		for(ICompatModule module : modules) {
			if(isLoaded(module)) {
				try {
					module.init();
				} catch(Throwable ex) {
					MainRegistry.logger.error("Compat module {} failed in init", module.modid(), ex);
				}
			}
		}
	}

	public static void postInit() {
		for(ICompatModule module : modules) {
			if(isLoaded(module)) {
				try {
					module.postInit();
				} catch(Throwable ex) {
					MainRegistry.logger.error("Compat module {} failed in postInit", module.modid(), ex);
				}
			}
		}
	}

	private static boolean isLoaded(ICompatModule module) {
		return module.isAvailable() && Loader.isModLoaded(module.modid());
	}
}
