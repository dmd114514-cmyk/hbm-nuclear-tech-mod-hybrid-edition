package com.hbm.main;

import java.io.File;
import java.util.Random;

import org.apache.logging.log4j.Logger;

import com.hbm.compat.CompatBootstrap;
import com.hbm.compat.CompatManager;
import com.hbm.config.ConfigManager;
import com.hbm.crafting.RecipeRegistry;
import com.hbm.crafting.SerializableRecipe;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardTickHandler;
import com.hbm.lib.RefStrings;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.HbmPackets;
import com.hbm.network.NetworkHandler;
import com.hbm.registry.HbmModules;
import com.hbm.registry.HbmRegistry;

import api.hbm.recipe.HbmRecipes;
import api.hbm.tile.TileAccessCache;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

/**
 * The mod entry. This is also the sole "assembly point" — each layer's registration unit is named in exactly one place,
 * and beyond it, no lower-layer code may reach back up.
 */
@Mod(modid = RefStrings.MODID, name = RefStrings.NAME, version = RefStrings.VERSION)
public class MainRegistry {

	@Instance(RefStrings.MODID)
	public static MainRegistry instance;

	@SidedProxy(clientSide = RefStrings.CLIENTSIDE, serverSide = RefStrings.SERVERSIDE)
	public static ServerProxy proxy;

	public static Logger logger;
	public static final Random rand = new Random();

	/*
	 * Tool/armor material tables. From 1.12.2 these two are no longer directly constructible, only made through EnumHelper,
	 * and must exist before any item is built — hence the assignment lives in preInit's initMaterials() (reasons on that method).
	 * Names and values copy the old table entry by entry; changing one number is changing a piece of equipment's strength.
	 */

	public static ToolMaterial tMatSchrab;
	public static ToolMaterial tMatHammmer;
	public static ToolMaterial tMatChainsaw;
	public static ToolMaterial tMatSteel;
	public static ToolMaterial tMatTitan;
	@Deprecated public static ToolMaterial tMatAlloy;
	public static ToolMaterial tMatCMB;
	public static ToolMaterial tMatElec;
	public static ToolMaterial tMatDesh;
	public static ToolMaterial tMatCobalt;

	public static ToolMaterial enumToolMaterialPipeLead;
	public static ToolMaterial enumToolMaterialBottleOpener;
	public static ToolMaterial enumToolMaterialSledge;
	public static ToolMaterial enumToolMaterialMultitool;

	/*
	 * 1.12.2's addArmorMaterial takes three more arguments than before: texture name, equip sound, toughness.
	 * Texture names are always the lowercase name — armor items override getArmorTexture themselves, so the value is just a label;
	 * the sound is GENERIC across the board (the old table had no sound concept at all, and guessing metal/leather would change the feel out of nowhere), toughness always 0.
	 */

	public static ArmorMaterial aMatSchrab;
	public static ArmorMaterial aMatEuph;
	public static ArmorMaterial aMatHaz;
	public static ArmorMaterial aMatHaz2;
	public static ArmorMaterial aMatHaz3;
	public static ArmorMaterial aMatSteel;
	@Deprecated public static ArmorMaterial aMatAlloy;
	public static ArmorMaterial aMatAsbestos;
	public static ArmorMaterial aMatTitan;
	public static ArmorMaterial aMatPaa;
	public static ArmorMaterial aMatCMB;
	public static ArmorMaterial aMatAus3;
	public static ArmorMaterial aMatSecurity;
	public static ArmorMaterial aMatCobalt;
	public static ArmorMaterial aMatStarmetal;
	public static ArmorMaterial aMatBismuth;

	private static ArmorMaterial armor(String name, int durability, int[] reduction, int enchantability) {
		return EnumHelper.addArmorMaterial(name, name.toLowerCase(java.util.Locale.ROOT), durability, reduction, enchantability, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0.0F);
	}

	/*
	 * The material tables are built here rather than in field initializers: reading `SoundEvents` before vanilla's Bootstrap runs
	 * throws "Accessed Sounds before Bootstrap!", and the entry class's static init happens at construction — necessarily earlier.
	 */
	private static void initMaterials() {
		tMatSchrab = EnumHelper.addToolMaterial("SCHRABIDIUM", 3, 10000, 50.0F, 100.0F, 200);
		tMatHammmer = EnumHelper.addToolMaterial("SCHRABIDIUMHAMMER", 3, 0, 50.0F, 999999996F, 200);
		tMatChainsaw = EnumHelper.addToolMaterial("CHAINSAW", 3, 1500, 50.0F, 22.0F, 0);
		tMatSteel = EnumHelper.addToolMaterial("HBM_STEEL", 3, 750, 8.0F, 2.0F, 10);
		tMatTitan = EnumHelper.addToolMaterial("HBM_TITANIUM", 3, 1000, 9.0F, 2.5F, 15);
		tMatAlloy = EnumHelper.addToolMaterial("HBM_ALLOY", 3, 2000, 15.0F, 5.0F, 5);
		tMatCMB = EnumHelper.addToolMaterial("HBM_CMB", 3, 8500, 40.0F, 55F, 100);
		tMatElec = EnumHelper.addToolMaterial("HBM_ELEC", 3, 0, 30.0F, 12.0F, 2);
		tMatDesh = EnumHelper.addToolMaterial("HBM_DESH", 2, 0, 7.5F, 2.0F, 10);
		tMatCobalt = EnumHelper.addToolMaterial("HBM_COBALT", 3, 750, 9.0F, 2.5F, 60);
		enumToolMaterialPipeLead = EnumHelper.addToolMaterial("PIPELEAD", 1, 250, 1.5F, 3F, 25);
		enumToolMaterialBottleOpener = EnumHelper.addToolMaterial("OPENER", 1, 250, 1.5F, 0.5F, 200);
		enumToolMaterialSledge = EnumHelper.addToolMaterial("SHIMMERSLEDGE", 1, 0, 25.0F, 26F, 200);
		enumToolMaterialMultitool = EnumHelper.addToolMaterial("MULTITOOL", 3, 5000, 25F, 5.5F, 25);
		aMatSchrab = armor("HBM_SCHRABIDIUM", 100, new int[] { 3, 8, 6, 3 }, 50);
		aMatEuph = armor("HBM_EUPHEMIUM", 15000000, new int[] { 3, 8, 6, 3 }, 100);
		aMatHaz = armor("HBM_HAZMAT", 60, new int[] { 2, 5, 4, 1 }, 5);
		aMatHaz2 = armor("HBM_HAZMAT2", 60, new int[] { 2, 5, 4, 1 }, 5);
		aMatHaz3 = armor("HBM_HAZMAT3", 60, new int[] { 2, 5, 4, 1 }, 5);
		aMatSteel = armor("HBM_STEEL", 30, new int[] { 3, 8, 6, 3 }, 5);
		aMatAlloy = armor("HBM_ALLOY", 40, new int[] { 3, 8, 6, 3 }, 12);
		aMatAsbestos = armor("HBM_ASBESTOS", 20, new int[] { 1, 4, 3, 1 }, 5);
		aMatTitan = armor("HBM_TITANIUM", 25, new int[] { 3, 8, 6, 3 }, 9);
		aMatPaa = armor("HBM_PAA", 75, new int[] { 3, 8, 6, 3 }, 25);
		aMatCMB = armor("HBM_CMB", 60, new int[] { 3, 8, 6, 3 }, 50);
		aMatAus3 = armor("HBM_AUSIII", 375, new int[] { 2, 6, 5, 2 }, 0);
		aMatSecurity = armor("HBM_SECURITY", 100, new int[] { 3, 8, 6, 3 }, 15);
		aMatCobalt = armor("HBM_COBALT", 70, new int[] { 3, 8, 6, 3 }, 60);
		aMatStarmetal = armor("HBM_STARMETAL", 150, new int[] { 3, 8, 6, 3 }, 100);
		aMatBismuth = armor("HBM_BISMUTH", 100, new int[] { 3, 8, 6, 3 }, 100);
		// Count the enum's actual length rather than how many lines we wrote: EnumHelper extends enums reflectively,
		// and on silent failure this falls back to vanilla's 5/5, visible at a glance
		logger.info("[hbm] material table ready: {} tools / {} armors", ToolMaterial.values().length, ArmorMaterial.values().length);
	}

	/** config/hbm, the recipe directory's parent */
	public static File configDir;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		logger = event.getModLog();
		configDir = new File(event.getModConfigurationDirectory(), RefStrings.MODID);

		logger.info("{} fires up the stove...", RefStrings.NAME);
		initMaterials();

		ConfigManager.load(configDir);
		NetworkHandler.init();
		HbmPackets.registerAll();
		AsyncPacketSender.init();
		// Capabilities must be registered before any entity is created
		com.hbm.extprop.HbmExtPropCapabilities.register();

		// First call the roll for every subdomain's registration unit; they only construct objects and queue — actual registration waits for RegistryEvent
		HbmModules.registerAll();
		// Modules are discovered before queueing: content packs register through HbmRegistry only, sharing one flush with the core roster above
		com.hbm.module.HbmModuleManager.load(configDir);
		// Fluids/materials aren't Forge registry objects; they must be explicitly initialized here, before content objects are built
		HbmRegistry.registerFluids();
		HbmRegistry.preInitRegistrars();
		com.hbm.module.HbmModuleManager.preInit();

		// Carry the modified recipe names into crash reports: recipes are external JSON, and without this you can't tell a code bug from a pack author's edit
		com.hbm.util.CrashHelper.init();

		// The openGui receiver: the block side has been calling all along, but with no IGuiHandler registered, clicking just failed silently
		net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(instance, new com.hbm.handler.GUIHandler());
		com.hbm.util.LoggingUtil.info("[hbm] IGuiHandler registered (four lookup routes by block / tile entity / held item / entity ID)");

		// The tweak table's JSON copy reads before scripts: scripts run later in the registration phase, so they naturally overwrite the same table
		com.hbm.tweak.TweakConfig.loadJson();

		// The recipe facade must be usable before content arrives, or CraftTweaker scripts find no entry when loading
		HbmRecipes.install(RecipeRegistry.INSTANCE);

		// Compatibility modules for optional mods get named here: the core only carries class names, module jars are never imported into the core
		CompatBootstrap.registerModules();

		proxy.preInit(event);
		CompatManager.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		HbmRegistry.initRegistrars();
		com.hbm.module.HbmModuleManager.init();
		// The hazmat table indexes by item object, so it waits until the ModItems fields are all filled — hence init
		com.hbm.handler.HazmatRegistry.registerHazmats();
		// Same for the armor-borne hazard-class overlay table: those Pairs were stuffed in at item construction
		com.hbm.util.ArmorUtil.register();
		// The hazard reducer roster reads the LBSM switch group; config finished loading in preInit, so placing it here satisfies both.
		// The old version also called registerItems() right here; not landed in this tree yet: it references ModItems fields line by line, to be added once that content registers
		HazardRegistry.registerTrafos();
		// The runtime entry for hazard settlement: dropped items and players ride the global heartbeat roster, other living things hang their own living event
		HazardTickHandler.register();
		// The damage-reduction table also keys on item objects; its two event ports are instance methods, so registering this instance once is enough
		com.hbm.util.DamageResistanceHandler dmgHandler = new com.hbm.util.DamageResistanceHandler();
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(dmgHandler);
		dmgHandler.init();
		// Block save migration bookkeeping: both are instance methods, so the instance must be registered explicitly (the bus only takes statics from a Class)
		com.hbm.handler.BlockMigrations migrations = new com.hbm.handler.BlockMigrations();
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(migrations);
		com.hbm.util.LoggingUtil.info("[hbm] chunk migration mounted, current migration number {}", com.hbm.handler.BlockMigrations.MIGRATION_VERSION);

		proxy.init(event);
		CompatManager.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		HbmRegistry.postInitRegistrars();
		// Modules run before recipes are read: what they register in postInit must be referenceable by that same batch of recipes
		com.hbm.module.HbmModuleManager.postInit();
		// Recipes only read after every item/fluid has registered, or the things JSON references don't exist yet
		SerializableRecipe.initialize();
		proxy.postInit(event);
		CompatManager.postInit();

		logger.info("{} fully assembled, ready to work", RefStrings.NAME);
	}

	/**
	 * The command hookup. The roster lives in {@code com.hbm.commands.ModCommands}; this just forwards one call,
	 * no more stacking {@code registerServerCommand(new X())} into this method.
	 * <br><br>
	 * Why it must go through the {@code @Mod} class: 1.12.2's {@code FMLServerStartingEvent} is a mod-bus state event,
	 * while this version's {@code @EventBusSubscriber} only puts classes on the game bus ⇒ a home-built class never receives it.
	 */
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		com.hbm.commands.ModCommands.register(event);
	}

	/** Clears cross-world caches on world exit, so memory doesn't hold a pile of dead references */
	@EventHandler
	public void serverStopped(FMLServerStoppedEvent event) {
		// Drain in-flight packets first, then clear cross-world caches — reversed, packets die halfway
		AsyncPacketSender.shutdown();
		TileAccessCache.clear();
		logger.info("World unloaded; node space and access cache cleared");
	}
}
