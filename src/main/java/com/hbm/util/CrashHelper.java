package com.hbm.util;

import com.hbm.crafting.SerializableRecipe;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ICrashCallable;

/**
 * Injects a "which recipes the scripts changed" section into crash reports.
 * Recipes live as external JSON; once a player or pack edits them they vanish from code,
 * and a stack trace alone can't tell a mod bug from an edited recipe — this section saves that round-trip.
 */
public class CrashHelper {

	public static void init() {
		FMLCommonHandler.instance().registerCrashCallable(new CrashCallableRecipe());
	}

	public static class CrashCallableRecipe implements ICrashCallable {

		@Override
		public String getLabel() {
			return "HBM Modified Recipes";
		}

		@Override
		public String call() throws Exception {

			StringBuilder out = new StringBuilder();

			for(SerializableRecipe recipe : SerializableRecipe.recipeHandlers) {
				if(recipe.modified)
					out.append("\n\t\t").append(recipe.getFileName());
			}

			return out.toString();
		}
	}
}
