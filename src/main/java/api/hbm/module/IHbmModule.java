package api.hbm.module;

/**
 * Entry point for an optional content package.
 *
 * New content (features worth absorbing from upstream/EX, plus third-party extensions) all comes in as modules,
 * and no module class name ever appears in core code - it only knows this interface.
 *
 * The lifecycle is driven by `HbmModuleManager` in dependency order, matching FML's own stages;
 * what each stage may do is written on the methods, because the out-of-bounds habits (setting your own registry name, newing straight into a registry)
 * make content look registered when it is not, so better to spell it out here.
 */
public interface IHbmModule {

	/** Lowercase unique id, also the config key `modules.<id>.enabled` and the name in the log */
	String getId();

	/** Ids of other modules this one needs; if any is missing the whole module stays off and the log says why */
	public default String[] getDependencies() {
		return new String[0];
	}

	/** The default switch when there is no config entry. Think when adding a module: default off means a half-finished one cannot wreck somebody's save */
	public default boolean enabledByDefault() {
		return true;
	}

	/** Register blocks/items/sounds/entities/TEs by queueing through `HbmRegistry` only - do not call setRegistryName yourself */
	public default void preInit() { }

	/** Recipes, events, capabilities, GUI handlers */
	public default void init() { }

	/** Things that depend on other mods' content (by now all content and recipes are in place) */
	public default void postInit() { }

	/** Client only: models and assets. Do not touch the world here */
	public default void registerModels() { }
}
