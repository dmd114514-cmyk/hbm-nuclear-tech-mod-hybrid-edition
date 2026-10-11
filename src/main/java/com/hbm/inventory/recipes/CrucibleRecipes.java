package com.hbm.inventory.recipes;

import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ModItems;
import com.hbm.util.Compat;

import net.minecraft.item.ItemStack;

/**
 * The crucible's alloying table: a pile of materials left, a pile right, the tick count stepping the mix through.
 *
 * ⚠ The crucible never checks whether outputs outnumber inputs, so don't design recipes where material "expands" —
 * alloys almost never grow in reality; if one does, fix the recipe, not the machine.
 */
public class CrucibleRecipes extends GenericRecipes<CrucibleRecipe> {

	public static final CrucibleRecipes INSTANCE = new CrucibleRecipes();

	@Override public int inputItemLimit() { return 0; }
	@Override public int inputFluidLimit() { return 0; }
	@Override public int outputItemLimit() { return 0; }
	@Override public int outputFluidLimit() { return 0; }

	@Override public CrucibleRecipe instantiateRecipe(String name) { return new CrucibleRecipe(name); }

	@Override
	public void registerDefaults() {

		int n = MaterialShapes.NUGGET.q(1);
		int i = MaterialShapes.INGOT.q(1);

		this.register(new CrucibleRecipe("crucible.steel").setup(20, new ItemStack(ModItems.ingot_steel))
				.inputs(new MaterialStack(Mats.MAT_IRON, n * 2), new MaterialStack(Mats.MAT_CARBON, n * 3), new MaterialStack(Mats.MAT_FLUX, n))
				.outputs(new MaterialStack(Mats.MAT_STEEL, n * 2)));

		if(Compat.isModLoaded(Compat.MOD_GT6)) {
			this.register(new CrucibleRecipe("crucible.steelWrought").setup(20, new ItemStack(ModItems.ingot_steel))
					.inputs(new MaterialStack(Mats.MAT_WROUGHTIRON, n * 2), new MaterialStack(Mats.MAT_CARBON, n * 3), new MaterialStack(Mats.MAT_FLUX, n))
					.outputs(new MaterialStack(Mats.MAT_STEEL, n * 2)));
			this.register(new CrucibleRecipe("crucible.steelPig").setup(20, new ItemStack(ModItems.ingot_steel))
					.inputs(new MaterialStack(Mats.MAT_PIGIRON, n * 2), new MaterialStack(Mats.MAT_CARBON, n * 3), new MaterialStack(Mats.MAT_FLUX, n))
					.outputs(new MaterialStack(Mats.MAT_STEEL, n * 2)));
			this.register(new CrucibleRecipe("crucible.steelMeteoric").setup(20, new ItemStack(ModItems.ingot_steel))
					.inputs(new MaterialStack(Mats.MAT_METEORICIRON, n * 2), new MaterialStack(Mats.MAT_CARBON, n * 3), new MaterialStack(Mats.MAT_FLUX, n))
					.outputs(new MaterialStack(Mats.MAT_STEEL, n * 2)));
		}

		// crucible.hematite and crucible.malachite need ModBlocks.stone_resource (the red iron / malachite raw ores); skipped until that block lands with its batch

		this.register(new CrucibleRecipe("crucible.redcopper").setup(2, new ItemStack(ModItems.ingot_red_copper))
				.inputs(new MaterialStack(Mats.MAT_COPPER, n), new MaterialStack(Mats.MAT_REDSTONE, n))
				.outputs(new MaterialStack(Mats.MAT_MINGRADE, n * 2)));

		this.register(new CrucibleRecipe("crucible.hss").setup(9, new ItemStack(ModItems.ingot_dura_steel))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 5), new MaterialStack(Mats.MAT_TUNGSTEN, n * 3), new MaterialStack(Mats.MAT_COBALT, n * 1))
				.outputs(new MaterialStack(Mats.MAT_DURA, n * 9)));

		this.register(new CrucibleRecipe("crucible.hsss").setup(12, new ItemStack(ModItems.ingot_dura_steel))
				.inputs(new MaterialStack(Mats.MAT_STAINLESS, n * 5), new MaterialStack(Mats.MAT_TUNGSTEN, n * 3), new MaterialStack(Mats.MAT_COBALT, n * 1))
				.outputs(new MaterialStack(Mats.MAT_DURA, i * 2)));

		this.register(new CrucibleRecipe("crucible.ferro").setup(3, new ItemStack(ModItems.ingot_ferrouranium))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 2), new MaterialStack(Mats.MAT_U238, n))
				.outputs(new MaterialStack(Mats.MAT_FERRO, n * 3)));

		this.register(new CrucibleRecipe("crucible.tcalloy").setup(9, new ItemStack(ModItems.ingot_tcalloy))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 8), new MaterialStack(Mats.MAT_TECHNETIUM, n))
				.outputs(new MaterialStack(Mats.MAT_TCALLOY, i)));

		this.register(new CrucibleRecipe("crucible.cdalloy").setup(9, new ItemStack(ModItems.ingot_cdalloy))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n * 8), new MaterialStack(Mats.MAT_CADMIUM, n))
				.outputs(new MaterialStack(Mats.MAT_CDALLOY, i)));

		this.register(new CrucibleRecipe("crucible.bbronze").setup(9, new ItemStack(ModItems.ingot_bismuth_bronze))
				.inputs(new MaterialStack(Mats.MAT_COPPER, n * 8), new MaterialStack(Mats.MAT_BISMUTH, n), new MaterialStack(Mats.MAT_FLUX, n * 3))
				.outputs(new MaterialStack(Mats.MAT_BBRONZE, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

		this.register(new CrucibleRecipe("crucible.abronze").setup(9, new ItemStack(ModItems.ingot_arsenic_bronze))
				.inputs(new MaterialStack(Mats.MAT_COPPER, n * 8), new MaterialStack(Mats.MAT_ARSENIC, n), new MaterialStack(Mats.MAT_FLUX, n * 3))
				.outputs(new MaterialStack(Mats.MAT_ABRONZE, i), new MaterialStack(Mats.MAT_SLAG, n * 3)));

		this.register(new CrucibleRecipe("crucible.cmb").setup(3, new ItemStack(ModItems.ingot_combine_steel))
				.inputs(new MaterialStack(Mats.MAT_MAGTUNG, n * 6), new MaterialStack(Mats.MAT_MUD, n * 3))
				.outputs(new MaterialStack(Mats.MAT_CMB, i)));

		this.register(new CrucibleRecipe("crucible.magtung").setup(3, new ItemStack(ModItems.ingot_magnetized_tungsten))
				.inputs(new MaterialStack(Mats.MAT_TUNGSTEN, i), new MaterialStack(Mats.MAT_SCHRABIDIUM, n * 1))
				.outputs(new MaterialStack(Mats.MAT_MAGTUNG, i)));

		this.register(new CrucibleRecipe("crucible.bscco").setup(3, new ItemStack(ModItems.ingot_bscco))
				.inputs(new MaterialStack(Mats.MAT_BISMUTH, n * 2), new MaterialStack(Mats.MAT_STRONTIUM, n * 2), new MaterialStack(Mats.MAT_CALCIUM, n * 2), new MaterialStack(Mats.MAT_COPPER, n * 3))
				.outputs(new MaterialStack(Mats.MAT_BSCCO, i)));

		this.register(new CrucibleRecipe("crucible.arse").setup(9, new ItemStack(ModItems.ingot_gaas))
				.inputs(new MaterialStack(Mats.MAT_GALLIUM, n * 6), new MaterialStack(Mats.MAT_ARSENIC, n * 3))
				.outputs(new MaterialStack(Mats.MAT_GAAS, i)));

		this.register(new CrucibleRecipe("crucible.stainless").setup(2, new ItemStack(ModItems.ingot_stainless))
				.inputs(new MaterialStack(Mats.MAT_STEEL, n), new MaterialStack(Mats.MAT_NICKEL, n))
				.outputs(new MaterialStack(Mats.MAT_STAINLESS, n * 2)));
	}

	@Override
	public String getFileName() {
		return "hbmCrucible.json";
	}

	@Override
	public String getComment() {
		return "ID must be unique, but not sequential. Order in which the recipes are defined determines the order in which they are displayed in-game. "
				+ "Frequency is the amount of ticks between operations, must be at least 1. The names are unlocalized by default, but if they can't be found in "
				+ "the lang files the names will be displayed as-is.";
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;
		String name = obj.get("name").getAsString();
		int freq = obj.get("frequency").getAsInt();
		ItemStack icon = readItemStack(obj.get("icon").getAsJsonArray());

		MaterialStack[] input = readMaterialArray(obj.get("input").getAsJsonArray());
		MaterialStack[] output = readMaterialArray(obj.get("output").getAsJsonArray());

		this.register(new CrucibleRecipe(name).setup(freq, icon).inputs(input).outputs(output));
	}

	private MaterialStack[] readMaterialArray(JsonArray array) {
		MaterialStack[] stacks = new MaterialStack[array.size()];
		for(int i = 0; i < stacks.length; i++) {
			JsonArray entry = array.get(i).getAsJsonArray();
			stacks[i] = new MaterialStack(materialFor(entry.get(0).getAsString()), entry.get(1).getAsInt());
		}
		return stacks;
	}

	/*The table stores a material's first name and detonates on a miss — silently swallowing one would quietly lose a table row, harder to find than an error */
	private static NTMMaterial materialFor(String name) {
		NTMMaterial material = Mats.matByName.get(name);
		if(material == null) throw new IllegalArgumentException("Unknown material '" + name + "' in a crucible recipe");
		return material;
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		CrucibleRecipe rec = (CrucibleRecipe) recipe;
		writer.name("name").value(rec.getInternalName());
		writer.name("frequency").value(rec.frequency);
		writer.name("icon");
		writeItemStack(rec.getIcon(), writer);

		writer.name("input");
		writeMaterialArray(rec.input, writer);

		writer.name("output");
		writeMaterialArray(rec.output, writer);
	}

	private void writeMaterialArray(MaterialStack[] stacks, JsonWriter writer) throws IOException {
		writer.beginArray();
		for(MaterialStack mat : stacks) {
			writer.beginArray();
			writer.setIndent("");
			writer.value(mat.material.names[0]).value(mat.amount);
			writer.endArray();
			writer.setIndent("  ");
		}
		writer.endArray();
	}
}
