package com.hbm.crafting;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.NBTStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.main.MainRegistry;
import com.hbm.util.ItemStackUtil;

import api.hbm.net.Tuple.Pair;
import api.hbm.recipe.IRecipeHandler;
import api.hbm.recipe.IRecipeRegisterListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The heart of the anti-spaghetti effort: one JSON lets players and custom packs rework a machine's whole recipe set.
 * Subclasses only care about "how a JSON entry becomes a recipe" and "how a recipe is written back to JSON",
 * the read/write driver, template generation and default-value fallback all live here.
 */
public abstract class SerializableRecipe implements IRecipeHandler {

	public static final Gson gson = new Gson();

	/** Loaded in registration order, and the order matters (Assembler must run before Anvil, which eats its outputs) */
	public static final List<SerializableRecipe> recipeHandlers = new ArrayList<>();
	public static final List<IRecipeRegisterListener> additionalListeners = new ArrayList<>();

	/** Recipe stream pushed by the server; on receipt it overwrites whatever was read from local files */
	public static final Map<String, InputStream> recipeSyncHandlers = new HashMap<>();

	public boolean modified = false;

	/*
	 * Registration and init
	 */

	/** Registers a recipe family. Registration order is load order; don't shuffle it. */
	public static void addHandler(SerializableRecipe handler) {
		recipeHandlers.add(handler);
	}

	public static void addListener(IRecipeRegisterListener listener) {
		additionalListeners.add(listener);
	}

	/**
	 * Recipes live in config/hbmRecipes. If a same-named file exists there, read it (the player-edited one);
	 * otherwise register the built-in defaults, notify the listeners, then write the defaults out as an underscore-prefixed template (never auto-read).
	 */
	public static void initialize() {
		File recDir = new File(MainRegistry.configDir, "hbmRecipes");

		if(!recDir.exists() && !recDir.mkdirs()) {
			throw new IllegalStateException("Cannot create recipe directory " + recDir.getAbsolutePath());
		}

		File info = new File(recDir, "REMOVE UNDERSCORE TO ENABLE RECIPE LOADING - RECIPES WILL RESET TO DEFAULT OTHERWISE");
		try { info.createNewFile(); } catch(IOException e) { }

		MainRegistry.logger.info("Starting recipe init!");

		for(SerializableRecipe recipe : recipeHandlers) {

			recipe.deleteRecipes();

			File recFile = new File(recDir, recipe.getFileName());
			if(recipeSyncHandlers.containsKey(recipe.getFileName())) {
				MainRegistry.logger.info("Reading synced recipe file {}", recipe.getFileName());
				InputStream stream = recipeSyncHandlers.get(recipe.getFileName());
				try {
					stream.reset();
					recipe.readRecipeStream(new InputStreamReader(stream));
					recipe.modified = true;
				} catch(Throwable ex) {
					MainRegistry.logger.error("Failed to reset synced recipe stream", ex);
				}
			} else if(recFile.exists() && recFile.isFile()) {
				MainRegistry.logger.info("Reading recipe file {}", recFile.getName());
				recipe.readRecipeFile(recFile);
				recipe.modified = true;
			} else {
				MainRegistry.logger.info("No recipe file found, registering defaults for {}", recipe.getFileName());
				recipe.registerDefaults();

				for(IRecipeRegisterListener listener : additionalListeners) {
					listener.onRecipeLoad(recipe.getClass().getSimpleName());
				}

				File recTemplate = new File(recDir, "_" + recipe.getFileName());
				MainRegistry.logger.info("Writing template file {}", recTemplate.getName());
				recipe.writeTemplateFile(recTemplate);
				recipe.modified = false;
			}

			recipe.registerPost();
		}

		MainRegistry.logger.info("Finished recipe init!");
	}

	public static void receiveRecipes(String filename, byte[] data) {
		recipeSyncHandlers.put(filename, new ByteArrayInputStream(data));
	}

	public static void clearReceivedRecipes() {
		boolean hasCleared = !recipeSyncHandlers.isEmpty();
		recipeSyncHandlers.clear();
		if(hasCleared) initialize();
	}

	/*
	 * Template write / read
	 */

	public void writeTemplateFile(File template) {

		try {
			Object recipeObject = this.getRecipeObject();
			List<Object> recipeList = new ArrayList<>();

			if(recipeObject instanceof Collection) {
				recipeList.addAll((Collection<?>) recipeObject);
			} else if(recipeObject instanceof Map) {
				recipeList.addAll(((Map<?, ?>) recipeObject).entrySet());
			}

			if(recipeList.isEmpty() && !allowEmptyRecipeList()) {
				throw new IllegalStateException("Failed to write recipe " + this.getClass().getSimpleName() + ": recipe table empty or format unsupported");
			}

			JsonWriter writer = new JsonWriter(new FileWriter(template));
			writer.setIndent("  ");
			writer.beginObject();

			if(this.getComment() != null) {
				writer.name("comment").value(this.getComment());
			}

			writer.name("recipes").beginArray();

			for(Object recipe : recipeList) {
				writer.beginObject();
				this.writeRecipe(recipe, writer);
				writer.endObject();
			}

			writer.endArray();
			writer.endObject();
			writer.close();
		} catch(Exception ex) {
			MainRegistry.logger.error("Failed to write recipe template for {}", this.getFileName(), ex);
		}
	}

	public void readRecipeFile(File file) {
		try {
			readRecipeStream(new FileReader(file));
		} catch(FileNotFoundException ex) {
			// The caller already checked existence; reaching this line means it was deleted in the exact meantime — ignore and move on
		}
	}

	public void readRecipeStream(Reader reader) {
		JsonObject json = gson.fromJson(reader, JsonObject.class);
		if(json == null || !json.has("recipes")) return;
		JsonArray recipes = json.get("recipes").getAsJsonArray();
		for(JsonElement recipe : recipes) {
			if(recipe != null) this.readRecipe(recipe);
		}
	}

	/*
	 * JSON read/write helpers. Field order in the arrays is the order recipe files use, bit-for-bit with the old version —
	 * the defaults table only parses if it's copied over as-is; reordering it rewrites every recipe file.
	 */

	public static ItemStack readItemStack(JsonArray array) {
		try {
			Item item = ItemStackUtil.getItem(array.get(0).getAsString());
			int stacksize = array.size() > 1 ? array.get(1).getAsInt() : 1;
			int meta = array.size() > 2 ? array.get(2).getAsInt() : 0;
			if(item != null) {
				ItemStack stack = new ItemStack(item, stacksize, meta);
				if(array.size() > 3) ItemStackUtil.addNBTFromString(stack, array.get(3).getAsString());
				return stack;
			}
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading stack array {}", array);
		return ItemStack.EMPTY;
	}

	public static ItemStack[] readItemStackArray(JsonArray array) {
		try {
			ItemStack[] items = new ItemStack[array.size()];
			for(int i = 0; i < items.length; i++) items[i] = readItemStack((JsonArray) array.get(i));
			return items;
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading stack array {}", array);
		return new ItemStack[0];
	}

	public static Pair<ItemStack, Float> readItemStackChance(JsonArray array) {
		try {
			Item item = ItemStackUtil.getItem(array.get(0).getAsString());
			int stacksize = array.size() > 2 ? array.get(1).getAsInt() : 1;
			int meta = array.size() > 3 ? array.get(2).getAsInt() : 0;
			if(item != null) {
				ItemStack stack = new ItemStack(item, stacksize, meta);
				if(array.size() > 4) ItemStackUtil.addNBTFromString(stack, array.get(3).getAsString());
				float chance = array.get(array.size() - 1).getAsFloat();
				return new Pair<>(stack, chance);
			}
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading stack array {}", array);
		return new Pair<>(ItemStack.EMPTY, 1F);
	}

	@SuppressWarnings("unchecked")
	public static Pair<ItemStack, Float>[] readItemStackArrayChance(JsonArray array) {
		try {
			Pair<ItemStack, Float>[] items = new Pair[array.size()];
			for(int i = 0; i < items.length; i++) items[i] = readItemStackChance((JsonArray) array.get(i));
			return items;
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading stack array {}", array);
		return new Pair[0];
	}

	public static void writeItemStack(ItemStack stack, JsonWriter writer) throws IOException {
		writer.beginArray();
		writer.setIndent("");
		writer.value(ItemStackUtil.getName(stack.getItem()));
		if(stack.getCount() != 1 || stack.getMetadata() != 0 || stack.hasTagCompound()) writer.value(stack.getCount());
		if(stack.getMetadata() != 0 || stack.hasTagCompound()) writer.value(stack.getMetadata());
		if(stack.hasTagCompound()) writer.value(stack.getTagCompound().toString());
		writer.endArray();
		writer.setIndent("  ");
	}

	public static void writeItemStackChance(Pair<ItemStack, Float> stack, JsonWriter writer) throws IOException {
		writer.beginArray();
		writer.setIndent("");
		writer.value(ItemStackUtil.getName(stack.getKey().getItem()));
		if(stack.getKey().getCount() != 1 || stack.getKey().getMetadata() != 0 || stack.getKey().hasTagCompound()) writer.value(stack.getKey().getCount());
		if(stack.getKey().getMetadata() != 0 || stack.getKey().hasTagCompound()) writer.value(stack.getKey().getMetadata());
		if(stack.getKey().hasTagCompound()) writer.value(stack.getKey().getTagCompound().toString());
		writer.value(stack.value);
		writer.endArray();
		writer.setIndent("  ");
	}

	/**
	 * Ingredient stack. Array form is [type, name, count, durability, NBT]; type is one of item / dict / nbt,
	 * later fields as the type requires; defaults are count 1, durability 0, no NBT.
	 */
	public static AStack readAStack(JsonArray array) {
		try {
			String type = array.get(0).getAsString();
			int stacksize = array.size() > 2 ? array.get(2).getAsInt() : 1;

			if("nbt".equals(type)) {
				Item item = ItemStackUtil.getItem(array.get(1).getAsString());
				NBTBase nbt = array.size() > 4 ? JsonToNBT.getTagFromJson(array.get(4).getAsString()) : null;
				return new NBTStack(item, stacksize, 0).withNBT(nbt instanceof NBTTagCompound ? (NBTTagCompound) nbt : null);
			}
			if("item".equals(type)) {
				Item item = ItemStackUtil.getItem(array.get(1).getAsString());
				int meta = array.size() > 3 ? array.get(3).getAsInt() : 0;
				return new ComparableStack(item, stacksize, meta);
			}
			if("dict".equals(type)) {
				return new OreDictStack(array.get(1).getAsString(), stacksize);
			}
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading stack array {}", array);
		return new ComparableStack((Item) null);
	}

	public static AStack[] readAStackArray(JsonArray array) {
		try {
			AStack[] stacks = new AStack[array.size()];
			for(int i = 0; i < stacks.length; i++) stacks[i] = readAStack((JsonArray) array.get(i));
			return stacks;
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading stack array {}", array);
		return new AStack[0];
	}

	/** NBTStack extends ComparableStack, so the ordering check can't be swapped — NBT ones would grade as plain */
	public static void writeAStack(AStack astack, JsonWriter writer) throws IOException {
		writer.beginArray();
		writer.setIndent("");

		if(astack instanceof NBTStack) {
			NBTStack comp = (NBTStack) astack;
			writer.value(comp.nbt != null ? "nbt" : "item");
			writer.value(ItemStackUtil.getName(comp.item));
			if(comp.stacksize != 1 || comp.meta > 0 || comp.nbt != null) writer.value(comp.stacksize);
			if(comp.meta > 0 || comp.nbt != null) writer.value(comp.meta);
			if(comp.nbt != null) writer.value(comp.nbt.toString());
		} else if(astack instanceof ComparableStack) {
			ComparableStack comp = (ComparableStack) astack;
			writer.value("item");
			writer.value(ItemStackUtil.getName(comp.item));
			if(comp.stacksize != 1 || comp.meta > 0) writer.value(comp.stacksize);
			if(comp.meta > 0) writer.value(comp.meta);
		} else if(astack instanceof OreDictStack) {
			OreDictStack ore = (OreDictStack) astack;
			writer.value("dict");
			writer.value(ore.name);
			if(ore.stacksize != 1) writer.value(ore.stacksize);
		}

		writer.endArray();
		writer.setIndent("  ");
	}

	/** Fluid stacks are written [name, mB, pressure]; pressure defaults to 0 */
	public static FluidStack readFluidStack(JsonArray array) {
		try {
			FluidType type = Fluids.fromName(array.get(0).getAsString());
			int fill = array.get(1).getAsInt();
			int pressure = array.size() < 3 ? 0 : array.get(2).getAsInt();
			return new FluidStack(type, fill, pressure);
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading fluid array {}", array);
		return new FluidStack(Fluids.NONE, 0);
	}

	public static FluidStack[] readFluidArray(JsonArray array) {
		try {
			FluidStack[] fluids = new FluidStack[array.size()];
			for(int i = 0; i < fluids.length; i++) fluids[i] = readFluidStack((JsonArray) array.get(i));
			return fluids;
		} catch(Exception ex) { }
		MainRegistry.logger.error("Error reading fluid array {}", array);
		return new FluidStack[0];
	}

	public static void writeFluidStack(FluidStack stack, JsonWriter writer) throws IOException {
		writer.beginArray();
		writer.setIndent("");
		writer.value(stack.type.getName());
		writer.value(stack.fill);
		if(stack.pressure != 0) writer.value(stack.pressure);
		writer.endArray();
		writer.setIndent("  ");
	}

	/**
	 * Whether a set of inputs consumes exactly one ingredient list: every input must find a match, and the list must come out empty.
	 * <br><br>Counts are "at least" (ignoreSize passes true), because machine slots usually hold more than the recipe asks for.
	 */
	public static boolean matchesIngredients(ItemStack[] inputs, AStack[] recipe) {

		List<AStack> remaining = new ArrayList<>();
		for(AStack ingredient : recipe) remaining.add(ingredient);

		for(ItemStack input : inputs) {

			if(input == null || input.isEmpty()) continue;

			boolean hasMatch = false;
			Iterator<AStack> it = remaining.iterator();

			while(it.hasNext()) {
				AStack stack = it.next();

				if(stack.matchesRecipe(input, true) && input.getCount() >= stack.stacksize) {
					it.remove();
					hasMatch = true;
					break;
				}
			}

			if(!hasMatch) return false;
		}

		return remaining.isEmpty();
	}
}
