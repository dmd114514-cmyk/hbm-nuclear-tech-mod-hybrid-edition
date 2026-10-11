package com.hbm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.entity.IResistanceProvider;
import api.hbm.net.Tuple.Quartet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The resistance table: single pieces, full sets, and entities' innate resistances — three granularities in one table.
 *
 * Every hit computes two numbers: dt, the flat reduction taken first, and dr, the proportional one after (capped at 100%).
 * The table can be wholesale overridden by config/hbm/hbmArmor.json; the first run writes only an underscore-prefixed template.
 *
 * Penetration travels via the currentPDT/currentPDR static pair: one hit's math spreads across two events,
 * with nowhere else to carry "how much penetration this hit brought". So they must always setup/reset as a pair,
 * See {@link EntityDamageUtil#attackEntityFromNT}.
 */
public class DamageResistanceHandler {

	/** This hit's effective penetration against flat reduction */
	public static float currentPDT = 0F;
	/** This hit's effective armor break against proportional reduction */
	public static float currentPDR = 0F;

	public static final String CATEGORY_EXPLOSION = "EXPL";
	public static final String CATEGORY_FIRE = "FIRE";
	public static final String CATEGORY_PHYSICAL = "PHYS";
	public static final String CATEGORY_ENERGY = "EN";

	public static final Gson gson = new Gson();

	public static HashMap<Item, ResistanceStats> itemStats = new HashMap();
	public static HashMap<Quartet<Item, Item, Item, Item>, ResistanceStats> setStats = new HashMap();
	public static HashMap<Class<? extends Entity>, ResistanceStats> entityStats = new HashMap();

	/** Reverse lookup: which sets this piece belongs to, for writing set names into tooltips */
	public static HashMap<Item, List<Quartet<Item, Item, Item, Item>>> itemInfoSet = new HashMap();

	public static void init() {
		File folder = MainRegistry.configDir;

		File config = new File(folder.getAbsolutePath() + File.separatorChar + "hbmArmor.json");
		File template = new File(folder.getAbsolutePath() + File.separatorChar + "_hbmArmor.json");

		clearSystem();

		if(!config.exists()) {
			initDefaults();
			writeDefault(template);
		} else {
			readConfig(config);
		}

		// All three tables key on item objects; an unregistered armor item silently becomes an empty entry —
		// compiling, starting, walking around all fine; only when damage lands do you learn a layer is missing, so this has to complain loudly
		MainRegistry.logger.info("[hbm] damage reduction table ready: pieces {} / sets {} / entities {}",
				itemStats.size(), setStats.size(), entityStats.size());

		if(skippedSets > 0 || skippedItems > 0)
			LoggingUtil.warn("[hbm] damage reduction table skipped {} sets and {} single pieces because the armor isn't registered yet — expected while the armor layer hasn't landed; recheck this line once it does",
					skippedSets, skippedItems);
	}

	/** While gear isn't registered yet, no null keys enter the table; just tally a skip count */
	private static void putItem(String what, Item item, ResistanceStats stats) {
		if(item == null) {
			skippedItems++;
			return;
		}
		itemStats.put(item, stats);
	}

	private static void clearSystem() {
		itemStats.clear();
		setStats.clear();
		entityStats.clear();
		itemInfoSet.clear();
	}

	private static void writeDefault(File file) {

		MainRegistry.logger.info("No armor file found, registering defaults for " + file.getName());

		try {
			JsonWriter writer = new JsonWriter(new FileWriter(file));
			writer.setIndent("  ");
			writer.beginObject();
			writer.name("comment").value("Template file, remove the underscore ('_') from the name to enable the config.");

			serialize(writer);

			writer.endObject();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static void readConfig(File file) {

		MainRegistry.logger.info("Reading armor file " + file.getName());

		try {
			JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);
			deserialize(json);

		} catch(FileNotFoundException ex) {
			clearSystem();
			initDefaults();
			ex.printStackTrace();
		}
	}

	public static void initDefaults() {

		entityStats.put(EntityCreeper.class, new ResistanceStats().addCategory(CATEGORY_EXPLOSION, 2F, 0.25F));
		putEntityByName("com.hbm.entity.mob.EntityCreeperNuclear", new ResistanceStats().addCategory(CATEGORY_EXPLOSION, 5F, 0.35F));

		putItem("jackt", ModItems.jackt, new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 1F, 0.20F));
		putItem("jackt2", ModItems.jackt2, new ResistanceStats().addCategory(CATEGORY_PHYSICAL, 2F, 0.25F));

		registerSet(ModItems.steel_helmet, ModItems.steel_plate, ModItems.steel_legs, ModItems.steel_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.1F));
		registerSet(ModItems.titanium_helmet, ModItems.titanium_plate, ModItems.titanium_legs, ModItems.titanium_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 3F, 0.1F));
		registerSet(ModItems.alloy_helmet, ModItems.alloy_plate, ModItems.alloy_legs, ModItems.alloy_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.1F));
		registerSet(ModItems.cobalt_helmet, ModItems.cobalt_plate, ModItems.cobalt_legs, ModItems.cobalt_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.1F));
		registerSet(ModItems.starmetal_helmet, ModItems.starmetal_plate, ModItems.starmetal_legs, ModItems.starmetal_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 3F, 0.25F)
				.setOther(1F, 0.1F));
		registerSet(ModItems.zirconium_legs, ModItems.zirconium_legs, ModItems.zirconium_legs, ModItems.zirconium_legs, new ResistanceStats()
				.setOther(0F, 1F));
		registerSet(ModItems.dnt_helmet, ModItems.dnt_plate, ModItems.dnt_legs, ModItems.dnt_boots, new ResistanceStats());
		registerSet(ModItems.cmb_helmet, ModItems.cmb_plate, ModItems.cmb_legs, ModItems.cmb_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 5F, 0.5F)
				.setOther(5F, 0.25F));
		registerSet(ModItems.schrabidium_helmet, ModItems.schrabidium_plate, ModItems.schrabidium_legs, ModItems.schrabidium_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 10F, 0.65F)
				.setOther(5F, 0.5F));
		registerSet(ModItems.robes_helmet, ModItems.robes_plate, ModItems.robes_legs, ModItems.robes_boots, new ResistanceStats());

		registerSet(ModItems.security_helmet, ModItems.security_plate, ModItems.security_legs, ModItems.security_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 5F, 0.5F)
				.addCategory(CATEGORY_EXPLOSION, 2F, 0.25F));
		registerSet(ModItems.steamsuit_helmet, ModItems.steamsuit_plate, ModItems.steamsuit_legs, ModItems.steamsuit_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.15F)
				.addCategory(CATEGORY_FIRE, 0.5F, 0.25F)
				.addExact(DamageSource.FALL.damageType, 5F, 0.25F)
				.setOther(0F, 0.1F));
		registerSet(ModItems.dieselsuit_helmet, ModItems.dieselsuit_plate, ModItems.dieselsuit_legs, ModItems.dieselsuit_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 1F, 0.15F)
				.addCategory(CATEGORY_FIRE, 0.5F, 0.5F)
				.addCategory(CATEGORY_EXPLOSION, 2F, 0.15F)
				.setOther(0F, 0.1F));
		registerSet(ModItems.t51_helmet, ModItems.t51_plate, ModItems.t51_legs, ModItems.t51_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.15F)
				.addCategory(CATEGORY_FIRE, 0.5F, 0.35F)
				.addCategory(CATEGORY_EXPLOSION, 5F, 0.25F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(0F, 0.1F));
		registerSet(ModItems.ajr_helmet, ModItems.ajr_plate, ModItems.ajr_legs, ModItems.ajr_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 4F, 0.15F)
				.addCategory(CATEGORY_FIRE, 0.5F, 0.35F)
				.addCategory(CATEGORY_EXPLOSION, 7.5F, 0.25F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(0F, 0.15F));
		registerSet(ModItems.ajro_helmet, ModItems.ajro_plate, ModItems.ajro_legs, ModItems.ajro_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 4F, 0.15F)
				.addCategory(CATEGORY_FIRE, 0.5F, 0.35F)
				.addCategory(CATEGORY_EXPLOSION, 7.5F, 0.25F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(0F, 0.15F));
		registerSet(ModItems.rpa_helmet, ModItems.rpa_plate, ModItems.rpa_legs, ModItems.rpa_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 25F, 0.65F)
				.addCategory(CATEGORY_FIRE, 10F, 0.9F)
				.addCategory(CATEGORY_EXPLOSION, 15F, 0.25F)
				.addCategory(CATEGORY_ENERGY, 25F, 0.75F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(15F, 0.3F));
		registerSet(ModItems.ncrpa_helmet, ModItems.ncrpa_plate, ModItems.ncrpa_legs, ModItems.ncrpa_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 25F, 0.65F)
				.addCategory(CATEGORY_FIRE, 10F, 0.9F)
				.addCategory(CATEGORY_EXPLOSION, 15F, 0.25F)
				.addCategory(CATEGORY_ENERGY, 10F, 0.5F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(15F, 0.25F));
		ResistanceStats bj = new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 5F, 0.5F)
				.addCategory(CATEGORY_FIRE, 2.5F, 0.5F)
				.addCategory(CATEGORY_EXPLOSION, 10F, 0.25F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(2F, 0.15F);
		registerSet(ModItems.bj_helmet, ModItems.bj_plate, ModItems.bj_legs, ModItems.bj_boots, bj);
		registerSet(ModItems.bj_helmet, ModItems.bj_plate_jetpack, ModItems.bj_legs, ModItems.bj_boots, bj);
		registerSet(ModItems.envsuit_helmet, ModItems.envsuit_plate, ModItems.envsuit_legs, ModItems.envsuit_boots, new ResistanceStats()
				.addCategory(CATEGORY_FIRE, 2F, 0.75F)
				.addExact(DamageSource.DROWN.damageType, 0F, 1F)
				.addExact(DamageSource.FALL.damageType, 5F, 0.75F)
				.setOther(0F, 0.1F));
		registerSet(ModItems.hev_helmet, ModItems.hev_plate, ModItems.hev_legs, ModItems.hev_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.25F)
				.addCategory(CATEGORY_FIRE, 0.5F, 0.5F)
				.addCategory(CATEGORY_EXPLOSION, 5F, 0.25F)
				.addExact(DamageSource.IN_FIRE.damageType, 0F, 1F)
				.addExact(DamageSource.FALL.damageType, 10F, 0F)
				.setOther(2F, 0.25F));
		registerSet(ModItems.bismuth_helmet, ModItems.bismuth_plate, ModItems.bismuth_legs, ModItems.bismuth_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.15F)
				.addCategory(CATEGORY_FIRE, 5F, 0.5F)
				.addCategory(CATEGORY_EXPLOSION, 5F, 0.25F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(2F, 0.25F));
		registerSet(ModItems.fau_helmet, ModItems.fau_plate, ModItems.fau_legs, ModItems.fau_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 100F, 0.99F)
				.addCategory(CATEGORY_EXPLOSION, 50F, 0.95F)
				.addCategory(CATEGORY_FIRE, 100F, 1F)
				.addExact(DamageClass.LASER.name(), 25F, 0.95F)
				.addExact(DamageSource.FALL.damageType, 0F, 1F)
				.setOther(100F, 0.99F));
		registerSet(ModItems.dns_helmet, ModItems.dns_plate, ModItems.dns_legs, ModItems.dns_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 1000F, 1F)
				.addCategory(CATEGORY_EXPLOSION, 100F, 0.99F)
				.addCategory(CATEGORY_FIRE, 0F, 1F)
				.setOther(1000F, 1F));
		registerSet(ModItems.taurun_helmet, ModItems.taurun_plate, ModItems.taurun_legs, ModItems.taurun_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 2F, 0.15F)
				.addCategory(CATEGORY_FIRE, 0F, 0.25F)
				.addCategory(CATEGORY_EXPLOSION, 0F, 0.25F)
				.addExact(DamageSource.FALL.damageType, 4F, 0.5F)
				.setOther(2F, 0.1F));
		registerSet(ModItems.trenchmaster_helmet, ModItems.trenchmaster_plate, ModItems.trenchmaster_legs, ModItems.trenchmaster_boots, new ResistanceStats()
				.addCategory(CATEGORY_PHYSICAL, 5F, 0.5F)
				.addCategory(CATEGORY_FIRE, 5F, 0.5F)
				.addCategory(CATEGORY_EXPLOSION, 5F, 0.25F)
				.addExact(DamageClass.LASER.name(), 15F, 0.9F)
				.addExact(DamageSource.FALL.damageType, 10F, 0.5F)
				.setOther(5F, 0.25F));

		registerSet(ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots, new ResistanceStats()
				.setOther(1_000_000F, 1F));

		registerSet(ModItems.hazmat_helmet, ModItems.hazmat_plate, ModItems.hazmat_legs, ModItems.hazmat_boots, new ResistanceStats());
		registerSet(ModItems.hazmat_helmet_red, ModItems.hazmat_plate_red, ModItems.hazmat_legs_red, ModItems.hazmat_boots_red, new ResistanceStats());
		registerSet(ModItems.hazmat_helmet_grey, ModItems.hazmat_plate_grey, ModItems.hazmat_legs_grey, ModItems.hazmat_boots_grey, new ResistanceStats());
		registerSet(ModItems.liquidator_helmet, ModItems.liquidator_plate, ModItems.liquidator_legs, ModItems.liquidator_boots, new ResistanceStats());
		registerSet(ModItems.hazmat_paa_helmet, ModItems.hazmat_paa_plate, ModItems.hazmat_paa_legs, ModItems.hazmat_paa_boots, new ResistanceStats());
		registerSet(ModItems.asbestos_helmet, ModItems.asbestos_plate, ModItems.asbestos_legs, ModItems.asbestos_boots, new ResistanceStats()
				.addCategory(CATEGORY_FIRE, 10F, 0.9F));
	}

	private static int skippedSets = 0;
	private static int skippedItems = 0;

	public static void registerSet(Item helmet, Item plate, Item legs, Item boots, ResistanceStats stats) {

		// Four empty pieces most likely means that armor never registered; keeping it would yield an "all-null set" key merging every miss into one entry
		if(helmet == null && plate == null && legs == null && boots == null) {
			skippedSets++;
			return;
		}

		Quartet<Item, Item, Item, Item> set = new Quartet(helmet, plate, legs, boots);
		setStats.put(set, stats);
		if(helmet != null) addToListInHashMap(helmet, itemInfoSet, set);
		if(plate != null) addToListInHashMap(plate, itemInfoSet, set);
		if(legs != null) addToListInHashMap(legs, itemInfoSet, set);
		if(boots != null) addToListInHashMap(boots, itemInfoSet, set);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void addToListInHashMap(Object key, HashMap map, Object listElement) {
		List list = (List) map.get(key);
		if(list == null) {
			list = new ArrayList();
			map.put(key, list);
		}
		list.add(listElement);
	}

	/** Armor tooltips: set/piece resistances as lines, handed to the item's addInformation */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addInfo(ItemStack stack, List desc) {
		if(stack == null || stack.getItem() == null) return;

		if(itemInfoSet.containsKey(stack.getItem())) {
			List<Quartet<Item, Item, Item, Item>> sets = itemInfoSet.get(stack.getItem());

			for(Quartet<Item, Item, Item, Item> set : sets) {

				ResistanceStats stats = setStats.get(set);
				if(stats == null) continue;

				List toAdd = new ArrayList();
				appendStats(stats, toAdd);

				if(!toAdd.isEmpty()) {
					desc.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("damage.inset"));
					// set members listed one by one, so people know the full set is what counts
					if(set.getZ() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getZ()).getDisplayName());
					if(set.getX() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getX()).getDisplayName());
					if(set.getY() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getY()).getDisplayName());
					if(set.getW() != null) desc.add(TextFormatting.DARK_PURPLE + "  " + new ItemStack(set.getW()).getDisplayName());
					desc.addAll(toAdd);
				}

				break; //one set listed for now; listing all would run too long
			}
		}

		if(itemStats.containsKey(stack.getItem())) {
			ResistanceStats stats = itemStats.get(stack.getItem());

			List toAdd = new ArrayList();
			appendStats(stats, toAdd);

			if(!toAdd.isEmpty()) {
				desc.add(TextFormatting.DARK_PURPLE + I18nUtil.resolveKey("damage.item"));
				desc.addAll(toAdd);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void appendStats(ResistanceStats stats, List toAdd) {

		for(Entry<String, Resistance> entry : stats.categoryResistances.entrySet()) {
			toAdd.add(I18nUtil.resolveKey("damage.category." + entry.getKey()) + ": " + entry.getValue().threshold + "/" + ((int)(entry.getValue().resistance * 100)) + "%");
		}
		for(Entry<String, Resistance> entry : stats.exactResistances.entrySet()) {
			toAdd.add(I18nUtil.resolveKey("damage.exact." + entry.getKey()) + ": " + entry.getValue().threshold + "/" + ((int)(entry.getValue().resistance * 100)) + "%");
		}
		if(stats.otherResistance != null) toAdd.add(I18nUtil.resolveKey("damage.other") + ": " + stats.otherResistance.threshold + "/" + ((int)(stats.otherResistance.resistance * 100)) + "%");
	}

	public static void serialize(JsonWriter writer) throws IOException {
		/// single pieces
		writer.name("itemStats").beginArray();
		for(Entry<Item, ResistanceStats> entry : itemStats.entrySet()) {
			writer.beginArray().setIndent("");
			writer.value(nameOf(entry.getKey())).setIndent("  ");
			writer.beginObject();
			entry.getValue().serialize(writer);
			writer.setIndent("");
			writer.endObject().endArray().setIndent("  ");
		}
		writer.endArray();

		/// full sets
		writer.name("setStats").beginArray();
		for(Entry<Quartet<Item, Item, Item, Item>, ResistanceStats> entry : setStats.entrySet()) {
			writer.beginArray().setIndent("");
			writer.value(nameOf(entry.getKey().getZ()))
			.value(nameOf(entry.getKey().getX()))
			.value(nameOf(entry.getKey().getY()))
			.value(nameOf(entry.getKey().getW())).setIndent("  ");
			writer.beginObject();
			entry.getValue().serialize(writer);
			writer.setIndent("");
			writer.endObject().endArray().setIndent("  ");
		}
		writer.endArray();

		/// entities ///
		writer.name("entityStats").beginArray();
		for(Entry<Class<? extends Entity>, ResistanceStats> entry : entityStats.entrySet()) {
			writer.beginArray().setIndent("");
			writer.value(entry.getKey().getName()).setIndent("  ");
			writer.beginObject();
			entry.getValue().serialize(writer);
			writer.setIndent("");
			writer.endObject().endArray().setIndent("  ");
		}
		writer.endArray();
	}

	public static void deserialize(JsonObject json) {
		/// single pieces
		JsonArray itemStatsArray = json.get("itemStats").getAsJsonArray();
		for(JsonElement element : itemStatsArray) {
			JsonArray statArray = element.getAsJsonArray();
			Item item = itemByName(statArray.get(0).getAsString());
			JsonObject stats = statArray.get(1).getAsJsonObject();
			itemStats.put(item, ResistanceStats.deserialize(stats));
		}

		/// full sets
		JsonArray setStatsArray = json.get("setStats").getAsJsonArray();
		for(JsonElement element : setStatsArray) {
			JsonArray statArray = element.getAsJsonArray();
			Item helmet = statArray.get(0).isJsonNull() ? null : itemByName(statArray.get(0).getAsString());
			Item plate = statArray.get(1).isJsonNull() ? null : itemByName(statArray.get(1).getAsString());
			Item legs = statArray.get(2).isJsonNull() ? null : itemByName(statArray.get(2).getAsString());
			Item boots = statArray.get(3).isJsonNull() ? null : itemByName(statArray.get(3).getAsString());
			JsonObject stats = statArray.get(4).getAsJsonObject();
			registerSet(helmet, plate, legs, boots, ResistanceStats.deserialize(stats));
		}

		/// entities ///
		JsonArray entityStatsArray = json.get("entityStats").getAsJsonArray();
		for(JsonElement element : entityStatsArray) {
			JsonArray statArray = element.getAsJsonArray();
			try {
				Class<?> clazz = Class.forName(statArray.get(0).getAsString(), false, DamageResistanceHandler.class.getClassLoader());
				if(Entity.class.isAssignableFrom(clazz)) {
					@SuppressWarnings("unchecked")
					Class<? extends Entity> entityClass = (Class<? extends Entity>) clazz;
					JsonObject stats = statArray.get(1).getAsJsonObject();
					entityStats.put(entityClass, ResistanceStats.deserialize(stats));
				}
			} catch(ClassNotFoundException | RuntimeException e) { }
		}
	}

	/**
	 * Registers entity resistances by name. Not all of this mod's entity classes have landed,
	 * so unrecognized names skip — whoever lands later never needs to revisit this table.
	 */
	private static void putEntityByName(String className, ResistanceStats stats) {

		try {
			Class<?> clazz = Class.forName(className, false, DamageResistanceHandler.class.getClassLoader());

			if(Entity.class.isAssignableFrom(clazz)) {
				@SuppressWarnings("unchecked")
				Class<? extends Entity> entityClass = (Class<? extends Entity>) clazz;
				entityStats.put(entityClass, stats);
			}
		} catch(ClassNotFoundException | RuntimeException ex) {
			MainRegistry.logger.debug("[hbm] entity resistance table skipping the not-yet-landed {}", className);
		}
	}

	private static String nameOf(Item item) {
		if(item == null) return null;
		ResourceLocation rl = Item.REGISTRY.getNameForObject(item);
		return rl == null ? null : rl.toString();
	}

	private static Item itemByName(String name) {
		return Item.REGISTRY.getObject(new ResourceLocation(name));
	}

	/** Damage-type bucketing; anything outside the four categories becomes its own resistance line keyed by type name */
	public static enum DamageClass {
		PHYSICAL,
		FIRE,
		EXPLOSIVE,
		ELECTRIC,
		PLASMA,
		LASER,
		MICROWAVE,
		SUBATOMIC,
		OTHER;

		public boolean isApplicable(String name) {
			return name.toLowerCase(Locale.US).equals(this.name().toLowerCase(Locale.US));
		}
	}

	public static void setup(float dt, float dr) {
		currentPDT = dt;
		currentPDR = dr;
	}

	public static void reset() {
		currentPDT = 0;
		currentPDR = 0;
	}

	@SubscribeEvent
	public void onEntityAttacked(LivingAttackEvent event) {

		DamageSource source = event.getSource();
		if(source.isDamageAbsolute()) return;

		EntityLivingBase e = event.getEntityLiving();
		float amount = event.getAmount();

		float[] vals = getDTDR(e, source, amount, currentPDT, currentPDR);
		float dt = vals[0] - currentPDT;
		float dr = vals[1] - currentPDR;

		if((dt > 0 && dt >= amount) || dr >= 1F) {
			event.setCanceled(true);
			EntityDamageUtil.damageArmorNT(e, amount);
		}
	}

	@SubscribeEvent
	public void onEntityDamaged(LivingHurtEvent event) {

		DamageSource source = event.getSource();
		float amount = event.getAmount();

		if(source.damageType.toLowerCase(Locale.US).equals(DamageClass.ELECTRIC.name().toLowerCase(Locale.US))) {
			ItemStack chest = event.getEntityLiving().getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			// The enriched suit conducts once powered; matched by class name for now, switching to instanceof once that armor lands
			if(chest != null && isItemOfClass(chest, "com.hbm.items.armor.ArmorFSBPowered")) {
				amount *= 5;
			}
		}

		amount = calculateDamage(event.getEntityLiving(), source, amount, currentPDT, currentPDR);
		event.setAmount(amount);

		if(event.getEntityLiving() instanceof IResistanceProvider) {
			IResistanceProvider irp = (IResistanceProvider) event.getEntityLiving();
			irp.onDamageDealt(source, amount);
		}
	}

	private static boolean isItemOfClass(ItemStack stack, String className) {

		if(stack == null || stack.getItem() == null)
			return false;

		for(Class<?> clazz = stack.getItem().getClass(); clazz != null; clazz = clazz.getSuperclass()) {
			if(clazz.getName().equals(className))
				return true;
		}

		return false;
	}

	public static String typeToCategory(DamageSource source) {
		if(source.isExplosion()) return CATEGORY_EXPLOSION;
		if(source.isFireDamage()) return CATEGORY_FIRE;
		if(source.isProjectile()) return CATEGORY_PHYSICAL;
		if(DamageClass.LASER.isApplicable(source.damageType)) return CATEGORY_ENERGY;
		if(DamageClass.PLASMA.isApplicable(source.damageType)) return CATEGORY_ENERGY;
		if(DamageClass.MICROWAVE.isApplicable(source.damageType)) return CATEGORY_ENERGY;
		if(DamageClass.SUBATOMIC.isApplicable(source.damageType)) return CATEGORY_ENERGY;
		if(DamageClass.ELECTRIC.isApplicable(source.damageType)) return CATEGORY_ENERGY;
		if(source == DamageSource.CACTUS) return CATEGORY_PHYSICAL;
		if(source == ModDamageSource.spikes) return CATEGORY_PHYSICAL;
		if(source == ModDamageSource.electricity) return CATEGORY_ENERGY;
		if(source == ModDamageSource.microwave) return CATEGORY_ENERGY;
		if(source instanceof EntityDamageSource) return CATEGORY_PHYSICAL;
		return source.damageType;
	}

	public static float calculateDamage(EntityLivingBase entity, DamageSource damage, float amount, float pierceDT, float pierce) {
		if(damage.isDamageAbsolute()) return amount;

		float[] vals = getDTDR(entity, damage, amount, pierceDT, pierce);
		float dt = vals[0];
		float dr = vals[1];

		dt = Math.max(0F, dt - pierceDT);
		if(dt >= amount) return 0F;
		amount -= dt;
		dr *= MathHelper.clamp(1F - pierce, 0F, 2F); // allows -1 armor break, which doubles the effective armor

		return amount *= (1F - dr);
	}

	/** Computes the flat and proportional reduction this hit actually takes */
	public static float[] getDTDR(EntityLivingBase entity, DamageSource damage, float amount, float pierceDT, float pierce) {

		float dt = 0;
		float dr = 0;

		if(entity instanceof IResistanceProvider) {
			IResistanceProvider irp = (IResistanceProvider) entity;
			float[] res = irp.getCurrentDTDR(damage, amount, pierceDT, pierce);
			dt += res[0];
			dr += res[1];
		}

		/// full sets
		Quartet<Item, Item, Item, Item> wornSet = new Quartet(
				itemInSlot(entity, EntityEquipmentSlot.HEAD),
				itemInSlot(entity, EntityEquipmentSlot.CHEST),
				itemInSlot(entity, EntityEquipmentSlot.LEGS),
				itemInSlot(entity, EntityEquipmentSlot.FEET)
				);

		ResistanceStats setResistance = setStats.get(wornSet);
		if(setResistance != null) {
			Resistance res = setResistance.getResistance(damage);
			if(res != null) {
				dt += res.threshold;
				dr += res.resistance;
			}
		}

		/// single pieces
		for(EntityEquipmentSlot slot : ARMOR_SLOTS) {
			ItemStack armor = entity.getItemStackFromSlot(slot);
			if(armor == null || armor.isEmpty()) continue;
			ResistanceStats stats = itemStats.get(armor.getItem());
			if(stats == null) continue;
			Resistance res = stats.getResistance(damage);
			if(res == null) continue;
			dt += res.threshold;
			dr += res.resistance;
		}

		/// innate resistance ///
		ResistanceStats innateResistance = entityStats.get(entity.getClass());
		if(innateResistance != null) {
			Resistance res = innateResistance.getResistance(damage);
			if(res != null) {
				dt += res.threshold;
				dr += res.resistance;
			}
		}

		return new float[] {dt, dr};
	}

	private static final EntityEquipmentSlot[] ARMOR_SLOTS = { EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST,
			EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

	private static Item itemInSlot(EntityLivingBase entity, EntityEquipmentSlot slot) {
		ItemStack stack = entity.getItemStackFromSlot(slot);
		return (stack == null || stack.isEmpty()) ? null : stack.getItem();
	}

	public static class ResistanceStats {

		public HashMap<String, Resistance> exactResistances = new HashMap();
		public HashMap<String, Resistance> categoryResistances = new HashMap();
		public Resistance otherResistance;

		public Resistance getResistance(DamageSource source) {
			Resistance exact = exactResistances.get(source.damageType.toLowerCase(Locale.US));
			if(exact != null) return exact;
			Resistance category = categoryResistances.get(typeToCategory(source));
			if(category != null) return category;
			return source.isUnblockable() ? null : otherResistance;
		}

		public ResistanceStats addExact(String type, float threshold, float resistance) { exactResistances.put(type.toLowerCase(Locale.US), new Resistance(threshold, resistance)); return this; }
		public ResistanceStats addCategory(String type, float threshold, float resistance) { categoryResistances.put(type, new Resistance(threshold, resistance)); return this; }
		public ResistanceStats setOther(float threshold, float resistance) { otherResistance = new Resistance(threshold, resistance); return this; }

		public void serialize(JsonWriter writer) throws IOException {

			if(!exactResistances.isEmpty()) {
				writer.name("exact").beginArray();
				for(Entry<String, Resistance> entry : exactResistances.entrySet()) {
					writer.beginArray().setIndent("");
					writer.value(entry.getKey()).value(entry.getValue().threshold).value(entry.getValue().resistance).endArray().setIndent("  ");
				}
				writer.endArray();
			}

			if(!categoryResistances.isEmpty()) {
				writer.name("category").beginArray();
				for(Entry<String, Resistance> entry : categoryResistances.entrySet()) {
					writer.beginArray().setIndent("");
					writer.value(entry.getKey()).value(entry.getValue().threshold).value(entry.getValue().resistance).endArray().setIndent("  ");
				}
				writer.endArray();
			}

			if(otherResistance != null) {
				writer.name("other").beginArray().setIndent("");
				writer.value(otherResistance.threshold).value(otherResistance.resistance).endArray().setIndent("  ");
			}
		}

		public static ResistanceStats deserialize(JsonObject json) {
			ResistanceStats stats = new ResistanceStats();

			if(json.has("exact")) {
				JsonArray exact = json.get("exact").getAsJsonArray();
				for(JsonElement element : exact) {
					JsonArray array = element.getAsJsonArray();
					stats.exactResistances.put(array.get(0).getAsString(), new Resistance(array.get(1).getAsFloat(), array.get(2).getAsFloat()));
				}
			}

			if(json.has("category")) {
				JsonArray category = json.get("category").getAsJsonArray();
				for(JsonElement element : category) {
					JsonArray array = element.getAsJsonArray();
					stats.categoryResistances.put(array.get(0).getAsString(), new Resistance(array.get(1).getAsFloat(), array.get(2).getAsFloat()));
				}
			}

			if(json.has("other")) {
				JsonArray other = json.get("other").getAsJsonArray();
				stats.otherResistance = new Resistance(other.get(0).getAsFloat(), other.get(1).getAsFloat());
			}

			return stats;
		}
	}

	public static class Resistance {

		public float threshold;
		public float resistance;

		public Resistance(float threshold, float resistance) {
			this.threshold = threshold;
			this.resistance = resistance;
		}
	}
}
