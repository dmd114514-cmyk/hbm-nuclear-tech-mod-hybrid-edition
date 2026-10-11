package com.hbm.compat.jei.wrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidForgeBridge;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.recipes.AtmosphereRecipes.AtmosphereRecipe;
import com.hbm.inventory.recipes.CrackingRecipes;
import com.hbm.inventory.recipes.FractionRecipes;
import com.hbm.inventory.recipes.HydrotreatingRecipes;
import com.hbm.inventory.recipes.ReformingRecipes;
import com.hbm.inventory.recipes.VacuumRefineryRecipes.VacuumRefineryRecipe;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.Tuple.Quartet;
import com.hbm.util.Tuple.Triplet;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraftforge.fluids.Fluid;

/**
 * The generic wrapper for pure-fluid recipes: so many fluids in, so many out, content flattened by each family's factory method.
 * <br><br>
 * Each family's table looks different (some values are {@code Pair}s, some {@code Triplet}s — and the same
 * {@code Triplet} means different things per machine: the hydrogenation unit's first slot is **an extra input**, while the reformer's three
 * are all **outputs**), so instead of forcing one universal parser it's "one factory method per family", spelling out
 * which cell of that family's table lands in which slot.
 * <br><br>
 * The numbers in those tables are mostly **ratios per 100 mB of feed** (paired ones sum to 100, unpaired ones are loose amounts),
 * so the feed side always displays as 100 mB and "how much comes out" reads as percentages — same as the old display.
 * Families using real amounts say so in their factory method's comment.
 * <br><br>
 * This version has two fluid type systems that aren't the same class: machines use {@link FluidType}, Forge wants
 * {@link net.minecraftforge.fluids.FluidStack}; conversion goes through {@link FluidForgeBridge}.
 * If any path fails to convert (that type never mounted to Forge) the whole entry skips, rather than drawing half of one.
 */
public class FluidRecipeWrapper implements IRecipeWrapper {

	/** A display-sized feed amount */
	private static final int INPUT_AMOUNT = 100;

	private final List<FluidStack> inputs;
	private final List<FluidStack> outputs;

	public FluidRecipeWrapper(List<FluidStack> inputs, List<FluidStack> outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}

	/* ---- family factories: table → display entries ---- */

	/** Splitter / cracker: one oil → two oils */
	public static List<IRecipeWrapper> pairTable(Map<FluidType, Pair<FluidStack, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Pair<FluidStack, FluidStack>> entry : table.entrySet()) {

			Pair<FluidStack, FluidStack> outputs = entry.getValue();

			if(!usable(entry.getKey(), INPUT_AMOUNT) || !usable(outputs == null ? null : outputs.getKey(), 0) || !usable(outputs == null ? null : outputs.getValue(), 0))
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			inputs.add(new FluidStack(entry.getKey(), INPUT_AMOUNT));

			List<FluidStack> outs = new ArrayList<FluidStack>();
			outs.add(outputs.getKey());
			outs.add(outputs.getValue());

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/**
	 * The hydrogenation unit: the {@code Triplet}'s first slot is **an extra input** (hydrogen), the latter two are outputs.
	 * <br>This family's hydrogen carries pressure; pressure is a machine-side parameter and isn't converted for display — drawn as written.
	 */
	public static List<IRecipeWrapper> hydrotreating(Map<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> entry : table.entrySet()) {

			Triplet<FluidStack, FluidStack, FluidStack> value = entry.getValue();

			if(value == null)
				continue;

			if(!usable(entry.getKey(), INPUT_AMOUNT) || !usable(value.getX(), 0) || !usable(value.getY(), 0) || !usable(value.getZ(), 0))
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			inputs.add(new FluidStack(entry.getKey(), INPUT_AMOUNT));
			inputs.add(value.getX());

			List<FluidStack> outs = new ArrayList<FluidStack>();
			outs.add(value.getY());
			outs.add(value.getZ());

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/** The reformer: all three {@code Triplet} slots are **outputs**; the feed is only the table key */
	public static List<IRecipeWrapper> reforming(Map<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> entry : table.entrySet()) {

			Triplet<FluidStack, FluidStack, FluidStack> value = entry.getValue();

			if(value == null)
				continue;

			if(!usable(entry.getKey(), INPUT_AMOUNT) || !usable(value.getX(), 0) || !usable(value.getY(), 0) || !usable(value.getZ(), 0))
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			inputs.add(new FluidStack(entry.getKey(), INPUT_AMOUNT));

			List<FluidStack> outs = new ArrayList<FluidStack>();
			outs.add(value.getX());
			outs.add(value.getY());
			outs.add(value.getZ());

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/** The vacuum distillation tower: one oil → four outputs (the table's four numbers sum to 100 as a group) */
	public static List<IRecipeWrapper> vacuumRefinery(Map<FluidType, VacuumRefineryRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, VacuumRefineryRecipe> entry : table.entrySet()) {

			VacuumRefineryRecipe value = entry.getValue();

			if(value == null || value.outputs == null || !usable(entry.getKey(), INPUT_AMOUNT))
				continue;

			List<FluidStack> outs = new ArrayList<FluidStack>();
			boolean allUsable = true;

			for(FluidStack out : value.outputs) {
				if(!usable(out, 0)) {
					allUsable = false;
					break;
				}
				outs.add(out);
			}

			if(!allUsable || outs.isEmpty())
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			inputs.add(new FluidStack(entry.getKey(), INPUT_AMOUNT));

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/**
	 * The alkylation unit: the {@code Triplet}'s first slot is **the second input** (the catalyst acid), the latter two are outputs.
	 * <br><br>
	 * Unlike the hydrogenation family, "no second acid path" is **normal writing** here — two of the default table's six rows put
	 * {@code Fluids.NONE, 0} in the first slot ({@code NONE} means the empty can, which never enters Forge's fluid registry,
	 * so {@link #usable} naturally returns false). A missing cell here **doesn't skip the entry** — only that cell stays empty;
	 * likewise one output path means one drawn path. Only "the feed itself won't convert" and "neither output path exists" actually skip.
	 */
	public static List<IRecipeWrapper> alkylation(Map<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Triplet<FluidStack, FluidStack, FluidStack>> entry : table.entrySet()) {

			if(!usable(entry.getKey(), INPUT_AMOUNT))
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			inputs.add(new FluidStack(entry.getKey(), INPUT_AMOUNT));

			List<FluidStack> outs = new ArrayList<FluidStack>();

			Triplet<FluidStack, FluidStack, FluidStack> value = entry.getValue();

			if(value != null) {

				if(usable(value.getX(), 0))
					inputs.add(value.getX());

				if(usable(value.getY(), 0))
					outs.add(value.getY());

				if(usable(value.getZ(), 0))
					outs.add(value.getZ());
			}

			if(outs.isEmpty())
				continue;

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/** The cryogenic fractionator: one gas → four outputs (all four {@code Quartet} slots are outputs, laid out in W/X/Y/Z written order) */
	public static List<IRecipeWrapper> cryo(Map<FluidType, Quartet<FluidStack, FluidStack, FluidStack, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Quartet<FluidStack, FluidStack, FluidStack, FluidStack>> entry : table.entrySet()) {

			Quartet<FluidStack, FluidStack, FluidStack, FluidStack> value = entry.getValue();

			if(value == null || !usable(entry.getKey(), INPUT_AMOUNT))
				continue;

			List<FluidStack> outs = new ArrayList<FluidStack>();
			boolean allUsable = true;

			for(FluidStack out : new FluidStack[] { value.getW(), value.getX(), value.getY(), value.getZ() }) {

				if(!usable(out, 0)) {
					allUsable = false;
					break;
				}

				outs.add(out);
			}

			if(!allUsable)
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			inputs.add(new FluidStack(entry.getKey(), INPUT_AMOUNT));

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/**
	 * The fusion breeder blanket: one fluid in → one fluid out, both amounts drawn as the recipe wrote them.
	 * <br><br>
	 * This is the only family in the wrapper that doesn't go by percentages: the {@code Pair}'s first slot is **the feed's real consumption**
	 * (not a 100-based ratio) and the output sits on the {@code FluidStack}'s own amount, so it copies verbatim, no conversion.
	 * If either side fails to convert to a Forge fluid, the whole entry skips — a half-drawn one-in-one-out chain is meaningless.
	 */
	public static List<IRecipeWrapper> fluidBreeder(Map<FluidType, Pair<Integer, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Pair<Integer, FluidStack>> entry : table.entrySet()) {

			Pair<Integer, FluidStack> value = entry.getValue();

			if(value == null || value.getKey() == null)
				continue;

			if(!usable(entry.getKey(), value.getKey()) || !usable(value.getValue(), 0))
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			inputs.add(new FluidStack(entry.getKey(), value.getKey()));

			List<FluidStack> outs = new ArrayList<FluidStack>();
			outs.add(value.getValue());

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/**
	 * Atmospheric chemistry (two fluid paths folded to one): **the table keys on the output**, with {@code inputFluids} in the value being the feed — direction reversed from the other families.
	 * <br>This family writes **real tank amounts** (the 30k/160k/260k mB tier), not 100-based percentages; drawn as-is.
	 * <br>The table is private; only its own public {@code getRecipesMap()} is used; any path failing to convert skips the whole entry.
	 */
	public static List<IRecipeWrapper> atmosphere(Map<FluidStack, AtmosphereRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidStack, AtmosphereRecipe> entry : table.entrySet()) {

			AtmosphereRecipe recipe = entry.getValue();

			if(recipe == null || recipe.inputFluids == null || !usable(entry.getKey(), 0))
				continue;

			List<FluidStack> inputs = new ArrayList<FluidStack>();
			boolean allUsable = true;

			for(FluidStack stack : recipe.inputFluids) {

				if(!usable(stack, 0)) {
					allUsable = false;
					break;
				}

				inputs.add(stack);
			}

			if(!allUsable || inputs.isEmpty())
				continue;

			List<FluidStack> outs = new ArrayList<FluidStack>();
			outs.add(entry.getKey());

			wrappers.add(new FluidRecipeWrapper(inputs, outs));
		}

		return wrappers;
	}

	/* ---- display ---- */
	@Override
	public void getIngredients(IIngredients ingredients) {

		List<net.minecraftforge.fluids.FluidStack> ins = new ArrayList<net.minecraftforge.fluids.FluidStack>();

		for(FluidStack stack : this.inputs)
			add(ins, stack);

		ingredients.setInputs(VanillaTypes.FLUID, ins);

		List<net.minecraftforge.fluids.FluidStack> outs = new ArrayList<net.minecraftforge.fluids.FluidStack>();

		for(FluidStack stack : this.outputs)
			add(outs, stack);

		ingredients.setOutputs(VanillaTypes.FLUID, outs);
	}

	private static void add(List<net.minecraftforge.fluids.FluidStack> list, FluidStack stack) {

		net.minecraftforge.fluids.FluidStack forge = toForge(stack, 0);

		if(forge != null)
			list.add(forge);
	}

	/* ---- conversion ---- */

	/** Whether it can flatten at all: type in hand, and a Forge fluid reachable */
	private static boolean usable(FluidStack stack, int amount) {
		return toForge(stack, amount) != null;
	}

	private static boolean usable(FluidType type, int amount) {
		return toForge(type, amount) != null;
	}

	/** amount of 0 means use the recipe's written amount; unconvertible returns null */
	private static net.minecraftforge.fluids.FluidStack toForge(FluidStack stack, int amount) {

		if(stack == null || stack.type == null)
			return null;

		return toForge(stack.type, amount > 0 ? amount : stack.fill);
	}

	private static net.minecraftforge.fluids.FluidStack toForge(FluidType type, int amount) {

		if(type == null || amount <= 0)
			return null;

		Fluid fluid = FluidForgeBridge.getFluid(type);

		if(fluid == null)
			return null;

		return new net.minecraftforge.fluids.FluidStack(fluid, amount);
	}
}
