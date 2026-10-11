package com.hbm.compat;

/**
 * A compat module. One implementation per optional dependency (CraftTweaker, GameStages, JEI...),
 * called only when the mod is actually loaded; the core never crashes over a missing compat mod.
 */
public interface ICompatModule {

	/** The target mod's modid */
	public String modid();

	/** Return false when other prerequisites (say, a version range) aren't met and the module gets skipped */
	public default boolean isAvailable() { return true; }

	public default void preInit() { }
	public default void init() { }
	public default void postInit() { }
}
