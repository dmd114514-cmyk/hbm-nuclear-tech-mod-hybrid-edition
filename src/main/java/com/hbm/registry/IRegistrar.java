package com.hbm.registry;

/**
 * A registration unit for one slice of content. The old ModBlocks/ModItems were several-hundred-KB monsters;
 * now each subdomain splits into its own Registrar minding its own patch, and none of them gets to grow into a behemoth again.
 *
 * The three lifecycle callbacks are driven by HbmRegistry, in the same order as FML's phases.
 */
public interface IRegistrar {

	/** Identifies this unit in logs; defaults to the class name */
	public default String id() {
		return getClass().getSimpleName();
	}

	/** Constructs objects and queues them for RegistryEvent (this is **not** the moment of actual registration) */
	public default void preInit() { }

	/**
	 * Fluid types, the material table and other "not Forge registry objects" get their explicit init here.
	 * Timed at preInit, before any Forge registration — because constructing blocks/items later references them.
	 */
	public default void registerFluids() { }

	/** Registrations without a RegistryEvent — TEs, recipes, ore dictionary — go here */
	public default void init() { }

	/** Anything depending on other mods' content goes here (every mod's content is registered by then) */
	public default void postInit() { }

	/**
	 * Registers this subdomain's item/block models. **Only ever called on the client**,
	 * so implementations may use client API freely — the server never reaches this method.
	 */
	public default void registerModels() { }
}
