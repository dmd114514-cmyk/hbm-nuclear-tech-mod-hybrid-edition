package com.hbm.compat.jei.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

/**
 * The JEI wrapper for one {@link GenericRecipe}.
 * <br><br>
 * Inputs go through {@link AStack#extractStacks()} — a recipe's input is by nature a set of "items that would match",
 * and JEI gets the whole list as options: anything the player drops in counts (same as the old display).
 * Outputs go through {@code IOutput.getAllPossibilities()}: chance outputs list every possible result, without computing odds for the player.
 * <br><br>
 * The fluid side is deliberately absent: this version's {@code FluidStack} isn't the same type as Forge's, and converting wants a
 * conversion door first, landing with the fluid family's pass (the pilot blast furnace recipes are item-only anyway).
 */
public class GenericRecipeWrapper implements IRecipeWrapper {

	/** The duration/power line draws below the slots */
	private static final int INFO_COLOR = 0x808080;

	private final GenericRecipe recipe;

	public GenericRecipeWrapper(GenericRecipe recipe) {
		this.recipe = recipe;
	}

	public GenericRecipe getRecipe() {
		return this.recipe;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {

		List<List<ItemStack>> inputs = new ArrayList<List<ItemStack>>();

		if(this.recipe.inputItem != null) {
			for(AStack stack : this.recipe.inputItem) {
				List<ItemStack> list = stack == null ? null : stack.extractStacks();
				if(list != null && !list.isEmpty())
					inputs.add(list);
			}
		}
		ingredients.setInputLists(VanillaTypes.ITEM, inputs);

		List<ItemStack> outputs = new ArrayList<ItemStack>();

		if(this.recipe.outputItem != null) {
			for(IOutput out : this.recipe.outputItem) {
				ItemStack[] all = out == null ? null : out.getAllPossibilities();
				if(all == null)
					continue;
				for(ItemStack stack : all)
					if(stack != null && !stack.isEmpty())
						outputs.add(stack);
			}
		}
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {

		String line = this.info();

		if(line != null)
			minecraft.fontRenderer.drawString(line, 2, recipeHeight - 10, INFO_COLOR);
	}

	private String info() {

		boolean hasTime = this.recipe.duration > 0;
		boolean hasPower = this.recipe.power > 0;

		if(!hasTime && !hasPower)
			return null;

		String text = "";

		if(hasTime)
			text += (this.recipe.duration / 20) + "s";

		if(hasPower)
			text += (text.isEmpty() ? "" : "  ") + this.recipe.power + "HE/t";

		return text;
	}
}
