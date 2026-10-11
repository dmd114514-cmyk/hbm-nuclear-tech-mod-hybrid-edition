package com.hbm.compat.jei.wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.FluidForgeBridge;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.inventory.recipes.CrucibleRecipe;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes.ElectrolysisRecipe;
import com.hbm.inventory.recipes.ElectrolyserMetalRecipes.ElectrolysisMetalRecipe;
import com.hbm.inventory.recipes.MixerRecipes.MixerRecipe;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes.RotaryFurnaceRecipe;
import com.hbm.inventory.recipes.SolderingRecipes.SolderingRecipe;
import com.hbm.inventory.recipes.OutgasserRecipes.OutgasserRecipe;
import com.hbm.inventory.recipes.ParticleAcceleratorRecipes.ParticleAcceleratorRecipe;
import com.hbm.inventory.recipes.PyroOvenRecipes.PyroOvenRecipe;
import com.hbm.inventory.recipes.RefineryRecipes.RefineryRecipe;
import com.hbm.items.machine.ItemScraps;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.Tuple.Triplet;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

/**
 * The mixed item/fluid wrapper: fluids in, fluids out, plus item byproducts.
 * <br><br>
 * Each family differs in how many paths in/out and whether a cell is a **real amount** or a **100-based percentage**,
 * so one factory method per family, with which cell maps to which spelled in its comment — no universal parser.
 * <br><br>
 * Four tables in: fluids in, fluids out, items out, **items in** (the last for machines like the pyrolysis oven/mixer/liquefier/arc furnace that "take solids as feed too";
 * item inputs are grouped by slot, each group being that cell's options — an ore-dict key flattens into one group, not many slots).
 * Families without item inputs pass {@link #NO_ITEM_IN}; that side takes no slots.
 */
public class MixedRecipeWrapper implements IRecipeWrapper {

	/** The refinery's table states "feed always counts as 100 mB", so display follows */
	private static final int REFINERY_INPUT = 100;

	/** The nitric acid the metal electrolyzer consumes per batch: no column in the table — it's the machine's hard-coded drain (the old page showed 100 too) */
	private static final int NITRIC_ACID_BATCH = 100;

	/** Passed when a family has no item inputs: read-only, so every factory doesn't build its own empty table */
	private static final List<List<ItemStack>> NO_ITEM_IN = Collections.emptyList();

	private final List<List<ItemStack>> itemIn;
	private final List<FluidStack> fluidIn;
	private final List<FluidStack> fluidOut;
	private final List<ItemStack> itemOut;

	public MixedRecipeWrapper(List<List<ItemStack>> itemIn, List<FluidStack> fluidIn, List<FluidStack> fluidOut, List<ItemStack> itemOut) {
		this.itemIn = itemIn;
		this.fluidIn = fluidIn;
		this.fluidOut = fluidOut;
		this.itemOut = itemOut;
	}

	/**
	 * The electrolyzer: one fluid enters at **the recipe's written real amount** → at most two fluid paths + several item byproducts (amounts verbatim, no conversion).
	 * <br>The second output often reads {@code Fluids.NONE, 0} meaning "no such path": zero amounts or unconvertible types simply aren't placed,
	 * but **unlike the ratio tables, the entry isn't skipped** — "one output path" is a normal electrolyzer recipe.
	 */
	public static List<IRecipeWrapper> electrolyser(Map<FluidType, ElectrolysisRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, ElectrolysisRecipe> entry : table.entrySet()) {

			ElectrolysisRecipe recipe = entry.getValue();

			if(recipe == null || !usable(entry.getKey(), recipe.amount))
				continue;

			List<FluidStack> ins = new ArrayList<FluidStack>();
			ins.add(new FluidStack(entry.getKey(), recipe.amount));

			List<FluidStack> outs = new ArrayList<FluidStack>();

			if(usable(recipe.output1, 0)) outs.add(recipe.output1);
			if(usable(recipe.output2, 0)) outs.add(recipe.output2);

			List<ItemStack> items = new ArrayList<ItemStack>();

			if(recipe.byproduct != null)
				for(ItemStack stack : recipe.byproduct)
					if(stack != null && !stack.isEmpty())
						items.add(stack);

			wrappers.add(new MixedRecipeWrapper(NO_ITEM_IN, ins, outs, items));
		}

		return wrappers;
	}

	/**
	 * The coker: one oil enters at the {@code Triplet}'s first-slot amount → one solid product (fuel coke) + one byproduct oil path.
	 * <br>{@code byproduct} may be null (the no-byproduct tier) — that's not a broken recipe; but entries with neither output have no display value and skip.
	 */
	public static List<IRecipeWrapper> coker(Map<FluidType, Triplet<Integer, ItemStack, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Triplet<Integer, ItemStack, FluidStack>> entry : table.entrySet()) {

			Triplet<Integer, ItemStack, FluidStack> value = entry.getValue();

			if(value == null || value.getX() == null || !usable(entry.getKey(), value.getX()))
				continue;

			List<FluidStack> ins = new ArrayList<FluidStack>();
			ins.add(new FluidStack(entry.getKey(), value.getX()));

			List<ItemStack> items = new ArrayList<ItemStack>();

			if(value.getY() != null && !value.getY().isEmpty())
				items.add(value.getY());

			List<FluidStack> outs = new ArrayList<FluidStack>();

			if(usable(value.getZ(), 0))
				outs.add(value.getZ());

			if(items.isEmpty() && outs.isEmpty())
				continue;

			wrappers.add(new MixedRecipeWrapper(NO_ITEM_IN, ins, outs, items));
		}

		return wrappers;
	}

	/**
	 * The refinery: one hot oil in → four fractions + one solid byproduct (sulfur or tar).
	 * <br>The family's own table states "feed always counts as 100 mB", and the four fraction numbers are **percentages** (summing to 100 as a group),
	 * so feed displays at 100 and outputs at their written amounts — what you read is the percentage, matching the vacuum distillation family.
	 * <br>A null solid cell means "no solid product on this row" — a normal recipe; only when one of the four fractions fails to convert to a Forge fluid does the entry skip
	 * (the machine takes the four as one batch; a half-drawn row misleads).
	 */
	public static List<IRecipeWrapper> refinery(Map<FluidType, RefineryRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, RefineryRecipe> entry : table.entrySet()) {

			RefineryRecipe value = entry.getValue();

			if(value == null || value.outputs == null || !usable(entry.getKey(), REFINERY_INPUT))
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

			if(!allUsable)
				continue;

			List<FluidStack> ins = new ArrayList<FluidStack>();
			ins.add(new FluidStack(entry.getKey(), REFINERY_INPUT));

			List<ItemStack> items = new ArrayList<ItemStack>();

			if(value.solid != null && !value.solid.isEmpty())
				items.add(value.solid);

			wrappers.add(new MixedRecipeWrapper(NO_ITEM_IN, ins, outs, items));
		}

		return wrappers;
	}

	/**
	 * The solidifier: one fluid enters at **the recipe's written real amount** → one solid, no fluid output.
	 * <br>The table's {@code Pair} is (mB, output); entries missing an output aren't placed (an empty slot reads clearer than a "?")
	 * and the same goes for fluids that won't convert to Forge's side — with no such slot, a half entry misleads.
	 * <br>The auto entries (mB back-computed from burn heat) often reach thousands of mB; that's the recipe's own convention, displayed without conversion.
	 */
	public static List<IRecipeWrapper> solidifier(Map<FluidType, Pair<Integer, ItemStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, Pair<Integer, ItemStack>> entry : table.entrySet()) {

			Pair<Integer, ItemStack> value = entry.getValue();

			if(value == null || value.getKey() == null || !usable(entry.getKey(), value.getKey()))
				continue;

			ItemStack out = value.getValue();

			if(out == null || out.isEmpty())
				continue;

			List<FluidStack> ins = new ArrayList<FluidStack>();
			ins.add(new FluidStack(entry.getKey(), value.getKey()));

			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add(out);

			wrappers.add(new MixedRecipeWrapper(NO_ITEM_IN, ins, new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/**
	 * The pyrolysis oven: all four cells may matter — one fluid in + one solid in → one fluid out + one solid out; unwritten cells stay empty.
	 * <br>This family's table is "one builder line per recipe" ({@code in(...)}/{@code out(...)} called once each),
	 * so {@code inputFluid}/{@code outputFluid} can each be null: fluid-in + solid-out is the oil-to-solid-fuel batch,
	 * double-in double-out is the coal-to-syngas family — both normal recipes.
	 * <br>{@code duration} isn't drawn (the mixed category has no info line, matching the electrolyzer families); a written fluid that won't convert skips the whole entry.
	 */
	public static List<IRecipeWrapper> pyroOven(List<PyroOvenRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(PyroOvenRecipe recipe : table) {

			if(recipe == null || !absentOrUsable(recipe.inputFluid) || !absentOrUsable(recipe.outputFluid))
				continue;

			List<FluidStack> ins = new ArrayList<FluidStack>();

			if(recipe.inputFluid != null)
				ins.add(recipe.inputFluid);

			List<FluidStack> outs = new ArrayList<FluidStack>();

			if(recipe.outputFluid != null)
				outs.add(recipe.outputFluid);

			List<ItemStack> items = new ArrayList<ItemStack>();

			if(recipe.outputItem != null && !recipe.outputItem.isEmpty())
				items.add(recipe.outputItem);

			List<List<ItemStack>> itemIn = solidSlot(recipe.inputItem);

			/* with one side of in or out completely empty there's nothing to see: such an entry would just be an arrow pointing at nothing */
			if(itemIn.isEmpty() && ins.isEmpty())
				continue;

			if(items.isEmpty() && outs.isEmpty())
				continue;

			wrappers.add(new MixedRecipeWrapper(itemIn, ins, outs, items));
		}

		return wrappers;
	}

	/**
	 * The mixer: at most two fluids + one solid in → **the key's fluid** out, output amount from the recipe's own {@code output}.
	 * <br>One key fluid carries an array (the machine cycles them in order), flattened here entry by entry — display order is the machine's polling order.
	 * <br>If the key path won't convert to a Forge fluid, the whole key skips — no output can be placed, so those entries have no result to show;
	 * entries missing all three inputs skip too (empty recipes).
	 */
	public static List<IRecipeWrapper> mixer(Map<FluidType, MixerRecipe[]> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<FluidType, MixerRecipe[]> entry : table.entrySet()) {

			MixerRecipe[] recipes = entry.getValue();

			if(recipes == null || !usable(entry.getKey(), 1))
				continue;

			for(MixerRecipe recipe : recipes) {

				if(recipe == null || recipe.output <= 0 || !absentOrUsable(recipe.input1) || !absentOrUsable(recipe.input2))
					continue;

				List<FluidStack> ins = new ArrayList<FluidStack>();

				if(recipe.input1 != null)
					ins.add(recipe.input1);

				if(recipe.input2 != null)
					ins.add(recipe.input2);

				List<List<ItemStack>> itemIn = solidSlot(recipe.solidInput);

				if(itemIn.isEmpty() && ins.isEmpty())
					continue;

				List<FluidStack> outs = new ArrayList<FluidStack>();
				outs.add(new FluidStack(entry.getKey(), recipe.output));

				wrappers.add(new MixedRecipeWrapper(itemIn, ins, outs, new ArrayList<ItemStack>()));
			}
		}

		return wrappers;
	}

	/**
	 * The liquefier: one solid in → one fluid out; no fluid-input slot on this one.
	 * <br>The table's key comes in two shapes: ore dict names (stored as String) and exact items ({@code ComparableStack}),
	 * each flattening into the input slot's options — dict names go through {@code OreDictStack}, and one that yields nothing (an empty dict) isn't placed;
	 * exact items place one entry via {@code extractStacks()} as-is, with the wildcard-meta row (the fish) showing the wildcard itself.
	 * <br>Beyond the table sits an ItemFood fallback (food value × saturation converted into slime), computed live at query time rather than stored here,
	 * so this page won't show it — the machine really does accept all foods; the display can only list what the table spells out.
	 */
	public static List<IRecipeWrapper> liquefaction(Map<Object, FluidStack> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<Object, FluidStack> entry : table.entrySet()) {

			FluidStack out = entry.getValue();

			if(!usable(out, 0))
				continue;

			List<List<ItemStack>> itemIn = dictOrStack(entry.getKey());

			if(itemIn.isEmpty())
				continue;

			List<FluidStack> outs = new ArrayList<FluidStack>();
			outs.add(out);

			wrappers.add(new MixedRecipeWrapper(itemIn, new ArrayList<FluidStack>(), outs, new ArrayList<ItemStack>()));
		}

		return wrappers;
	}

	/**
	 * The arc furnace: one solid in → one solid out and/or several melts.
	 * <br>A melt in this table isn't a fluid type but a {@code MaterialStack} (counted by material); both the old version and this machine's GUI
	 * used the scrap icon for it, so it draws on the item output row here too, not shoved into the fluid row.
	 * <br>Either output tier may be absent: solids-only are the ones converted from vanilla smelting, melts-only are material auto-entries — both normal recipes;
	 * entries with neither, or whose input yields no items, skip.
	 */
	public static List<IRecipeWrapper> arcFurnace(List<Pair<AStack, ArcFurnaceRecipe>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Pair<AStack, ArcFurnaceRecipe> entry : table) {

			if(entry == null || entry.getValue() == null)
				continue;

			List<List<ItemStack>> itemIn = solidSlot(entry.getKey());

			if(itemIn.isEmpty())
				continue;

			List<ItemStack> items = itemOutputs(entry.getValue());

			if(items.isEmpty())
				continue;

			wrappers.add(new MixedRecipeWrapper(itemIn, new ArrayList<FluidStack>(), new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/** The arc furnace's output slot count = the solid cell (at most one) + the melt rows' scrap count, declared from the widest row */
	public static int maxArcFurnaceOutputs(List<Pair<AStack, ArcFurnaceRecipe>> table) {

		int max = 0;

		for(Pair<AStack, ArcFurnaceRecipe> entry : table) {
			if(entry == null || entry.getValue() == null)
				continue;

			ArcFurnaceRecipe recipe = entry.getValue();
			int count = recipe.solidOutput == null ? 0 : 1;

			if(recipe.fluidOutput != null)
				count += recipe.fluidOutput.length;

			max = Math.max(max, count);
		}

		return max;
	}

	private static List<ItemStack> itemOutputs(ArcFurnaceRecipe recipe) {

		List<ItemStack> items = new ArrayList<ItemStack>();

		if(recipe.solidOutput != null && !recipe.solidOutput.isEmpty())
			items.add(recipe.solidOutput);

		if(recipe.fluidOutput != null)
			for(MaterialStack mat : recipe.fluidOutput)
				/* an empty material yields no scrap icon (it would come out as ModItems.nothing placeholder); not placed */
				if(mat != null && mat.material != null)
					items.add(ItemScraps.create(mat, true));

		return items;
	}

	/** The branch for the two tables whose keys come in two shapes (liquefier/arc): String is a dict name, {@code AStack} an exact or dict item; unrecognized shapes aren't placed */
	private static List<List<ItemStack>> dictOrStack(Object key) {

		if(key instanceof String)
			return solidSlot(new OreDictStack((String) key));

		if(key instanceof AStack)
			return solidSlot((AStack) key);

		return Collections.emptyList();
	}

	/**
	 * The irradiation channel (the block the old page hung under): one solid in → one solid out and/or one fluid out; no fluid-input slot.
	 * <br>Either output tier may be absent (gold ingot→gold-198 solid-only, lithium→tritium fluid-only, coal→tar+syngas both),
	 * entries with neither skip; entries with the {@code fusionOnly} bit are for the fusion breeder — the table's own comment says "no other machine should eat this"
	 * ⇒ not placed on this page (the old NEI likewise filed them under the breeder by this flag).
	 * <br>Solid outputs include NBT variants folded by {@code DictFrame.fromOne} (the tar/coke tiers), drawn as-is with their own icon and name.
	 */
	public static List<IRecipeWrapper> outgasser(Map<AStack, OutgasserRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<AStack, OutgasserRecipe> entry : table.entrySet()) {

			OutgasserRecipe recipe = entry.getValue();

			/* the {@code fusionOnly} bit, see the comment above; a written fluid that won't convert skips the whole entry, same rule as the pyrolysis family */
			if(recipe == null || recipe.fusionOnly || !absentOrUsable(recipe.liquidOutput))
				continue;

			List<List<ItemStack>> itemIn = solidSlot(entry.getKey());

			if(itemIn.isEmpty())
				continue;

			List<ItemStack> items = new ArrayList<ItemStack>();

			if(recipe.solidOutput != null && !recipe.solidOutput.isEmpty())
				items.add(recipe.solidOutput);

			List<FluidStack> outs = new ArrayList<FluidStack>();

			if(recipe.liquidOutput != null)
				outs.add(recipe.liquidOutput);

			if(items.isEmpty() && outs.isEmpty())
				continue;

			wrappers.add(new MixedRecipeWrapper(itemIn, new ArrayList<FluidStack>(), outs, items));
		}

		return wrappers;
	}

	/**
	 * The dual coker: one solid in → one solid out + one fluid out; either tier may be absent (tar→coke solid-only, sodium powder→sodium fluid-only).
	 * <br>The key's two shapes branch like the liquefier's; a written fluid that won't convert skips the whole entry — a solids-only half would suggest this machine makes no gas.
	 * <br>The bedrock-ore roasting entries were never in this table (skip-noted there), so this page lists only what's written in it.
	 */
	public static List<IRecipeWrapper> combination(Map<Object, Pair<ItemStack, FluidStack>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<Object, Pair<ItemStack, FluidStack>> entry : table.entrySet()) {

			Pair<ItemStack, FluidStack> out = entry.getValue();

			if(out == null || !absentOrUsable(out.getValue()))
				continue;

			List<List<ItemStack>> itemIn = dictOrStack(entry.getKey());

			if(itemIn.isEmpty())
				continue;

			List<ItemStack> items = new ArrayList<ItemStack>();

			if(out.getKey() != null && !out.getKey().isEmpty())
				items.add(out.getKey());

			List<FluidStack> outs = new ArrayList<FluidStack>();

			if(out.getValue() != null)
				outs.add(out.getValue());

			if(items.isEmpty() && outs.isEmpty())
				continue;

			wrappers.add(new MixedRecipeWrapper(itemIn, new ArrayList<FluidStack>(), outs, items));
		}

		return wrappers;
	}

	/**
	 * The metal electrolyzer: one solid in (various crystals) + nitric acid in → two melt paths + several solid byproducts; no fluid output.
	 * <br>The nitric acid cell has no table field: it's the machine's fixed per-batch drain (the old page displayed it the same way),
	 * so the number lives on the display side with its provenance noted, rather than touching the recipe table.
	 * <br>The two melt paths follow the arc furnace's rule — not fluid types in this version, drawn as scrap icons on the item output row; byproducts trail them in recipe order.
	 */
	public static List<IRecipeWrapper> electrolyserMetal(Map<AStack, ElectrolysisMetalRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<AStack, ElectrolysisMetalRecipe> entry : table.entrySet()) {

			ElectrolysisMetalRecipe recipe = entry.getValue();

			if(recipe == null)
				continue;

			List<List<ItemStack>> itemIn = solidSlot(entry.getKey());

			if(itemIn.isEmpty())
				continue;

			List<ItemStack> items = metalOutputs(recipe);

			if(items.isEmpty())
				continue;

			List<FluidStack> ins = new ArrayList<FluidStack>();
			ins.add(new FluidStack(Fluids.NITRIC_ACID, NITRIC_ACID_BATCH));

			wrappers.add(new MixedRecipeWrapper(itemIn, ins, new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/** The metal electrolyzer's output slot count = the melt paths that have materials + the solid byproduct count, declared from the widest row */
	public static int maxMetalElectrolyserOutputs(Map<AStack, ElectrolysisMetalRecipe> table) {

		int max = 0;

		for(ElectrolysisMetalRecipe recipe : table.values())
			if(recipe != null)
				max = Math.max(max, metalOutputs(recipe).size());

		return max;
	}

	private static List<ItemStack> metalOutputs(ElectrolysisMetalRecipe recipe) {

		List<ItemStack> items = new ArrayList<ItemStack>();

		if(recipe.output1 != null && recipe.output1.material != null)
			items.add(ItemScraps.create(recipe.output1, true));

		if(recipe.output2 != null && recipe.output2.material != null)
			items.add(ItemScraps.create(recipe.output2, true));

		if(recipe.byproduct != null)
			for(ItemStack stack : recipe.byproduct)
				if(stack != null && !stack.isEmpty())
					items.add(stack);

		return items;
	}

	/**
	 * The rotary furnace: several solids in (at most three cells: dict pieces + flux combinations) + one optional fluid in → one melt (scrap icon).
	 * <br>The solid cells are **positional**: a cell that yields no items still takes its place — otherwise the second cell's flux would drift into the first's drawing.
	 * <br>Fluid-only entries (the two aluminate rows) are normal recipes; the item-input row simply doesn't appear at all.
	 * <br>The old page wrote two more lines of numbers below the panel ({@code duration} and steam {@code steam} mB/t); the mixed category has no info line, so they aren't drawn here;
	 * steam is the machine's own operating cost, not a recipe ingredient — same rule as the pyrolysis family.
	 */
	public static List<IRecipeWrapper> rotaryFurnace(List<RotaryFurnaceRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(RotaryFurnaceRecipe recipe : table) {

			/* an empty material yields no scrap icon; a written fluid that won't convert skips the whole entry, same rule as the pyrolysis family */
			if(recipe == null || recipe.output == null || recipe.output.material == null || !absentOrUsable(recipe.fluid))
				continue;

			List<List<ItemStack>> itemIn = ingredientSlots(recipe.ingredients);

			List<FluidStack> ins = new ArrayList<FluidStack>();

			if(recipe.fluid != null)
				ins.add(recipe.fluid);

			if(itemIn.isEmpty() && ins.isEmpty())
				continue;

			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add(ItemScraps.create(recipe.output, true));

			wrappers.add(new MixedRecipeWrapper(itemIn, ins, new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/** The rotary furnace's solid slot count = the widest row's ingredient count, declaring positions whether or not a cell yields items */
	public static int maxRotaryIngredients(List<RotaryFurnaceRecipe> table) {

		int max = 0;

		for(RotaryFurnaceRecipe recipe : table)
			if(recipe != null && recipe.ingredients != null)
				max = Math.max(max, recipe.ingredients.length);

		return max;
	}

	/** One slot per ingredient, the group holding that cell's options; an unyielding cell keeps an empty group as a placeholder so later cells don't drift */
	private static List<List<ItemStack>> ingredientSlots(AStack[] ingredients) {

		List<List<ItemStack>> slots = new ArrayList<List<ItemStack>>();

		if(ingredients != null)
			for(AStack ingredient : ingredients) {

				List<ItemStack> options = ingredient == null ? null : ingredient.extractStacks();

				slots.add(options == null ? Collections.<ItemStack>emptyList() : options);
			}

		return slots;
	}

	/**
	 * The welding bench: three solid groups in (components toppings → baseboard pcb → solder, laid cell by cell in that order) + one optional fluid in → one product; no fluid output.
	 * <br>Solid cells follow the rotary furnace's rule: **positional**, an unyielding group still takes a slot, or the second group's materials would drift into the first's cell.
	 * <br>The old page wrote two lines of numbers below the panel ({@code duration} and {@code consumption} HE/t); the mixed category has no info line, so they aren't drawn here;
	 * the power draw is the machine's operating cost anyway, never an ingredient — same rule as the rotary furnace family.
	 */
	public static List<IRecipeWrapper> soldering(List<SolderingRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(SolderingRecipe recipe : table) {

			if(recipe == null || recipe.output == null || recipe.output.isEmpty() || !absentOrUsable(recipe.fluid))
				continue;

			List<List<ItemStack>> itemIn = ingredientSlots(combine(recipe.toppings, recipe.pcb, recipe.solder));

			List<FluidStack> ins = new ArrayList<FluidStack>();

			if(recipe.fluid != null)
				ins.add(recipe.fluid);

			if(itemIn.isEmpty() && ins.isEmpty())
				continue;

			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add(recipe.output);

			wrappers.add(new MixedRecipeWrapper(itemIn, ins, new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/** The welding bench's solid slot count = the widest row's three groups combined (component slot + baseboard slot + solder slot) */
	public static int maxSolderingIngredients(List<SolderingRecipe> table) {

		int max = 0;

		for(SolderingRecipe recipe : table)
			if(recipe != null)
				max = Math.max(max, combine(recipe.toppings, recipe.pcb, recipe.solder).length);

		return max;
	}

	/** The three groups flatten into one row of cells: the old page's reading order (components, baseboard, solder) */
	private static AStack[] combine(AStack[] toppings, AStack[] pcb, AStack[] solder) {

		List<AStack> all = new ArrayList<AStack>();

		for(AStack[] group : new AStack[][] {toppings, pcb, solder})
			if(group != null)
				for(AStack stack : group)
					if(stack != null)
						all.add(stack);

		return all.toArray(new AStack[all.size()]);
	}

	/**
	 * The cyclotron: **two solid slots in** (first the particle piece, then the target it strikes) → one product; no fluid slots on either side.
	 * <br>The key is a {@code Pair(particle, target)} — both together resolve it (the target may be a dict key, flattened into the second slot's options),
	 * so the two slots are **fixed positions**: slot 1 always the particle, slot 2 always the target, an unyielding one keeping an empty group (same rule as the rotary furnace).
	 * <br>The integer in the value is the antimatter measure; the old page never drew it in a slot either (only a progress bar), so it isn't placed here.
	 */
	public static List<IRecipeWrapper> cyclotron(Map<Pair<ComparableStack, AStack>, Pair<ItemStack, Integer>> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(Map.Entry<Pair<ComparableStack, AStack>, Pair<ItemStack, Integer>> entry : table.entrySet()) {

			Pair<ComparableStack, AStack> key = entry.getKey();
			Pair<ItemStack, Integer> value = entry.getValue();

			if(key == null || key.getKey() == null || value == null || value.getKey() == null || value.getKey().isEmpty())
				continue;

			List<List<ItemStack>> itemIn = new ArrayList<List<ItemStack>>();
			itemIn.add(Collections.singletonList(key.getKey().toStack()));
			itemIn.add(slotOptions(key.getValue()));

			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add(value.getKey());

			wrappers.add(new MixedRecipeWrapper(itemIn, new ArrayList<FluidStack>(), new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/** One solid input = one slot, the group listing all its options (the dict key's whole set); an unwritten cell returns an empty table */
	private static List<List<ItemStack>> solidSlot(AStack stack) {
		List<List<ItemStack>> slots = new ArrayList<List<ItemStack>>();

		if(stack != null) {

			List<ItemStack> options = stack.extractStacks();

			if(options != null && !options.isEmpty())
				slots.add(options);
		}

		return slots;
	}

	/**
	 * The particle accelerator (the old page hung under the particle detector block): two particle slots in → one or two products; no fluid slots on either side.
	 * <br>The two input slots' **order carries no meaning** — the machine matches both ways around ({@code matchesRecipe} tries both directions),
	 * but the cells stay fixed at two, unyielding ones keeping placeholders; never collapse to one.
	 * <br>{@code momentum} was a separate line below the old panel; the mixed category has no info line, so it isn't drawn here;
	 * the product cell may hold only output1 (the second being null, true of most recipes).
	 */
	public static List<IRecipeWrapper> particleAccelerator(List<ParticleAcceleratorRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(ParticleAcceleratorRecipe recipe : table) {

			if(recipe == null || recipe.output1 == null || recipe.output1.isEmpty())
				continue;

			List<List<ItemStack>> itemIn = new ArrayList<List<ItemStack>>();
			itemIn.add(slotOptions(recipe.input1));
			itemIn.add(slotOptions(recipe.input2));

			List<ItemStack> items = new ArrayList<ItemStack>();
			items.add(recipe.output1);

			if(recipe.output2 != null && !recipe.output2.isEmpty())
				items.add(recipe.output2);

			wrappers.add(new MixedRecipeWrapper(itemIn, new ArrayList<FluidStack>(), new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/**
	 * Crucible alloys: several materials in, several out, both sides counted by material.
	 * <br>Melts aren't a fluid type in this version (counted via {@code MaterialStack} like the arc furnace family); the old page placed
	 * molten scrap icons on both sides, so both rows go through items here, not shoved into the fluid row.
	 * <br>This version's table keeps only the "alloying" tier: the old three pages' smelting and casting data no longer exist in the tree (melts ride material auto-entries,
	 * casting rides the pour-acceptance port), hence one page. Each entry's {@code frequency} (ticks between operations) never sat in the old page's slots; not drawn here either.
	 */
	public static List<IRecipeWrapper> crucibleAlloying(List<CrucibleRecipe> table) {

		List<IRecipeWrapper> wrappers = new ArrayList<IRecipeWrapper>();

		for(CrucibleRecipe recipe : table) {

			if(recipe == null)
				continue;

			List<List<ItemStack>> itemIn = materialSlots(recipe.input);
			List<ItemStack> items = materialStacks(recipe.output);

			if(itemIn.isEmpty() || items.isEmpty())
				continue;

			wrappers.add(new MixedRecipeWrapper(itemIn, new ArrayList<FluidStack>(), new ArrayList<FluidStack>(), items));
		}

		return wrappers;
	}

	/** One material per input cell (no "this cell could be something else" options, so one item per group); entries that yield nothing take no cell — order carries no meaning, collapsing doesn't drift */
	private static List<List<ItemStack>> materialSlots(MaterialStack[] stacks) {

		List<List<ItemStack>> slots = new ArrayList<List<ItemStack>>();

		if(stacks == null)
			return slots;

		for(MaterialStack stack : stacks) {
			if(stack == null || stack.material == null)
				continue;
			slots.add(Collections.singletonList(ItemScraps.create(stack, true)));
		}

		return slots;
	}

	/** The output side, same as above, just without grouping */
	private static List<ItemStack> materialStacks(MaterialStack[] stacks) {

		List<ItemStack> items = new ArrayList<ItemStack>();

		if(stacks == null)
			return items;

		for(MaterialStack stack : stacks) {
			if(stack == null || stack.material == null)
				continue;
			items.add(ItemScraps.create(stack, true));
		}

		return items;
	}

	/** Slot count declared from the widest row, counted per side (using the same flattening pair, so declared and drawn numbers agree) */
	public static int maxCrucibleInputs(List<CrucibleRecipe> table) {

		int max = 0;

		for(CrucibleRecipe recipe : table)
			if(recipe != null)
				max = Math.max(max, materialSlots(recipe.input).size());

		return max;
	}

	public static int maxCrucibleOutputs(List<CrucibleRecipe> table) {

		int max = 0;

		for(CrucibleRecipe recipe : table)
			if(recipe != null)
				max = Math.max(max, materialStacks(recipe.output).size());

		return max;
	}

	/** A fixed-position solid cell: draws all options when it yields items, otherwise an empty group holds the place — position must not collapse (later materials would drift into it) */
	private static List<ItemStack> slotOptions(AStack stack) {

		if(stack == null)
			return Collections.emptyList();

		List<ItemStack> options = stack.extractStacks();

		return options == null ? Collections.<ItemStack>emptyList() : options;
	}

	/** This fluid path is either entirely unwritten (the family has no such cell) or written and usable */
	private static boolean absentOrUsable(FluidStack stack) {
		return stack == null || usable(stack, 0);
	}

	/** One byproduct per cell, so slot count comes from the widest row */
	public static int maxItemOutputs(Map<FluidType, ElectrolysisRecipe> table) {
		int max = 0;

		for(ElectrolysisRecipe recipe : table.values())
			if(recipe != null && recipe.byproduct != null)
				max = Math.max(max, recipe.byproduct.length);

		return max;
	}

	@Override
	public void getIngredients(IIngredients ingredients) {

		List<net.minecraftforge.fluids.FluidStack> ins = new ArrayList<net.minecraftforge.fluids.FluidStack>();

		for(FluidStack stack : this.fluidIn)
			if(usable(stack, 0))
				ins.add(new net.minecraftforge.fluids.FluidStack(FluidForgeBridge.getFluid(stack.type), stack.fill));

		ingredients.setInputs(VanillaTypes.FLUID, ins);

		List<net.minecraftforge.fluids.FluidStack> outs = new ArrayList<net.minecraftforge.fluids.FluidStack>();

		for(FluidStack stack : this.fluidOut)
			if(usable(stack, 0))
				outs.add(new net.minecraftforge.fluids.FluidStack(FluidForgeBridge.getFluid(stack.type), stack.fill));

		ingredients.setOutputs(VanillaTypes.FLUID, outs);

		ingredients.setOutputs(VanillaTypes.ITEM, this.itemOut);

		/* Item inputs feed JEI as "one slot per group, options inside the group", same convention as the generic/annihilator wrappers */
		if(!this.itemIn.isEmpty())
			ingredients.setInputLists(VanillaTypes.ITEM, this.itemIn);
	}

	private static boolean usable(FluidStack stack, int amount) {

		if(stack == null || stack.type == null)
			return false;

		if((amount > 0 ? amount : stack.fill) <= 0)
			return false;

		return FluidForgeBridge.getFluid(stack.type) != null;
	}

	private static boolean usable(FluidType type, int amount) {
		return type != null && amount > 0 && FluidForgeBridge.getFluid(type) != null;
	}
}
