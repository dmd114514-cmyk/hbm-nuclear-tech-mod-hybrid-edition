package com.hbm.crafting;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.hbm.main.MainRegistry;

import api.hbm.recipe.IRecipeHandler;
import api.hbm.recipe.IRecipeRegisterListener;
import api.hbm.recipe.IRecipeRegistry;

/**
 * Implementation of the recipe registration facade. All external add/remove goes through JSON:
 * add = hand the JSON to the matching handler; remove = have the handler serialize its own recipes to JSON and compare,
 * so a handler needs no extra code for "reverse removal" and custom packs don't have to touch HBM classes.
 */
public class RecipeRegistry implements IRecipeRegistry {

	public static final RecipeRegistry INSTANCE = new RecipeRegistry();

	private RecipeRegistry() { }

	@Override
	public boolean register(String handlerClassName, JsonElement recipe) {
		IRecipeHandler handler = handler(handlerClassName);
		if(handler == null || recipe == null) return false;
		try {
			handler.readRecipe(recipe);
			return true;
		} catch(Exception ex) {
			MainRegistry.logger.error("Failed to register recipe for {}", handlerClassName, ex);
			return false;
		}
	}

	@Override
	public boolean remove(String handlerClassName, JsonElement recipe) {
		IRecipeHandler handler = handler(handlerClassName);
		if(handler == null || recipe == null) return false;

		String target = compact(recipe);
		Object recipeObject = handler.getRecipeObject();

		if(recipeObject instanceof Collection) {
			Collection<?> collection = (Collection<?>) recipeObject;
			Iterator<?> it = collection.iterator();
			while(it.hasNext()) {
				if(target.equals(serialize(handler, it.next()))) {
					it.remove();
					return true;
				}
			}
		} else if(recipeObject instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) recipeObject;
			for(Object key : new ArrayList<>(map.keySet())) {
				if(target.equals(serialize(handler, map.get(key)))) {
					((Map) map).remove(key);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int removeAll(String handlerClassName) {
		IRecipeHandler handler = handler(handlerClassName);
		if(handler == null) return 0;

		int before = count(handler);
		handler.deleteRecipes();
		return Math.max(before, 0);
	}

	@Override
	public List<IRecipeHandler> handlers() {
		return new ArrayList<>(SerializableRecipe.recipeHandlers);
	}

	@Override
	public IRecipeHandler handler(String handlerClassName) {
		for(SerializableRecipe recipe : SerializableRecipe.recipeHandlers) {
			if(recipe.getClass().getSimpleName().equals(handlerClassName)) return recipe;
		}
		return null;
	}

	@Override
	public void addListener(IRecipeRegisterListener listener) {
		SerializableRecipe.addListener(listener);
	}

	private static int count(IRecipeHandler handler) {
		Object recipeObject = handler.getRecipeObject();
		if(recipeObject instanceof Collection) return ((Collection<?>) recipeObject).size();
		if(recipeObject instanceof Map) return ((Map<?, ?>) recipeObject).size();
		return 0;
	}

	/** Serializes one recipe object into compact JSON, for "remove by exact JSON" */
	private static String serialize(IRecipeHandler handler, Object recipe) {
		try {
			StringWriter sw = new StringWriter();
			JsonWriter writer = new JsonWriter(sw);
			writer.setIndent("");
			writer.beginObject();
			handler.writeRecipe(recipe, writer);
			writer.endObject();
			writer.close();
			return sw.toString();
		} catch(Exception ex) {
			return null;
		}
	}

	private static String compact(JsonElement element) {
		return SerializableRecipe.gson.toJson(element);
	}
}
