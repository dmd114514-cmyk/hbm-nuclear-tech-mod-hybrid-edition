package api.hbm.recipe;

/**
 * Install point for the outward interface. The implementation lives in com.hbm.crafting, but the reference is held here,
 * so the api layer does not depend back on com.hbm - and tweaking mods only have to know this one face.
 */
public final class HbmRecipes {

	private static IRecipeRegistry registry = null;
	private static IHbmRecipeFilter filter = IHbmRecipeFilter.ALLOW_ALL;

	private HbmRecipes() { }

	public static void install(IRecipeRegistry impl) {
		registry = impl;
	}

	/** May be null (before the mod has finished initializing); callers null-check */
	public static IRecipeRegistry registry() {
		return registry;
	}

	public static void setFilter(IHbmRecipeFilter recipeFilter) {
		filter = recipeFilter == null ? IHbmRecipeFilter.ALLOW_ALL : recipeFilter;
	}

	public static IHbmRecipeFilter filter() {
		return filter;
	}
}
