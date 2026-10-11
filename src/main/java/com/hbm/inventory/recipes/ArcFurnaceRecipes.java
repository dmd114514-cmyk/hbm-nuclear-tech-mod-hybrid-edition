package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import static com.hbm.inventory.OreDictManager.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.crafting.SerializableRecipe;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.util.Tuple.Pair;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

/**
 * The arc furnace: one thing in, either a better solid out or a melt pot out (folded into scrap icons by material).
 *
 * Last in the whole table is the old rule — it eats the arcSmeltable filled by the ore dict surface plus the material distribution table's
 * materialOreEntries, and both must run first.
 */
public class ArcFurnaceRecipes extends SerializableRecipe {

	public static List<Pair<AStack, ArcFurnaceRecipe>> recipeList = new ArrayList();
	/* The lookup quick table, built off recipeList the first time a real match is needed */
	public static HashMap<ComparableStack, ArcFurnaceRecipe> fastCacheSolid = new HashMap();
	public static HashMap<ComparableStack, ArcFurnaceRecipe> fastCacheLiquid = new HashMap();
	/* A placeholder book during table building: once an input has a solid (or melt) output, don't hang a second one */
	public static HashSet<ComparableStack> occupiedSolid = new HashSet();
	public static HashSet<ComparableStack> occupiedLiquid = new HashSet();

	@Override
	public void registerDefaults() {

		register(new OreDictStack(KEY_SAND),			new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(1))));
		register(new ComparableStack(Items.FLINT),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(QUARTZ.gem()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 3))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
		register(new OreDictStack(QUARTZ.dust()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 3))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(3))));
		register(new OreDictStack(QUARTZ.block()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 12))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.NUGGET.q(12))));
		register(new OreDictStack(FIBER.ingot()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(FIBER.block()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 40))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));
		register(new OreDictStack(ASBESTOS.ingot()),	new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(ASBESTOS.dust()),		new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 4))		.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(1, 2))));
		register(new OreDictStack(ASBESTOS.block()),	new ArcFurnaceRecipe().solid(new ItemStack(ModItems.nugget_silicon, 40))	.fluid(new MaterialStack(Mats.MAT_SILICON, MaterialShapes.INGOT.q(9, 2))));

		// skipped (piece not landed: glass_quartz — block side): the quartz sand smelted into quartz glass row; the input is in place (com.hbm.blocks.generic.ModBlocksGeneric.sand_mix
		// (this version's com.hbm.blocks.generic.EnumSandType.QUARTZ tier), only the product missing; restore and re-add both imports once it lands
		// register(new ComparableStack(ModBlocksGeneric.sand_mix, 1, EnumSandType.QUARTZ), new ArcFurnaceRecipe().solid(new ItemStack(ModBlocks.glass_quartz)));
		register(new OreDictStack(BORAX.dust()), new ArcFurnaceRecipe().solid(new ItemStack(ModItems.powder_boron_tiny, 3)).fluid(new MaterialStack(Mats.MAT_BORON, MaterialShapes.NUGGET.q(3))));

		// skipped (piece not landed: the ItemBedrockOreNew family): the old section hung roast/arc products per celestial bedrock ore grade,
		// restore the twelve register lines of old 68~82 when it lands, public doors untouched.

		// Single-shape autogen: when shape name + material name resolves in the ore dict, hang one smelting output
		for(NTMMaterial material : Mats.orderedList) {
			int in = material.convIn;
			int out = material.convOut;
			NTMMaterial convert = material.smeltsInto;
			if(convert.smeltable == SmeltingBehavior.SMELTABLE) {
				for(MaterialShapes shape : MaterialShapes.allShapes) {
					if(!shape.noAutogen) {
						String name = shape.name() + material.names[0];
						if(!OreDictionary.getOres(name).isEmpty()) {
							OreDictStack dict = new OreDictStack(name);
							ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
							recipe.fluid(new MaterialStack(convert, (int) (shape.q(1) * out / in)));
							register(dict, recipe);
						}
					}
				}
			}
		}

		// Custom smeltables: these two books are filled by the material distribution table; MatDistribution isn't migrated ⇒ empty for now, hooks kept in place
		for(Entry<String, List<MaterialStack>> entry : Mats.materialOreEntries.entrySet()) {
			OreDictStack dict = new OreDictStack(entry.getKey());
			addCustomSmeltable(dict, entry.getValue());
		}
		for(Entry<ComparableStack, List<MaterialStack>> entry : Mats.materialEntries.entrySet()) {
			addCustomSmeltable(entry.getKey(), entry.getValue());
		}

		// The vanilla smelting entries the arc furnace also honors
		registerFurnaceSmeltables();

		// skipped (a wiring-order matter): the old version also called registerFurnaceSmeltables() a second time in the server-start event,
		// to wait out other mods' slow recipe registration. This tree's whole registerDefaults only runs at postInit via the framework
		// (MainRegistry:222), by which point everyone else's init is long past — one pass suffices;
		// should a mod genuinely hang recipes after postInit, add a line at the main package's serverStarted door.
	}

	public static void registerFurnaceSmeltables() {
		for(Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
			ItemStack input = entry.getKey();
			ItemStack output = entry.getValue();

			if(input != null && output != null) {
				ComparableStack comp = new ComparableStack(input);
				if(OreDictManager.arcSmeltable.contains(comp) || OreDictManager.arcSmeltable.contains(new ComparableStack(output))) {
					ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
					recipe.solid(output.copy());
					register(comp, recipe);
				}
			}
		}
	}

	public static void register(AStack input, ArcFurnaceRecipe output) {
		List<ItemStack> inputs = input.extractStacks();
		for(ItemStack stack : inputs) {
			ComparableStack compStack = new ComparableStack(stack);
			if(compStack.meta == OreDictionary.WILDCARD_VALUE) compStack.meta = 0;
			if(output.solidOutput != null) if(occupiedSolid.contains(compStack)) return;
			if(output.fluidOutput != null) if(occupiedLiquid.contains(compStack)) return;
		}
		recipeList.add(new Pair(input, output));
		for(ItemStack stack : inputs) {
			ComparableStack compStack = new ComparableStack(stack);
			if(compStack.meta == OreDictionary.WILDCARD_VALUE) compStack.meta = 0;
			if(output.solidOutput != null) occupiedSolid.add(compStack);
			if(output.fluidOutput != null) occupiedLiquid.add(compStack);
		}
	}

	private static void addCustomSmeltable(AStack astack, List<MaterialStack> mats) {
		List<MaterialStack> smeltables = new ArrayList();
		for(MaterialStack mat : mats) {
			if(mat.material.smeltable == SmeltingBehavior.SMELTABLE) {
				smeltables.add(mat);
			}
		}
		if(smeltables.isEmpty()) return;
		ArcFurnaceRecipe recipe = new ArcFurnaceRecipe();
		recipe.fluid(smeltables.toArray(new MaterialStack[0]));
		register(astack, recipe);
	}

	public static ArcFurnaceRecipe getOutput(ItemStack stack, boolean liquid) {

		if(stack == null || stack.getItem() == null) return null;

		// Scrap computes on the spot: it carries no recipe of its own, hinging entirely on whether its material melts
		if(stack.getItem() == ModItems.scraps && liquid) {
			NTMMaterial mat = Mats.matById.get(stack.getItemDamage());
			if(mat == null) return null;
			MaterialStack mats = ItemScraps.getMats(stack);
			if(mats.material.smeltable == SmeltingBehavior.SMELTABLE) {
				return new ArcFurnaceRecipe().fluid(mats);
			}
		}

		ComparableStack cacheKey = new ComparableStack(stack).makeSingular();
		if(!liquid && fastCacheSolid.containsKey(cacheKey)) return fastCacheSolid.get(cacheKey);
		if(liquid && fastCacheLiquid.containsKey(cacheKey)) return fastCacheLiquid.get(cacheKey);

		for(Pair<AStack, ArcFurnaceRecipe> entry : recipeList) {
			if(entry.getKey().matchesRecipe(stack, true)) {
				ArcFurnaceRecipe rec = entry.getValue();
				if((liquid && rec.fluidOutput != null) || (!liquid && rec.solidOutput != null)) {
					if(!liquid) fastCacheSolid.put(cacheKey, rec);
					if(liquid) fastCacheLiquid.put(cacheKey, rec);
					return rec;
				}
			}
		}

		if(!liquid) fastCacheSolid.put(cacheKey, null);
		if(liquid) fastCacheLiquid.put(cacheKey, null);

		return null;
	}

	/** The solid tier's whole table, display only; gameplay always goes through getOutput */
	public static HashMap getSolidRecipes() {
		HashMap<Object, Object> recipes = new HashMap<Object, Object>();
		for(Pair<AStack, ArcFurnaceRecipe> recipe : ArcFurnaceRecipes.recipeList) {
			if(recipe.getValue().solidOutput != null) recipes.put(recipe.getKey().copy(), recipe.getValue().solidOutput.copy());
		}
		return recipes;
	}

	/** The melt tier's whole table: products all folded into scrap icons, including the "any meltable straight into the furnace" half */
	public static HashMap getFluidRecipes() {
		HashMap<Object, Object> recipes = new HashMap<Object, Object>();
		for(Pair<AStack, ArcFurnaceRecipe> recipe : ArcFurnaceRecipes.recipeList) {
			if(recipe.getValue().fluidOutput != null && recipe.getValue().fluidOutput.length > 0) {
				Object[] out = new Object[recipe.getValue().fluidOutput.length];
				for(int i = 0; i < out.length; i++) out[i] = ItemScraps.create(recipe.getValue().fluidOutput[i], true);
				recipes.put(recipe.getKey().copy(), out);
			}
		}
		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.smeltable == SmeltingBehavior.SMELTABLE) {
				recipes.put(new ItemStack(ModItems.scraps, 1, mat.id), ItemScraps.create(new MaterialStack(mat, MaterialShapes.INGOT.q(1)), true));
			}
		}
		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmArcFurnace.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipeList;
	}

	@Override
	public void deleteRecipes() {
		occupiedSolid.clear();
		occupiedLiquid.clear();
		recipeList.clear();
		fastCacheSolid.clear();
		fastCacheLiquid.clear();
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject rec = (JsonObject) recipe;
		ArcFurnaceRecipe arc = new ArcFurnaceRecipe();

		AStack input = this.readAStack(rec.get("input").getAsJsonArray());

		if(rec.has("solid")) {
			arc.solid(this.readItemStack(rec.get("solid").getAsJsonArray()));
		}

		if(rec.has("fluid")) {
			JsonArray fluids = rec.get("fluid").getAsJsonArray();
			List<MaterialStack> mats = new ArrayList();
			for(JsonElement fluid : fluids) {
				JsonArray matStack = fluid.getAsJsonArray();
				MaterialStack stack = new MaterialStack(Mats.matByName.get(matStack.get(0).getAsString()), matStack.get(1).getAsInt());
				if(stack.material.smeltable == SmeltingBehavior.SMELTABLE) {
					mats.add(stack);
				}
			}
			if(!mats.isEmpty()) {
				arc.fluid(mats.toArray(new MaterialStack[0]));
			}
		}

		register(input, arc);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Pair<AStack, ArcFurnaceRecipe> rec = (Pair<AStack, ArcFurnaceRecipe>) recipe;

		writer.name("input");
		this.writeAStack(rec.getKey(), writer);

		if(rec.getValue().solidOutput != null) {
			writer.name("solid");
			this.writeItemStack(rec.getValue().solidOutput, writer);
		}

		if(rec.getValue().fluidOutput != null) {
			writer.name("fluid").beginArray();
			writer.setIndent("");
			for(MaterialStack stack : rec.getValue().fluidOutput) {
				writer.beginArray();
				writer.value(stack.material.names[0]).value(stack.amount);
				writer.endArray();
			}
			writer.endArray();
			writer.setIndent("  ");
		}
	}

	public static class ArcFurnaceRecipe {

		public MaterialStack[] fluidOutput;
		public ItemStack solidOutput;

		public ArcFurnaceRecipe fluid(MaterialStack... outputs) {
			this.fluidOutput = outputs;
			return this;
		}

		/** For bedrock ores' "a tier may be unconfigured" case: all-empty means no melt output hung */
		public ArcFurnaceRecipe fluidNull(MaterialStack... outputs) {
			List<MaterialStack> mat = new ArrayList();
			for(MaterialStack stack : outputs) if(stack != null) mat.add(stack);
			if(!mat.isEmpty()) this.fluidOutput = mat.toArray(new MaterialStack[0]);
			return this;
		}

		public ArcFurnaceRecipe solid(ItemStack output) {
			this.solidOutput = output;
			return this;
		}
	}
}
