package api.hbm.module;

/**
 * Module lifecycle stages.
 *
 * Its only reason to exist: the driver and the error messages use one word, spelled once.
 * A module normally overrides the individual methods ({@link IHbmModule#preInit()} and friends),
 * and does not need this parameter to branch on - if it does, that content probably wants splitting into two modules.
 */
public enum ModulePhase {

	PRE_INIT("preInit"),
	INIT("init"),
	POST_INIT("postInit"),
	REGISTER_MODELS("registerModels");

	public final String label;

	ModulePhase(String label) {
		this.label = label;
	}
}
