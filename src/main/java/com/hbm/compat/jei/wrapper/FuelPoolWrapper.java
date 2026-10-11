package com.hbm.compat.jei.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hbm.inventory.RecipesCommon.ComparableStack;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * One spent-fuel-pool display: one hot item → one cooled item.
 * <br><br>
 * The table keys on {@link ComparableStack} (item + meta compared together), so display places whatever its own {@code toStack()} gives,
 * never guessing which fuel a meta is — that's the fuel family's definition; display just copies.
 * <br><br>
 * The RBMK fuel rod segment isn't in this family: the machine's table hasn't taken it either (see {@code FuelPoolRecipes}' skip note),
 * so what's shown here is the table's entirety.
 */
public class FuelPoolWrapper implements IRecipeWrapper {

	private final ItemStack input;
	private final ItemStack output;

	public FuelPoolWrapper(ComparableStack input, ItemStack output) {
		this.input = input.toStack();
		this.output = output;
	}

	/** Flattens the table into display entries; entries missing a key or an output aren't placed (nothing to read into slots) */
	public static List<IRecipeWrapper> wrapAll(Map<ComparableStack, ItemStack> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<ComparableStack, ItemStack> entry : table.entrySet()) {

			ItemStack out = entry.getValue();

			if(entry.getKey() == null || out == null || out.isEmpty())
				continue;

			wrappers.add(new FuelPoolWrapper(entry.getKey(), out));
		}

		return wrappers;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {

		List<ItemStack> inputs = new ArrayList<ItemStack>();
		inputs.add(this.input);
		ingredients.setInputs(VanillaTypes.ITEM, inputs);

		List<ItemStack> outputs = new ArrayList<ItemStack>();
		outputs.add(this.output);
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}
}
