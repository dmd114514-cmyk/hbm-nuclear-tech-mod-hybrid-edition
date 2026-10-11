package com.hbm.handler;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import com.hbm.util.Compat;
import com.hbm.util.ShadyUtil;
import api.hbm.net.Tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * How much radiation each piece of gear blocks.
 *
 * The table stores "resistance coefficients", not "percent reductions": pieces add up before converting,
 * so a heavy chestplate plus boots can out-shield any single piece. Values copy the old table,
 * and the percent-conversion comments stay, so number changes can be checked against both.
 *
 * Beyond the default table it also reads hbmRadResist.json from the config directory, letting server owners override the whole table —
 * the first run writes out an underscore-prefixed template instead of taking effect directly.
 */
public class HazmatRegistry {

	/** Other mods adding protection to their gear drop it here; initDefault folds them into the table */
	public static List<Pair<Item, Double>> external = new ArrayList();

	public static double helmet = 0.2D;
	public static double chest = 0.4D;
	public static double legs = 0.3D;
	public static double boots = 0.1D;

	public static void initDefault() {

		for(Pair<Item, Double> pair : external) {
			registerHazmat(pair.getKey(), pair.getValue());
		}

		// The coefficient was written for 10 but measures 5; left as found
		double iron = 0.0225D; // 5%
		double gold = 0.0225D; // 5%
		double steel = 0.045D; // 10%
		double titanium = 0.045D; // 10%
		double alloy = 0.07D; // 15%
		double cobalt = 0.125D; // 25%

		double hazYellow = 0.6D; // 50%
		double hazRed = 1.0D; // 90%
		double hazGray = 2D; // 99%
		double paa = 1.7D; // 97%
		double liquidator = 2.4D; // 99.6%

		double security = 0.825D; // 85%
		double star = 1D; // 90%
		double cmb = 1.3D; // 95%
		double schrab = 3D; // 99.9%
		double euph = 10D; // <100%

		registerHazmat(ModItems.hazmat_helmet, hazYellow * helmet);
		registerHazmat(ModItems.hazmat_plate, hazYellow * chest);
		registerHazmat(ModItems.hazmat_legs, hazYellow * legs);
		registerHazmat(ModItems.hazmat_boots, hazYellow * boots);

		registerHazmat(ModItems.hazmat_helmet_red, hazRed * helmet);
		registerHazmat(ModItems.hazmat_plate_red, hazRed * chest);
		registerHazmat(ModItems.hazmat_legs_red, hazRed * legs);
		registerHazmat(ModItems.hazmat_boots_red, hazRed * boots);

		registerHazmat(ModItems.hazmat_helmet_grey, hazGray * helmet);
		registerHazmat(ModItems.hazmat_plate_grey, hazGray * chest);
		registerHazmat(ModItems.hazmat_legs_grey, hazGray * legs);
		registerHazmat(ModItems.hazmat_boots_grey, hazGray * boots);

		registerHazmat(ModItems.liquidator_helmet, liquidator * helmet);
		registerHazmat(ModItems.liquidator_plate, liquidator * chest);
		registerHazmat(ModItems.liquidator_legs, liquidator * legs);
		registerHazmat(ModItems.liquidator_boots, liquidator * boots);

		registerHazmat(ModItems.paa_plate, paa * chest);
		registerHazmat(ModItems.paa_legs, paa * legs);
		registerHazmat(ModItems.paa_boots, paa * boots);

		registerHazmat(ModItems.hazmat_paa_helmet, paa * helmet);
		registerHazmat(ModItems.hazmat_paa_plate, paa * chest);
		registerHazmat(ModItems.hazmat_paa_legs, paa * legs);
		registerHazmat(ModItems.hazmat_paa_boots, paa * boots);

		registerHazmat(ModItems.security_helmet, security * helmet);
		registerHazmat(ModItems.security_plate, security * chest);
		registerHazmat(ModItems.security_legs, security * legs);
		registerHazmat(ModItems.security_boots, security * boots);

		registerHazmat(ModItems.starmetal_helmet, star * helmet);
		registerHazmat(ModItems.starmetal_plate, star * chest);
		registerHazmat(ModItems.starmetal_legs, star * legs);
		registerHazmat(ModItems.starmetal_boots, star * boots);

		registerHazmat(ModItems.jackt, 0.1);
		registerHazmat(ModItems.jackt2, 0.1);

		registerHazmat(ModItems.gas_mask, 0.07);
		registerHazmat(ModItems.gas_mask_m65, 0.095);

		registerHazmat(ModItems.steel_helmet, steel * helmet);
		registerHazmat(ModItems.steel_plate, steel * chest);
		registerHazmat(ModItems.steel_legs, steel * legs);
		registerHazmat(ModItems.steel_boots, steel * boots);

		registerHazmat(ModItems.titanium_helmet, titanium * helmet);
		registerHazmat(ModItems.titanium_plate, titanium * chest);
		registerHazmat(ModItems.titanium_legs, titanium * legs);
		registerHazmat(ModItems.titanium_boots, titanium * boots);

		registerHazmat(ModItems.cobalt_helmet, cobalt * helmet);
		registerHazmat(ModItems.cobalt_plate, cobalt * chest);
		registerHazmat(ModItems.cobalt_legs, cobalt * legs);
		registerHazmat(ModItems.cobalt_boots, cobalt * boots);

		registerHazmat(Items.IRON_HELMET, iron * helmet);
		registerHazmat(Items.IRON_CHESTPLATE, iron * chest);
		registerHazmat(Items.IRON_LEGGINGS, iron * legs);
		registerHazmat(Items.IRON_BOOTS, iron * boots);

		registerHazmat(Items.GOLDEN_HELMET, gold * helmet);
		registerHazmat(Items.GOLDEN_CHESTPLATE, gold * chest);
		registerHazmat(Items.GOLDEN_LEGGINGS, gold * legs);
		registerHazmat(Items.GOLDEN_BOOTS, gold * boots);

		registerHazmat(ModItems.alloy_helmet, alloy * helmet);
		registerHazmat(ModItems.alloy_plate, alloy * chest);
		registerHazmat(ModItems.alloy_legs, alloy * legs);
		registerHazmat(ModItems.alloy_boots, alloy * boots);

		registerHazmat(ModItems.cmb_helmet, cmb * helmet);
		registerHazmat(ModItems.cmb_plate, cmb * chest);
		registerHazmat(ModItems.cmb_legs, cmb * legs);
		registerHazmat(ModItems.cmb_boots, cmb * boots);

		registerHazmat(ModItems.schrabidium_helmet, schrab * helmet);
		registerHazmat(ModItems.schrabidium_plate, schrab * chest);
		registerHazmat(ModItems.schrabidium_legs, schrab * legs);
		registerHazmat(ModItems.schrabidium_boots, schrab * boots);

		registerHazmat(ModItems.euphemium_helmet, euph * helmet);
		registerHazmat(ModItems.euphemium_plate, euph * chest);
		registerHazmat(ModItems.euphemium_legs, euph * legs);
		registerHazmat(ModItems.euphemium_boots, euph * boots);

		Compat.registerCompatHazmat();
	}

	private static HashMap<Item, Double> entries = new HashMap();

	public static void registerHazmat(Item item, double resistance) {

		if(item == null)
			return;

		entries.put(item, resistance);
	}

	public static double getResistance(ItemStack stack) {

		if(stack == null)
			return 0;

		double cladding = getCladding(stack);

		Double f = entries.get(stack.getItem());

		if(f != null)
			return f + cladding;

		return cladding;
	}

	/**
	 * Extra protection from cladding materials. Reads hfr_cladding from the item's NBT,
	 * computed and written at equip time, so this only reads — no recipe lookups.
	 *
	 * One source still missing: "cladding kits" mounted on armor (the ArmorModHandler set) carry their own radiation resistance,
	 * the number living on ItemModCladding.rad. The mod table is in; that kit class hasn't landed,
	 * so once it does, add a lookup here — for now armor wearing a cladding kit undercounts by that much.
	 */
	public static double getCladding(ItemStack stack) {

		if(stack.hasTagCompound() && stack.getTagCompound().getFloat("hfr_cladding") > 0)
			return stack.getTagCompound().getFloat("hfr_cladding");

		return 0;
	}

	public static double getResistance(EntityPlayer player) {

		double res = 0.0D;

		// One specific player's vest bonus. Don't ask
		if(player.getUniqueID().toString().equals(ShadyUtil.Pu_238)) {
			res += 0.4D;
		}

		for(EntityEquipmentSlot slot : ARMOR_SLOTS) {
			res += getResistance(player.getItemStackFromSlot(slot));
		}

		if(player.isPotionActive(HbmPotion.radx))
			res += 0.2D;

		return res;
	}

	private static final EntityEquipmentSlot[] ARMOR_SLOTS = { EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST,
			EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

	public static final Gson gson = new Gson();

	/** Must run after all items register, or the ModItems fields are still null */
	public static void registerHazmats() {
		File folder = MainRegistry.configDir;

		File config = new File(folder.getAbsolutePath() + File.separatorChar + "hbmRadResist.json");
		File template = new File(folder.getAbsolutePath() + File.separatorChar + "_hbmRadResist.json");

		initDefault();

		if(!config.exists()) {
			writeDefault(template);
		} else {
			HashMap<Item, Double> conf = readConfig(config);

			if(conf != null) {
				entries.clear();
				entries.putAll(conf);
			}
		}
	}

	private static void writeDefault(File file) {

		try {
			JsonWriter writer = new JsonWriter(new FileWriter(file));
			writer.setIndent("  ");
			writer.beginObject();
			writer.name("comment").value("Template file, remove the underscore ('_') from the name to enable the config.");
			writer.name("entries").beginArray();

			for(Entry<Item, Double> entry : entries.entrySet()) {
				writer.beginObject();
				writer.name("item").value(Item.REGISTRY.getNameForObject(entry.getKey()).toString());
				writer.name("resistance").value(entry.getValue());
				writer.endObject();
			}

			writer.endArray();
			writer.endObject();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static HashMap<Item, Double> readConfig(File config) {

		try {
			JsonObject json = gson.fromJson(new FileReader(config), JsonObject.class);
			JsonArray array = json.get("entries").getAsJsonArray();
			HashMap<Item, Double> conf = new HashMap();

			for(JsonElement element : array) {
				JsonObject object = (JsonObject) element;

				try {
					String name = object.get("item").getAsString();
					Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
					double resistance = object.get("resistance").getAsDouble();
					if(item != null) {
						conf.put(item, resistance);
					} else {
						MainRegistry.logger.error("Tried loading unknown item " + name + " for hazmat entry.");
					}
				} catch(Exception ex) {
					MainRegistry.logger.error("Encountered " + ex + " trying to read hazmat entry " + element.toString());
				}
			}
			return conf;

		} catch(Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}
}
