package com.hbm.compat.jei;

import com.hbm.compat.ICompatModule;

/**
 * The JEI display module shell.
 * <br><br>
 * This class **deliberately references no {@code mezz.jei} classes**: it gets loaded in clean environments (the core registers modules by class name,
 * loading it only to check the interface), so if a JEI type appeared in the body, environments without JEI would die right at class load.
 * The classes actually talking to JEI are the plugin plus the category/wrapper classes in the same package, touched only by JEI's own annotation scan.
 * <br><br>
 * Category and recipe registration happen in the plugin (JEI instantiates it via {@code @JEIPlugin} itself); this class only carries
 * "is JEI present, is the version sufficient, log a line on error".
 */
public class HbmJeiModule implements ICompatModule {

	/*Only JEI 4.x has IModPlugin; a 3.x-era jar fails class lookup, so the major version is guarded too */
	private static final int MIN_MAJOR_VERSION = 4;

	@Override
	public String modid() {
		return "jei";
	}

	@Override
	public boolean isAvailable() {
		return hasSupportedVersion();
	}

	@Override
	public void preInit() { }

	private static boolean hasSupportedVersion() {

		net.minecraftforge.fml.common.ModContainer jei =
				net.minecraftforge.fml.common.Loader.instance().getIndexedModList().get("jei");

		if(jei == null)
			return true;

		try {
			return Integer.parseInt(jei.getVersion().split("\\.")[0]) >= MIN_MAJOR_VERSION;
		} catch(Throwable ex) {
			/* A version string that doesn't parse as a version is treated as "usable": better the plugin errors on its own than the whole display silently vanishes */
			return true;
		}
	}
}
