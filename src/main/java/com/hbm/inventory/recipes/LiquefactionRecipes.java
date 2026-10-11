package com.hbm.inventory.recipes;

import static com.hbm.inventory.OreDictManager.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.ModBlocksGeneric;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.crafting.SerializableRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * The liquefier table: one solid → one blob of fluid; keys may be dict names (stored as String).
 *
 * Lookup order is "exact piece → its dict name", so one item can be meta-specific and dict-wildcarded at once;
 * ItemFoods missing from the table take the fallback tier: "food value × saturation × 20" folded into SALIENT (juice).
 *
 * The consumer is A4's liquefier tile entity (not landed yet) ⇒ this table landing only means registered and template-writable.
 */
public class LiquefactionRecipes extends SerializableRecipe {

	public static HashMap<Object, FluidStack> recipes = new HashMap();

	@Override
	public void registerDefaults() {

		//oil processing
		// The old line read new FluidStack(amount, fluid); this tree's door is new FluidStack(fluid, amount) — arguments swapped
		recipes.put(COAL.gem(),											new FluidStack(Fluids.COALOIL, 100));
		recipes.put(COAL.dust(),										new FluidStack(Fluids.COALOIL, 100));
		recipes.put(LIGNITE.gem(),										new FluidStack(Fluids.COALOIL, 50));
		recipes.put(LIGNITE.dust(),										new FluidStack(Fluids.COALOIL, 50));
		recipes.put(KEY_OIL_TAR,										new FluidStack(Fluids.BITUMEN, 75));
		recipes.put(KEY_CRACK_TAR,										new FluidStack(Fluids.BITUMEN, 100));
		recipes.put(KEY_COAL_TAR,										new FluidStack(Fluids.BITUMEN, 50));
		// The laythe root and this tree's plant block (plant_flower) aren't registered ⇒ both lines keep the old intent and skip for now:
		// recipes.put(new ComparableStack(ModBlocks.laythe_root), new FluidStack(Fluids.MUG, 100));
		// recipes.put(new ComparableStack(ModBlocks.plant_flower, 1, 3), new FluidStack(Fluids.ETHANOL, 150));
		// recipes.put(new ComparableStack(ModBlocks.plant_flower, 1, 4), new FluidStack(Fluids.ETHANOL, 50));
		recipes.put(NA.dust(),											new FluidStack(Fluids.SODIUM, 100));
		recipes.put(PB.ingot(),											new FluidStack(Fluids.LEAD, 100));
		recipes.put(PB.dust(),											new FluidStack(Fluids.LEAD, 100));
		recipes.put(PB.block(),											new FluidStack(Fluids.LEAD, 900));
		//general utility recipes because why not
		recipes.put(new ComparableStack(Blocks.NETHERRACK),				new FluidStack(Fluids.LAVA, 250));
		recipes.put(KEY_COBBLESTONE,									new FluidStack(Fluids.LAVA, 250));
		recipes.put(KEY_STONE,											new FluidStack(Fluids.LAVA, 250));
		recipes.put(new ComparableStack(Blocks.OBSIDIAN),				new FluidStack(Fluids.LAVA, 500));
		recipes.put(new ComparableStack(Items.SNOWBALL),				new FluidStack(Fluids.WATER, 125));
		recipes.put(new ComparableStack(Blocks.SNOW),					new FluidStack(Fluids.WATER, 500));
		recipes.put(new ComparableStack(Blocks.ICE),					new FluidStack(Fluids.WATER, 1000));
		recipes.put(new ComparableStack(Blocks.PACKED_ICE),				new FluidStack(Fluids.WATER, 2000));
		recipes.put(new ComparableStack(ModBlocks.dry_ice),				new FluidStack(Fluids.CARBONDIOXIDE, 1000));
		recipes.put(new ComparableStack(Items.ENDER_PEARL),				new FluidStack(Fluids.ENDERJUICE, 100));
		recipes.put(new ComparableStack(ModBlocksGeneric.ore_oil_sand),	new FluidStack(Fluids.BITUMEN, 100));

		recipes.put(new ComparableStack(Items.SUGAR),					new FluidStack(Fluids.ETHANOL, 100));
		recipes.put(new ComparableStack(ModItems.biomass),				new FluidStack(Fluids.BIOGAS, 125));
		recipes.put(new ComparableStack(ModItems.glyphid_gland_empty),	new FluidStack(Fluids.BIOGAS, 2000));
		recipes.put(new ComparableStack(Items.FISH, 1, OreDictionary.WILDCARD_VALUE), new FluidStack(Fluids.FISHOIL, 100));
		recipes.put(new ComparableStack(Blocks.DOUBLE_PLANT, 1, 0),		new FluidStack(Fluids.SUNFLOWEROIL, 100));

		recipes.put(new ComparableStack(Items.WHEAT_SEEDS),				new FluidStack(Fluids.SEEDSLURRY, 50));
		recipes.put(new ComparableStack(Blocks.GRASS, 1, 1),			new FluidStack(Fluids.SEEDSLURRY, 100));
		recipes.put(new ComparableStack(Blocks.GRASS, 1, 2),			new FluidStack(Fluids.SEEDSLURRY, 100));
		recipes.put(new ComparableStack(Blocks.VINE),					new FluidStack(Fluids.SEEDSLURRY, 100));
		recipes.put(new ComparableStack(ModItems.flesh),				new FluidStack(Fluids.BLOOD, 100));
		recipes.put(new ComparableStack(ModItems.ingot_osmiridium),		new FluidStack(Fluids.ETHANOL, 24000));
	}

	public static FluidStack getOutput(ItemStack stack) {

		if(stack == null || stack.getItem() == null)
			return null;

		ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());

		if(recipes.containsKey(comp))
			return recipes.get(comp);

		String[] dictKeys = comp.getDictKeys();
		comp = new ComparableStack(stack.getItem(), 1, OreDictionary.WILDCARD_VALUE);

		if(recipes.containsKey(comp))
			return recipes.get(comp);

		for(String key : dictKeys) {

			if(recipes.containsKey(key))
				return recipes.get(key);
		}

		if(stack.getItem() instanceof ItemFood) {
			ItemFood food = (ItemFood) stack.getItem();
			float saturation = food.getHealAmount(stack) * food.getSaturationModifier(stack) * 20; //food val * saturation mod * 2 (constant) * 10 (quanta)
			return new FluidStack(Fluids.SALIENT, (int) saturation);
		}

		return null;
	}

	public static HashMap<Object, ItemStack> getRecipes() {

		HashMap<Object, ItemStack> recipes = new HashMap<Object, ItemStack>();

		for(Entry<Object, FluidStack> entry : LiquefactionRecipes.recipes.entrySet()) {

			FluidStack out = entry.getValue();

			if(entry.getKey() instanceof String) {
				recipes.put(new OreDictStack((String)entry.getKey()), ItemFluidIcon.make(out.type, out.fill));
			} else {
				recipes.put(((ComparableStack)entry.getKey()).toStack(), ItemFluidIcon.make(out.type, out.fill));
			}
		}

		return recipes;
	}

	@Override
	public String getFileName() {
		return "hbmLiquefactor.json";
	}

	@Override
	public String getComment() {
		return "As with most handlers, stacksizes for the inputs are ignored and default to 1.";
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
		AStack in = this.readAStack(obj.get("input").getAsJsonArray());
		FluidStack out = this.readFluidStack(obj.get("output").getAsJsonArray());

		if(in instanceof ComparableStack) {
			recipes.put(((ComparableStack) in).makeSingular(), out);
		} else if(in instanceof OreDictStack) {
			recipes.put(((OreDictStack) in).name, out);
		}
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<Object, FluidStack> rec = (Entry<Object, FluidStack>) recipe;
		Object key = rec.getKey();

		writer.name("input");
		if(key instanceof String) {
			this.writeAStack(new OreDictStack((String) key), writer);
		} else if(key instanceof ComparableStack) {
			this.writeAStack((ComparableStack) key, writer);
		}

		writer.name("output");
		this.writeFluidStack(rec.getValue(), writer);
	}
}
