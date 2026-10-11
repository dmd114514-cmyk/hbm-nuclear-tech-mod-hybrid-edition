package api.hbm.recipe;

import java.io.IOException;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

/**
 * Contract for a serializable recipe handler (the abstract part of the old SerializableRecipe, pulled out into an interface
 * so the api layer does not have to import com.hbm.crafting in reverse).
 *
 * Each implementation: given a JSON, add the recipe to its own static table; given a recipe, write it back out as JSON.
 */
public interface IRecipeHandler {

	/** Recipe file name, which is also the recipe id inside the JSON */
	public String getFileName();

	/** The container holding all recipes (usually a List or Map), walked when writing the template out */
	public Object getRecipeObject();

	/** Read one recipe out of a JSON and register it */
	public void readRecipe(JsonElement recipe);

	/** Write one recipe into the GSON stream */
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException;

	/** Register the built-in default recipes */
	public void registerDefaults();

	/** Wipe all recipes */
	public void deleteRecipes();

	/** Finish up after everything is registered (IMC hooks and the like go here) */
	public default void registerPost() { }

	/** Blurb written at the top of the JSON file */
	public default String getComment() { return null; }

	/** Whether an empty default table still gets a template written */
	public default boolean allowEmptyRecipeList() { return false; }
}
