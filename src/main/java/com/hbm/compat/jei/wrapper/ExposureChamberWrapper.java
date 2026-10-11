package com.hbm.compat.jei.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.ExposureChamberRecipes.ExposureChamberRecipe;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * The exposure chamber: pellet + target → products, all three inputs items, no duration or power to draw.
 * <br><br>
 * The pellet input is also an {@code AStack}, so it flattens wholesale as "items that would match" — any one the player drops counts,
 * consistent with the machine's matchesRecipe (it tries one by one; here all are listed — same semantics).
 */
public class ExposureChamberWrapper implements IRecipeWrapper {

	private final ExposureChamberRecipe recipe;

	public ExposureChamberWrapper(ExposureChamberRecipe recipe) {
		this.recipe = recipe;
	}

	public ExposureChamberRecipe getRecipe() {
		return this.recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {

		List<List<ItemStack>> inputs = new ArrayList<List<ItemStack>>();

		addStack(inputs, this.recipe.particle);
		addStack(inputs, this.recipe.ingredient);

		ingredients.setInputLists(VanillaTypes.ITEM, inputs);

		List<ItemStack> outputs = new ArrayList<ItemStack>();

		if(this.recipe.output != null && !this.recipe.output.isEmpty())
			outputs.add(this.recipe.output);

		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	private static void addStack(List<List<ItemStack>> inputs, AStack stack) {

		if(stack == null)
			return;

		List<ItemStack> list = stack.extractStacks();

		if(list != null && !list.isEmpty())
			inputs.add(list);
	}
}
