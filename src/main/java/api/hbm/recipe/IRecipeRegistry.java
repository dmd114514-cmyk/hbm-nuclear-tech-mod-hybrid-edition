package api.hbm.recipe;

import java.util.List;

import com.google.gson.JsonElement;

/**
 * The outward recipe facade. Tweaking mods like CraftTweaker / KubeJS only see this one face:
 * locate a recipe family by handler class name, then add/remove JSON.
 *
 * JSON instead of Java objects so tweak scripts never have to load HBM's classes.
 */
public interface IRecipeRegistry {

	/** Add a recipe; returns whether it worked (unknown family or bad JSON both give false) */
	public boolean register(String handlerClassName, JsonElement recipe);

	/** Remove the recipe matching the given JSON exactly */
	public boolean remove(String handlerClassName, JsonElement recipe);

	/** Empty one recipe family entirely; returns how many were removed */
	public int removeAll(String handlerClassName);

	/** Every handler currently loaded */
	public List<IRecipeHandler> handlers();

	/** Look up a handler by simple class name, null if absent */
	public IRecipeHandler handler(String handlerClassName);

	/** Attach a default-recipe load listener */
	public void addListener(IRecipeRegisterListener listener);
}
