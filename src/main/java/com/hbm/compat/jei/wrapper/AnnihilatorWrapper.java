package com.hbm.compat.jei.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hbm.inventory.recipes.AnnihilatorRecipes;
import com.hbm.inventory.recipes.AnnihilatorRecipes.AnnihilatorRecipe;
import com.hbm.items.ModItems;
import com.hbm.util.InventoryUtil;
import com.hbm.util.Tuple.Pair;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

/**
 * One annihilator display: one input material → one output.
 * <br><br>
 * The machine's table maps one material key to several tiers of (dose, output); the tiers themselves carry no display value,
 * so each tier flattens into its own entry — readers only care "what can this turn into".
 * <br><br>
 * Two rules carried from the old version: a key that isn't an item (ore dict / fluid) flattens wholesale into options; any entry with `item_secret` on either side
 * isn't shown at all — that's the hidden easter-egg piece, hidden from the old NEI page too.
 */
public class AnnihilatorWrapper implements IRecipeWrapper {

	private final Object key;
	private final ItemStack output;

	public AnnihilatorWrapper(Object key, ItemStack output) {
		this.key = key;
		this.output = output;
	}

	public ItemStack getOutputItem() {
		return this.output;
	}

	/** Flattens the whole table into display entries; tiers whose input yields no item, or that hit the hidden piece, are skipped */
	public static List<IRecipeWrapper> wrapAll() {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<Object, AnnihilatorRecipe> entry : AnnihilatorRecipes.recipes.entrySet()) {

			ItemStack[][] inputs = extract(entry.getKey());

			if(inputs == null || inputs.length == 0 || containsSecret(inputs))
				continue;

			for(Pair<java.math.BigInteger, ItemStack> milestone : entry.getValue().milestones) {

				ItemStack out = milestone.getValue();

				if(out == null || out.isEmpty() || out.getItem() == ModItems.item_secret)
					continue;

				wrappers.add(new AnnihilatorWrapper(entry.getKey(), out));
			}
		}

		return wrappers;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {

		List<List<ItemStack>> inputs = new ArrayList<List<ItemStack>>();
		ItemStack[][] extracted = extract(this.key);

		if(extracted != null) {
			for(ItemStack[] array : extracted) {
				List<ItemStack> list = new ArrayList<ItemStack>();
				for(ItemStack stack : array)
					if(stack != null && !stack.isEmpty())
						list.add(stack);
				if(!list.isEmpty())
					inputs.add(list);
			}
		}

		ingredients.setInputLists(VanillaTypes.ITEM, inputs);

		List<ItemStack> outputs = new ArrayList<ItemStack>();
		outputs.add(this.output);
		ingredients.setOutputs(VanillaTypes.ITEM, outputs);
	}

	private static ItemStack[][] extract(Object key) {

		try {
			return InventoryUtil.extractObject(key);
		} catch(Throwable ex) {
			/* when a key mixes in another mod's stuff, don't drag the whole display down — skip the entry */
			return null;
		}
	}

	private static boolean containsSecret(ItemStack[][] stacks) {

		for(ItemStack[] array : stacks)
			for(ItemStack stack : array)
				if(stack != null && stack.getItem() == ModItems.item_secret)
					return true;

		return false;
	}
}
