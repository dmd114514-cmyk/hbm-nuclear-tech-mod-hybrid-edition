package com.hbm.inventory.recipes.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.crafting.SerializableRecipe;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.util.ItemStackUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.text.TextFormatting;

/**
 * The generic recipes' table: stored by name, emitted in registration order, minding blueprint pools (blueprint unlocks) and auto-groups along the way.
 *
 * The per-machine caps (the inputItemLimit set) decide whether the JSON writes a column and whether reads skip it,
 * so "this machine has no fluid input" is a subclass returning 0, not an if duplicated on both the read and write sides.
 */
public abstract class GenericRecipes<T extends GenericRecipe> extends SerializableRecipe {

	public static final Random RNG = new Random();

	/** Recipes other machines can also make (another production line, say) */
	public static final String POOL_PREFIX_ALT = "alt.";
	/** Recipes only makeable once a blueprint unlocks them */
	public static final String POOL_PREFIX_DISCOVER = "discover.";
	/** Hidden recipes */
	public static final String POOL_PREFIX_SECRET = "secret.";
	/** The 528 blueprint tier */
	public static final String POOL_PREFIX_528 = "528.";

	public List<T> recipeOrderedList = new ArrayList<T>();
	public HashMap<String, T> recipeNameMap = new HashMap<String, T>();

	/** Blueprint pool name → the recipe names inside */
	public static HashMap<String, List<String>> blueprintPools = new HashMap<String, List<String>>();
	/** Every pooled recipe also stored by name, so the pool side can look them back up */
	public static HashMap<String, GenericRecipe> nameToRecipeGlobal = new HashMap<String, GenericRecipe>();

	/** Auto-grouping: within a group, recipes picked by the first solid ingredient */
	public HashMap<String, List<GenericRecipe>> autoSwitchGroups = new HashMap<String, List<GenericRecipe>>();

	public abstract int inputItemLimit();
	public abstract int inputFluidLimit();
	public abstract int outputItemLimit();
	public abstract int outputFluidLimit();
	public boolean hasDuration() { return true; }
	public boolean hasPower() { return true; }

	/** Hangs a recipe into a blueprint pool (i.e. a blueprint item's usable list) */
	public static void addToPool(String pool, GenericRecipe recipe) {
		List<String> list = blueprintPools.get(pool);
		if(list == null) {
			list = new ArrayList<String>();
			blueprintPools.put(pool, list);
		}
		list.add(recipe.name);
	}

	public void addToGroup(String group, GenericRecipe recipe) {
		List<GenericRecipe> list = autoSwitchGroups.get(group);
		if(list == null) list = new ArrayList<GenericRecipe>();
		list.add(recipe);
		autoSwitchGroups.put(group, list);
	}

	public static void clearPools() {
		blueprintPools.clear();
		nameToRecipeGlobal.clear();
	}

	@Override
	public Object getRecipeObject() {
		return this.recipeOrderedList;
	}

	@Override
	public void deleteRecipes() {
		this.recipeOrderedList.clear();
		this.recipeNameMap.clear();
		this.autoSwitchGroups.clear();
	}

	/** Name collisions detonate outright: two tables clashing lets the later entry silently eat the earlier one — far harder to find than an error */
	public void register(T recipe) {
		this.recipeOrderedList.add(recipe);
		if(recipeNameMap.containsKey(recipe.name)) throw new IllegalStateException("Recipe " + recipe.name + " has been registered with a duplicate ID!");
		this.recipeNameMap.put(recipe.name, recipe);
		nameToRecipeGlobal.put(recipe.name, recipe);
	}

	@Override
	public void readRecipe(JsonElement element) {
		JsonObject obj = (JsonObject) element;
		T recipe = instantiateRecipe(obj.get("name").getAsString());

		if(this.inputItemLimit() > 0 && obj.has("inputItem")) recipe.inputItem = readAStackArray(obj.get("inputItem").getAsJsonArray());
		if(this.inputFluidLimit() > 0 && obj.has("inputFluid")) recipe.inputFluid = readFluidArray(obj.get("inputFluid").getAsJsonArray());

		if(this.outputItemLimit() > 0 && obj.has("outputItem")) recipe.outputItem = this.readOutputArray(obj.get("outputItem").getAsJsonArray());
		if(this.outputFluidLimit() > 0 && obj.has("outputFluid")) recipe.outputFluid = readFluidArray(obj.get("outputFluid").getAsJsonArray());

		if(this.hasDuration()) recipe.setDuration(obj.get("duration").getAsInt());
		if(this.hasPower()) recipe.setPower(obj.get("power").getAsLong());

		if(obj.has("icon")) recipe.setIcon(readItemStack(obj.get("icon").getAsJsonArray()));
		if(obj.has("named") && obj.get("named").getAsBoolean()) recipe.setNamed();
		if(obj.has("blueprintpool")) recipe.setPoolsAllow528(obj.get("blueprintpool").getAsString().split(":"));
		if(obj.has("nameWrapper")) recipe.setNameWrapper(obj.get("nameWrapper").getAsString());
		if(obj.has("autoSwitchGroup")) recipe.setGroup(obj.get("autoSwitchGroup").getAsString(), this);

		readExtraData(element, recipe);

		register(recipe);
	}

	public abstract T instantiateRecipe(String name);
	public void readExtraData(JsonElement element, T recipe) { }

	@Override
	public void writeRecipe(Object recipeObject, JsonWriter writer) throws IOException {
		T recipe = (T) recipeObject;

		writer.name("name").value(recipe.name);

		if(this.inputItemLimit() > 0 && recipe.inputItem != null) {
			writer.name("inputItem").beginArray();
			for(AStack stack : recipe.inputItem) writeAStack(stack, writer);
			writer.endArray();
		}

		if(this.inputFluidLimit() > 0 && recipe.inputFluid != null) {
			writer.name("inputFluid").beginArray();
			for(FluidStack stack : recipe.inputFluid) writeFluidStack(stack, writer);
			writer.endArray();
		}

		if(this.outputItemLimit() > 0 && recipe.outputItem != null) {
			writer.name("outputItem").beginArray();
			for(IOutput stack : recipe.outputItem) stack.serialize(writer);
			writer.endArray();
		}

		if(this.outputFluidLimit() > 0 && recipe.outputFluid != null) {
			writer.name("outputFluid").beginArray();
			for(FluidStack stack : recipe.outputFluid) writeFluidStack(stack, writer);
			writer.endArray();
		}

		if(this.hasDuration()) writer.name("duration").value(recipe.duration);
		if(this.hasPower()) writer.name("power").value(recipe.power);

		if(recipe.writeIcon) {
			writer.name("icon");
			writeItemStack(recipe.icon, writer);
		}

		if(recipe.customLocalization) writer.name("named").value(true);
		if(recipe.nameWrapper != null) writer.name("nameWrapper").value(recipe.nameWrapper);
		if(recipe.blueprintPools != null && recipe.blueprintPools.length > 0) writer.name("blueprintpool").value(String.join(":", recipe.blueprintPools));
		if(recipe.autoSwitchGroup != null) writer.name("autoSwitchGroup").value(recipe.autoSwitchGroup);

		writeExtraData(recipe, writer);
	}

	public void writeExtraData(T recipe, JsonWriter writer) throws IOException { }

	public IOutput[] readOutputArray(JsonArray array) {
		IOutput[] output = new IOutput[array.size()];
		int index = 0;

		for(JsonElement element : array) {
			JsonArray arrayElement = element.getAsJsonArray();
			String type = arrayElement.get(0).getAsString();
			if("single".equals(type)) {
				ChanceOutput co = new ChanceOutput();
				co.deserialize(arrayElement);
				output[index] = co;
			} else if("multi".equals(type)) {
				ChanceOutputMulti com = new ChanceOutputMulti();
				com.deserialize(arrayElement);
				output[index] = com;
			} else {
				throw new IllegalArgumentException("Invalid IOutput type '" + type + "', expected 'single' or 'multi' for recipe " + array.toString());
			}
			index++;
		}

		return output;
	}

	/**
	 * The output cell's two shapes: one fixed item, or one weight-drawn from a pile.
	 * Serialized as `[["single" or omitted, [items...] , chance, weight], ...]`,
	 * reads tell the single flag by whether the first element is a string, so both writings parse.
	 */
	public static interface IOutput {

		/** Only a ChanceOutputMulti with pool > 1 is a true multi-pick */
		public boolean possibleMultiOutput();
		/** Rolls the output once, returning a copy of the result (possibly null) */
		public ItemStack collapse();
		/** Only meaningful when possibleMultiOutput is false; the multi tier returns null */
		public ItemStack getSingle();
		public ItemStack[] getAllPossibilities();
		public void serialize(JsonWriter writer) throws IOException;
		public void deserialize(JsonArray array);
		public String[] getLabel();
	}

	/** One output with a "does it even happen" chance */
	public static class ChanceOutput extends WeightedRandom.Item implements IOutput {

		// weight 0 means this piece stays out of the weighted draw — a fixed output

		public ItemStack stack;
		public float chance = 1F;

		public ChanceOutput() { super(0); }
		public ChanceOutput(ItemStack stack) { this(stack, 1F, 0); }
		public ChanceOutput(ItemStack stack, int weight) { this(stack, 1F, weight); }
		public ChanceOutput(ItemStack stack, float chance) { this(stack, chance, 0); }
		public ChanceOutput(ItemStack stack, float chance, int weight) {
			super(weight);
			this.stack = stack;
			this.chance = chance;
		}

		@Override
		public ItemStack collapse() {
			if(this.chance >= 1F) return getSingle();
			// rolled per piece: the chance applies to each item, not all-or-nothing
			int finalSize = 0;
			for(int i = 0; i < this.stack.getCount(); i++) if(RNG.nextFloat() <= chance) finalSize++;
			if(finalSize <= 0) return null;
			ItemStack finalStack = getSingle();
			finalStack.setCount(finalSize);
			return finalStack;
		}

		@Override public ItemStack getSingle() { return this.stack.copy(); }
		@Override public boolean possibleMultiOutput() { return false; }
		@Override public ItemStack[] getAllPossibilities() {
			return new ItemStack[] { this.chance >= 1F ? getSingle() : ItemStackUtil.addTooltipToStack(getSingle(), TextFormatting.RED + "" + (int)(this.chance * 1000) / 10F + "%") };
		}

		@Override
		public void serialize(JsonWriter writer) throws IOException {
			boolean standardStack = chance >= 1 && itemWeight == 0;

			writer.beginArray();
			writer.setIndent("");

			if(itemWeight == 0) writer.value("single");
			writeItemStack(stack, writer);
			writer.setIndent("");

			if(!standardStack) {
				writer.value(chance);
				if(itemWeight > 0) writer.value(itemWeight);
			}

			writer.endArray();
			writer.setIndent("  ");
		}

		@Override
		public void deserialize(JsonArray array) {

			if(array.get(0).isJsonPrimitive()) { // carries the "single" marker, weight untouched
				this.stack = readItemStack(array.get(1).getAsJsonArray());
				if(array.size() > 2) this.chance = array.get(2).getAsFloat();
			} else {
				this.stack = readItemStack(array.get(0).getAsJsonArray());
				if(array.size() > 1) this.chance = array.get(1).getAsFloat();
				if(array.size() > 2) this.itemWeight = array.get(2).getAsInt();
			}
		}

		@Override
		public String[] getLabel() {
			return new String[] { TextFormatting.GRAY + "" + this.stack.getCount() + "x " + this.stack.getDisplayName() + (this.chance >= 1 ? "" : " (" + (int)(this.chance * 1000) / 10F + "%)") };
		}
	}

	/** The pick-one output: weight-draws one from the pool, then lets that piece roll its own chance */
	public static class ChanceOutputMulti implements IOutput {

		public List<ChanceOutput> pool = new ArrayList<ChanceOutput>();

		public ChanceOutputMulti(ChanceOutput... out) {
			for(ChanceOutput output : out) pool.add(output);
		}

		@Override public ItemStack collapse() { return WeightedRandom.getRandomItem(RNG, pool).collapse(); }
		@Override public boolean possibleMultiOutput() { return pool.size() > 1; }
		@Override public ItemStack getSingle() { return possibleMultiOutput() ? null : pool.get(0).getSingle(); }

		@Override public ItemStack[] getAllPossibilities() {
			ItemStack[] outputs = new ItemStack[pool.size()];
			int totalWeight = WeightedRandom.getTotalWeight(pool);
			for(int i = 0; i < outputs.length; i++) {
				ChanceOutput out = pool.get(i);
				float chance = (float) out.itemWeight / (float) totalWeight;
				outputs[i] = chance >= 1 ? out.getAllPossibilities()[0] :
					ItemStackUtil.addTooltipToStack(out.getAllPossibilities()[0], TextFormatting.RED + "" + (int)(chance * 1000) / 10F + "%");
			}
			return outputs;
		}

		@Override
		public void serialize(JsonWriter writer) throws IOException {
			writer.beginArray();
			writer.value("multi");
			for(ChanceOutput output : pool) output.serialize(writer);
			writer.endArray();
		}

		@Override
		public void deserialize(JsonArray array) {
			for(JsonElement element : array) {
				if(element.isJsonPrimitive()) continue; // the array carries the "multi" marker, which is also its only raw value
				ChanceOutput output = new ChanceOutput();
				output.deserialize(element.getAsJsonArray());
				pool.add(output);
			}
		}

		@Override
		public String[] getLabel() {
			String[] label = new String[pool.size() + 1];
			label[0] = "One of:";
			int totalWeight = WeightedRandom.getTotalWeight(pool);
			for(int i = 1; i < label.length; i++) {
				ChanceOutput output = pool.get(i - 1);
				float chance = (float) output.itemWeight / (float) totalWeight * output.chance;
				label[i] = "  " + TextFormatting.GRAY + output.stack.getCount() + "x " + output.stack.getDisplayName() + " (" + (int)(chance * 1000F) / 10F + "%)";
			}
			return label;
		}
	}
}
