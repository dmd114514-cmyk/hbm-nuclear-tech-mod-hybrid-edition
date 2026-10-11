package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.crafting.SerializableRecipe;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel;

import net.minecraft.item.ItemStack;

/**
 * The fuel pool's (spent fuel cooling pool) disassembly recipes: hot pieces go in, cooled products come out.
 */
public class FuelPoolRecipes extends SerializableRecipe {

	public static final HashMap<ComparableStack, ItemStack> recipes = new HashMap<ComparableStack, ItemStack>();
	public static final FuelPoolRecipes instance = new FuelPoolRecipes();

	@Override
	public void registerDefaults() {
		recipes.put(new ComparableStack(ModItems.waste_natural_uranium, 1, 1), new ItemStack(ModItems.waste_natural_uranium));
		recipes.put(new ComparableStack(ModItems.waste_uranium, 1, 1), new ItemStack(ModItems.waste_uranium));
		recipes.put(new ComparableStack(ModItems.waste_thorium, 1, 1), new ItemStack(ModItems.waste_thorium));
		recipes.put(new ComparableStack(ModItems.waste_mox, 1, 1), new ItemStack(ModItems.waste_mox));
		recipes.put(new ComparableStack(ModItems.waste_plutonium, 1, 1), new ItemStack(ModItems.waste_plutonium));
		recipes.put(new ComparableStack(ModItems.waste_u233, 1, 1), new ItemStack(ModItems.waste_u233));
		recipes.put(new ComparableStack(ModItems.waste_u235, 1, 1), new ItemStack(ModItems.waste_u235));
		recipes.put(new ComparableStack(ModItems.waste_schrabidium, 1, 1), new ItemStack(ModItems.waste_schrabidium));
		recipes.put(new ComparableStack(ModItems.waste_zfb_mox, 1, 1), new ItemStack(ModItems.waste_zfb_mox));
		recipes.put(new ComparableStack(ModItems.waste_plate_u233, 1, 1), new ItemStack(ModItems.waste_plate_u233));
		recipes.put(new ComparableStack(ModItems.waste_plate_u235, 1, 1), new ItemStack(ModItems.waste_plate_u235));
		recipes.put(new ComparableStack(ModItems.waste_plate_mox, 1, 1), new ItemStack(ModItems.waste_plate_mox));
		recipes.put(new ComparableStack(ModItems.waste_plate_pu239, 1, 1), new ItemStack(ModItems.waste_plate_pu239));
		recipes.put(new ComparableStack(ModItems.waste_plate_sa326, 1, 1), new ItemStack(ModItems.waste_plate_sa326));
		recipes.put(new ComparableStack(ModItems.waste_plate_ra226be, 1, 1), new ItemStack(ModItems.waste_plate_ra226be));
		recipes.put(new ComparableStack(ModItems.waste_plate_pu238be, 1, 1), new ItemStack(ModItems.waste_plate_pu238be));

		for(EnumPWRFuel pwr : EnumPWRFuel.values()) recipes.put(new ComparableStack(ModItems.pwr_fuel_hot, 1, pwr.ordinal()), new ItemStack(ModItems.pwr_fuel_depleted, 1, pwr.ordinal()));

		// Skipped, noted: the old version also had an RBMK fuel rod disassembly drain here, keying on ItemRBMKRod.craftableRods with
		// handler.nei.RBMKRodDisassemblyHandler.ComparableStackHeat as the key. Two reasons it's held:
		// ItemRBMKRod depends on the whole com.hbm.tileentity.machine.rbmk package (RBMKDials, IRBMKFluxReceiver.NType),
	}

	@Override
	public String getFileName() {
		return "hbmFuelpool.json";
	}

	@Override
	public Object getRecipeObject() {
		return recipes;
	}

	@Override
	public void deleteRecipes() {
		recipes.clear();
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;
		ItemStack in = this.readItemStack(obj.get("input").getAsJsonArray());
		ItemStack out = this.readItemStack(obj.get("output").getAsJsonArray());
		recipes.put(new ComparableStack(in), out);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<ComparableStack, ItemStack> entry = (Entry<ComparableStack, ItemStack>) recipe;

		writer.name("input");
		this.writeItemStack(entry.getKey().toStack(), writer);
		writer.name("output");
		this.writeItemStack(entry.getValue(), writer);
	}
}
