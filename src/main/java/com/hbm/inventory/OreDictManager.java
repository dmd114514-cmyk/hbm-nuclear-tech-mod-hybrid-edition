package com.hbm.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.hbm.hazard.HazardData;
import com.hbm.hazard.HazardEntry;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.interfaces.NotableComments;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.main.MainRegistry;
import com.hbm.util.Compat;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

// This list gets more GregTech-esque with every cleanup; you get used to it
@NotableComments
public class OreDictManager {

	/** The alias registry for ore dict entries, fed mostly by DictGroup */
	private static final HashMap<String, HashSet<String>> reRegistration = new HashMap<>();

	/*
	 * generic keys
	 */
	public static final String KEY_STICK = "stickWood";					//When no any-prefix or shape prefix is needed, a plain string key does the job — no DictFrame required
	public static final String KEY_ANYGLASS = "blockGlass";
	public static final String KEY_CLEARGLASS = "blockGlassColorless";
	public static final String KEY_ANYPANE = "paneGlass";
	public static final String KEY_CLEARPANE = "paneGlassColorless";
	public static final String KEY_BRICK = "ingotBrick";
	public static final String KEY_NETHERBRICK = "ingotBrickNether";
	public static final String KEY_SLIME = "slimeball";
	public static final String KEY_LOG = "logWood";
	public static final String KEY_PLANKS = "plankWood";
	public static final String KEY_SLAB = "slabWood";
	public static final String KEY_LEAVES = "treeLeaves";
	public static final String KEY_SAPLING = "treeSapling";
	public static final String KEY_SAND = "sand";
	public static final String KEY_STONE = "stone";
	public static final String KEY_COBBLESTONE = "cobblestone";

	public static final String KEY_BLACK = "dyeBlack";
	public static final String KEY_RED = "dyeRed";
	public static final String KEY_GREEN = "dyeGreen";
	public static final String KEY_BROWN = "dyeBrown";
	public static final String KEY_BLUE = "dyeBlue";
	public static final String KEY_PURPLE = "dyePurple";
	public static final String KEY_CYAN = "dyeCyan";
	public static final String KEY_LIGHTGRAY = "dyeLightGray";
	public static final String KEY_GRAY = "dyeGray";
	public static final String KEY_PINK = "dyePink";
	public static final String KEY_LIME = "dyeLime";
	public static final String KEY_YELLOW = "dyeYellow";
	public static final String KEY_LIGHTBLUE = "dyeLightBlue";
	public static final String KEY_MAGENTA = "dyeMagenta";
	public static final String KEY_ORANGE = "dyeOrange";
	public static final String KEY_WHITE = "dyeWhite";

	public static final String KEY_OIL_TAR = "oiltar";
	public static final String KEY_CRACK_TAR = "cracktar";
	public static final String KEY_COAL_TAR = "coaltar";
	public static final String KEY_WOOD_TAR = "woodtar";

	public static final String KEY_UNIVERSAL_TANK = "ntmuniversaltank";
	public static final String KEY_HAZARD_TANK = "ntmhazardtank";
	public static final String KEY_UNIVERSAL_BARREL = "ntmuniversalbarrel";

	public static final String KEY_TOOL_SCREWDRIVER = "ntmscrewdriver";
	public static final String KEY_TOOL_HANDDRILL = "ntmhanddrill";
	public static final String KEY_TOOL_CHEMISTRYSET = "ntmchemistryset";
	public static final String KEY_TOOL_TORCH = "ntmtorch";

	public static final String KEY_GLYPHID_MEAT = "glyphidMeat";
	public static final String KEY_FLOUR = "foodFlour";
	public static final String KEY_BUTTER = "foodButter";
	public static final String KEY_CHEESE = "foodCheese";
	public static final String KEY_STRAWBERRY = "cropStrawberry";
	public static final String KEY_STRAWBERRY_ICECREAM = "foodStrawberryicecream";
	public static final String KEY_STRAWBERRY_MILKSHAKE = "foodStrawberrymilkshake";

	/*
	 * materials
	 */
	/*
	 * The vanilla set
	 */
	public static final DictFrame WOOD = new DictFrame("Wood");
	public static final DictFrame BONE = new DictFrame("Bone");
	public static final DictFrame COAL = new DictFrame("Coal");
	public static final DictFrame IRON = new DictFrame("Iron");
	public static final DictFrame GOLD = new DictFrame("Gold");
	public static final DictFrame LAPIS = new DictFrame("Lapis");
	public static final DictFrame REDSTONE = new DictFrame("Redstone");
	public static final DictFrame NETHERQUARTZ = new DictFrame("NetherQuartz");
	public static final DictFrame QUARTZ = new DictFrame("Quartz");
	public static final DictFrame DIAMOND = new DictFrame("Diamond");
	public static final DictFrame EMERALD = new DictFrame("Emerald");
	public static final DictFrame GLOWSTONE = new DictFrame("Glowstone");
	/*
	 * The radioactive ones
	 */
	public static final DictFrame U = new DictFrame(Compat.isModLoaded(Compat.MOD_GT6) ? "Uraninite" : "Uranium");
	public static final DictFrame U233 = new DictFrame("Uranium233", "U233");
	public static final DictFrame U235 = new DictFrame("Uranium235", "U235");
	public static final DictFrame U238 = new DictFrame("Uranium238", "U238");
	public static final DictFrame TH232 = new DictFrame("Thorium232", "Th232", "Thorium");
	public static final DictFrame PU = new DictFrame("Plutonium");
	public static final DictFrame PURG = new DictFrame("PlutoniumRG");
	public static final DictFrame PU238 = new DictFrame("Plutonium238", "Pu238");
	public static final DictFrame PU239 = new DictFrame("Plutonium239", "Pu239");
	public static final DictFrame PU240 = new DictFrame("Plutonium240", "Pu240");
	public static final DictFrame PU241 = new DictFrame("Plutonium241", "Pu241");
	public static final DictFrame AM241 = new DictFrame("Americium241", "Am241");
	public static final DictFrame AM242 = new DictFrame("Americium242", "Am242");
	public static final DictFrame CM242 = new DictFrame ("Curium242", "Cm242");
	public static final DictFrame CM243 = new DictFrame ("Curium243", "Cm243");
	public static final DictFrame CM244 = new DictFrame ("Curium244", "Cm244");
	public static final DictFrame CM245 = new DictFrame ("Curium245", "Cm245");
	public static final DictFrame CM246 = new DictFrame ("Curium246", "Cm246");
	public static final DictFrame CM247 = new DictFrame ("Curium247", "Cm247");
	public static final DictFrame CMRG = new DictFrame ("CuriumRG");
	public static final DictFrame CMF = new DictFrame ("CuriumFuel");
	public static final DictFrame BK247 = new DictFrame ("Berkelium247", "Bk247");
	public static final DictFrame CF251 = new DictFrame ("Californium251", "Cf251");
	public static final DictFrame CF252 = new DictFrame ("Californium252", "Cf252");
	public static final DictFrame ES253 = new DictFrame ("Einsteinium253", "Es253");
	public static final DictFrame ES255 = new DictFrame ("Einsteinium255", "Es255");
	public static final DictFrame AMRG = new DictFrame("AmericiumRG");
	public static final DictFrame AMF = new DictFrame("AmericiumFuel");
	public static final DictFrame NP237 = new DictFrame("Neptunium237", "Np237", "Neptunium");
	public static final DictFrame PO210 = new DictFrame("Polonium210", "Po210", "Polonium");
	public static final DictFrame TC99 = new DictFrame("Technetium99", "Tc99");
	public static final DictFrame RA226 = new DictFrame("Radium226", "Ra226");
	public static final DictFrame AC227 = new DictFrame("Actinium227", "Ac227");
	public static final DictFrame CO60 = new DictFrame("Cobalt60", "Co60");
	public static final DictFrame AU198 = new DictFrame("Gold198", "Au198");
	public static final DictFrame PB209 = new DictFrame("Lead209", "Pb209");
	public static final DictFrame SA326 = new DictFrame("Schrabidium");
	public static final DictFrame SA327 = new DictFrame("Solinium");
	public static final DictFrame SBD = new DictFrame("Schrabidate");
	public static final DictFrame SRN = new DictFrame("Schraranium");
	public static final DictFrame GH336 = new DictFrame("Ghiorsium336", "Gh336");
	public static final DictFrame MUD = new DictFrame("WatzMud");
	public static final DictFrame CN989 = new DictFrame("Chinesium989", "Cn989");
	/*
	 * stable
	 */
	/** Titanium */
	public static final DictFrame TI = new DictFrame("Titanium");
	/** Copper */
	public static final DictFrame CU = new DictFrame("Copper");
	public static final DictFrame MINGRADE = new DictFrame("Mingrade");
	/** Nickel */
	public static final DictFrame NI = new DictFrame("NickelPure");
	public static final DictFrame NIM = new DictFrame("Nickel"); // Compatible with "ferrous" so Thermal neither breaks nor derails the tech line we want
	/** Tungsten */
	public static final DictFrame W = new DictFrame("Tungsten");
	public static final DictFrame WC = new DictFrame("TungstenCarbide");
	/** Aluminium */
	public static final DictFrame AL = new DictFrame("Aluminum");
	public static final DictFrame STEEL = new DictFrame("Steel");
	public static final DictFrame STAINLESS = new DictFrame("StainlessSteel");
	/** Californium Steel */
	public static final DictFrame TCALLOY = new DictFrame("TcAlloy");
	/** Cadmium Steel */
	public static final DictFrame CDALLOY = new DictFrame("CdAlloy");
	/** Bismuth Bronze */
	public static final DictFrame BBRONZE = new DictFrame("BismuthBronze");
	/** Arsenic Bronze */
	public static final DictFrame ABRONZE = new DictFrame("ArsenicBronze");
	/** Bismuth Strontium Calcium Copper Oxide */
	public static final DictFrame BSCCO = new DictFrame("BSCCO");
	/** Lead */
	public static final DictFrame PB = new DictFrame("Lead");
	public static final DictFrame BI = new DictFrame("Bismuth");
	public static final DictFrame AS = new DictFrame("Arsenic");
	public static final DictFrame CA = new DictFrame("Calcium");
	public static final DictFrame CD = new DictFrame("Cadmium");
	/** Tantalum */
	public static final DictFrame TA = new DictFrame("Tantalum");
	public static final DictFrame COLTAN = new DictFrame("Coltan");
	/** Niobium */
	public static final DictFrame NB = new DictFrame("Niobium");
	/** Beryllium */
	public static final DictFrame BE = new DictFrame("Beryllium");
	/** Cobalt */
	public static final DictFrame CO = new DictFrame("Cobalt");
	/** Boron */
	public static final DictFrame B = new DictFrame("Boron");
	/** Silicon */
	public static final DictFrame SI = new DictFrame("Silicon");
	public static final DictFrame GRAPHITE = new DictFrame("Graphite");
	public static final DictFrame CARBON = new DictFrame("Carbon");
	public static final DictFrame DURA = new DictFrame("DuraSteel");
	public static final DictFrame POLYMER = new DictFrame("Polymer");
	public static final DictFrame BAKELITE = new DictFrame("Bakelite");
	public static final DictFrame PET = new DictFrame("PET");
	public static final DictFrame PC = new DictFrame("Polycarbonate");
	public static final DictFrame PVC = new DictFrame("PVC");
	public static final DictFrame LATEX = new DictFrame("Latex");
	public static final DictFrame RUBBER = new DictFrame("Rubber");
	public static final DictFrame SEMTEX = new DictFrame("Semtex");
	public static final DictFrame MAGTUNG = new DictFrame("MagnetizedTungsten");
	public static final DictFrame CMB = new DictFrame("CMBSteel");
	public static final DictFrame DESH = new DictFrame("Desh");
	public static final DictFrame STAR = new DictFrame("Starmetal");
	public static final DictFrame GUNMETAL = new DictFrame("GunMetal");
	public static final DictFrame WEAPONSTEEL = new DictFrame("WeaponSteel");
	public static final DictFrame BIGMT = new DictFrame("Saturnite");
	public static final DictFrame FERRO = new DictFrame("Ferrouranium");
	public static final DictFrame EUPH = new DictFrame("Euphemium");
	public static final DictFrame DNT = new DictFrame("Dineutronium");
	public static final DictFrame FIBER = new DictFrame("Fiberglass");
	public static final DictFrame ASBESTOS = new DictFrame("Asbestos");
	public static final DictFrame OSMIRIDIUM = new DictFrame("Osmiridium");
	public static final DictFrame HAFNIUM = new DictFrame("Hafnium");
	public static final DictFrame IRIDIUM = new DictFrame("Iridium");
	public static final DictFrame ZI = new DictFrame("Zinc");
	public static final DictFrame GALLIUM = new DictFrame("Gallium");
	public static final DictFrame GAAS = new DictFrame("GalliumArsenide");
	/** Mint */
	public static final DictFrame MEN = new DictFrame("Menthol");

	public static final DictFrame MIN = new DictFrame("Mineral");
	/*
	 * Dusts and gem ores
	 */
	/** Sulfur */
	public static final DictFrame S = new DictFrame("Sulfur");
	/** Niter */
	public static final DictFrame KNO = new DictFrame("Saltpeter");
	/** Fluorite */
	public static final DictFrame F = new DictFrame("Fluorite");
	public static final DictFrame LIGNITE = new DictFrame("Lignite");
	public static final DictFrame COALCOKE = new DictFrame("CoalCoke");
	public static final DictFrame PETCOKE = new DictFrame("PetCoke");
	public static final DictFrame LIGCOKE = new DictFrame("LigniteCoke");
	public static final DictFrame CINNABAR = new DictFrame("Cinnabar");
	public static final DictFrame BORAX = new DictFrame("Borax");
	public static final DictFrame CHLOROCALCITE = new DictFrame("Chlorocalcite");
	public static final DictFrame MOLYSITE = new DictFrame("Molysite");
	public static final DictFrame SODALITE = new DictFrame("Sodalite");
	public static final DictFrame VOLCANIC = new DictFrame("Volcanic");
	public static final DictFrame HEMATITE = new DictFrame("Hematite");
	public static final DictFrame CONGLOMERATE  = new DictFrame("Conglomerate");
	public static final DictFrame MALACHITE = new DictFrame("Malachite");
	public static final DictFrame LIMESTONE = new DictFrame("Limestone");
	public static final DictFrame SLAG = new DictFrame("Slag");
	public static final DictFrame BAUXITE = new DictFrame("Bauxite");
	public static final DictFrame CRYOLITE = new DictFrame("Cryolite");
	public static final DictFrame RICHMAGMA = new DictFrame("RichMagma");
	public static final DictFrame FLOUR = new DictFrame("foodFlour");
	public static final DictFrame PENTLANDITE = new DictFrame("Pentlandite");

	/*
	 * Hazards and odds and ends
	 */
	/** Lithium */
	public static final DictFrame LI = new DictFrame("Lithium");
	/** Sodium */
	public static final DictFrame NA = new DictFrame("Sodium");
	/*
	 * Phosphorus
	 */
	public static final DictFrame P_WHITE = new DictFrame("WhitePhosphorus");
	public static final DictFrame P_RED = new DictFrame("RedPhosphorus");
	/*
	 * Rare metals
	 */
	public static final DictFrame AUSTRALIUM = new DictFrame("Australium");
	public static final DictFrame TASMANITE = new DictFrame("Tasmanite");
	public static final DictFrame AYERITE = new DictFrame("Ayerite");

	/*
	 * rare earths
	 */
	public static final DictFrame RAREEARTH = new DictFrame("RareEarth");
	/** Lanthanum */
	public static final DictFrame LA = new DictFrame("Lanthanum");
	/** Zirconium */
	public static final DictFrame ZR = new DictFrame("Zirconium");
	/** Neodymium */
	public static final DictFrame ND = new DictFrame("Neodymium");
	/** Cerium */
	public static final DictFrame CE = new DictFrame("Cerium");
	/*
	 * That nitric acid business
	 */
	/** Iodine */
	public static final DictFrame I = new DictFrame("Iodine");
	/** Astatine */
	public static final DictFrame AT = new DictFrame("Astatine");
	/** Caesium */
	public static final DictFrame CS = new DictFrame("Caesium");
	/** Strontium */
	public static final DictFrame ST = new DictFrame("Strontium");
	/** Bromine */
	public static final DictFrame BR = new DictFrame("Bromine");
	/** Tennessine */
	public static final DictFrame TS = new DictFrame("Tennessine") ;
	/*
	 * Fission products
	 */
	public static final DictFrame SR = new DictFrame("Strontium");
	public static final DictFrame SR90 = new DictFrame("Strontium90", "Sr90");
	public static final DictFrame I131 = new DictFrame("Iodine131", "I131");
	public static final DictFrame XE135 = new DictFrame("Xenon135", "Xe135");
	public static final DictFrame CS137 = new DictFrame("Caesium137", "Cs137");
	public static final DictFrame AT209 = new DictFrame("Astatine209", "At209");

	/*
	 * collection
	 */
	/** The various elastic polymers */
	public static final DictGroup ANY_RUBBER = new DictGroup("AnyRubber", LATEX, RUBBER);
	/** The various refining-downstream polymers, like Teflon ("polymer") and bakelite */
	public static final DictGroup ANY_PLASTIC = new DictGroup("AnyPlastic", POLYMER, BAKELITE);		//The Any prefix means only the secondary prefix applies; the shape prefix still works
	/** The various vacuum-downstream polymers, like PET and PVC */
	public static final DictGroup ANY_HARDPLASTIC = new DictGroup("AnyHardPlastic", PC, PVC);
	/** The various reactor-downstream steels, like TCA and CDA */
	public static final DictGroup ANY_RESISTANTALLOY = new DictGroup("AnyResistantAlloy", TCALLOY, CDALLOY);
	/** The various RBMK-downstream bronzes, like BB and AB */
	public static final DictGroup ANY_BISMOIDBRONZE = new DictGroup("AnyBismoidBronze", BBRONZE, ABRONZE);
	/** The various "powder" propellants, like gunpowder, propellant, smokeless powder */
	public static final DictFrame ANY_GUNPOWDER = new DictFrame("AnyPropellant");
	/** The various smokeless powders */
	public static final DictFrame ANY_SMOKELESS = new DictFrame("AnySmokeless");
	/** The various plastic explosives, like C-4 and Semtex H */
	public static final DictFrame ANY_PLASTICEXPLOSIVE = new DictFrame("AnyPlasticexplosive");
	/** One tier up from TNT (so no plain TNT), like Semtex-H */
	public static final DictFrame ANY_HIGHEXPLOSIVE = new DictFrame("AnyHighexplosive");
	public static final DictFrame ANY_COKE = new DictFrame("AnyCoke", "Coke");
	public static final DictGroup ANY_COAL_COKE = new DictGroup("AnyCoalCoke", ANY_COKE, COAL);
	public static final DictFrame ANY_CONCRETE = new DictFrame("Concrete");			//No any-prefix means reads go through the any()/anys() getters, registering as a shape-less any
	public static final DictGroup ANY_TAR = new DictGroup("Tar", KEY_OIL_TAR, KEY_COAL_TAR, KEY_CRACK_TAR, KEY_WOOD_TAR);
	/** The various RBMK-downstream special gate materials, i.e. bismuth and arsenic */
	public static final DictGroup ANY_BISMOID = new DictGroup("AnyBismoid", BI, AS);
	public static final DictFrame ANY_ASH = new DictFrame("Ash");

	/*
	 * The registration surface. The data table is OreDictRegistration, which uses these chained doors as a DSL;
	 * the methods below only hang keys into the ore dict, noting hazards and arc-smeltable lists along the way.
	 */

	/** Every ingot/ore/plate/block-shaped ore key counts on the arc furnace side; ArcFurnaceRecipes reads this cell directly */
	public static final HashSet<ComparableStack> arcSmeltable = new HashSet<>();

	/** Adding an alias itself fires OreRegisterEvent again, so a gate is needed or it recurses forever */
	private static boolean recursionBrake = false;

	/** Registers the DictGroup collections as alias tables; must run before registerOres() */
	public static void registerGroups() {
		ANY_RUBBER.addPrefix(MaterialShapes.INGOT, true);
		ANY_PLASTIC.addPrefix(MaterialShapes.INGOT, true).addPrefix(MaterialShapes.DUST, true).addPrefix(MaterialShapes.BLOCK, true).addPrefix(MaterialShapes.GRIP, true).addPrefix(MaterialShapes.STOCK, true);
		ANY_HARDPLASTIC.addPrefix(MaterialShapes.INGOT, true).addPrefix(MaterialShapes.STOCK, true).addPrefix(MaterialShapes.GRIP, true);
		ANY_RESISTANTALLOY.addPrefix(MaterialShapes.INGOT, true).addPrefix(MaterialShapes.DUST, true).addPrefix(MaterialShapes.CASTPLATE, true).addPrefix(MaterialShapes.WELDEDPLATE, true).addPrefix(MaterialShapes.BLOCK, true)
		.addPrefix(MaterialShapes.LIGHTBARREL, true).addPrefix(MaterialShapes.HEAVYBARREL, true).addPrefix(MaterialShapes.LIGHTRECEIVER, true).addPrefix(MaterialShapes.HEAVYRECEIVER, true);
		ANY_BISMOIDBRONZE.addPrefix(MaterialShapes.INGOT, true).addPrefix(MaterialShapes.CASTPLATE, true).addPrefix(MaterialShapes.LIGHTBARREL, true).addPrefix(MaterialShapes.HEAVYBARREL, true).addPrefix(MaterialShapes.LIGHTRECEIVER, true).addPrefix(MaterialShapes.HEAVYRECEIVER, true);
		ANY_TAR.addPrefix(MaterialShapes.ANY, false);
		ANY_COAL_COKE.addPrefix(MaterialShapes.DUST, true);
		ANY_BISMOID.addPrefix(MaterialShapes.NUGGET, true).addPrefix(MaterialShapes.INGOT, true).addPrefix(MaterialShapes.BLOCK, true);
	}

	/**
	 * When other mods (GregTech, Tinkers and friends) register into the ore dict, add the HBM-known keys per the alias table.
	 *
	 * 1.12.2's event body exposes getName()/getOre(), not the old pair of public fields.
	 */
	public static void onRegisterOre(OreRegisterEvent event) {
		if(recursionBrake) return;

		recursionBrake = true;

		HashSet<String> strings = reRegistration.get(event.getName());

		if(strings != null) {
			for(String name : strings) {
				OreDictionary.registerOre(name, event.getOre());
				MainRegistry.logger.info("Re-registration for " + event.getName() + " to " + name);
			}
		}

		recursionBrake = false;

		if(event.getName().startsWith("ingot") || event.getName().startsWith("ore") || event.getName().startsWith("plate") || event.getName().startsWith("block")) {
			arcSmeltable.add(new ComparableStack(event.getOre()));
		}
	}

	/** Vanilla pieces' registration events fire before HBM's handler is even attached; these can only be patched by hand */
	public static void compensateMojangSpaghettiBullshit() {

		arcSmeltable.add(new ComparableStack(Blocks.GOLD_ORE));
		arcSmeltable.add(new ComparableStack(Blocks.IRON_ORE));
		arcSmeltable.add(new ComparableStack(Blocks.LAPIS_ORE));
		arcSmeltable.add(new ComparableStack(Blocks.DIAMOND_ORE));
		arcSmeltable.add(new ComparableStack(Blocks.REDSTONE_ORE));
		arcSmeltable.add(new ComparableStack(Blocks.EMERALD_ORE));
		arcSmeltable.add(new ComparableStack(Blocks.QUARTZ_ORE));
		arcSmeltable.add(new ComparableStack(Blocks.GOLD_BLOCK));
		arcSmeltable.add(new ComparableStack(Blocks.IRON_BLOCK));
		arcSmeltable.add(new ComparableStack(Blocks.LAPIS_BLOCK));
		arcSmeltable.add(new ComparableStack(Blocks.DIAMOND_BLOCK));
		arcSmeltable.add(new ComparableStack(Blocks.REDSTONE_BLOCK));
		arcSmeltable.add(new ComparableStack(Blocks.EMERALD_BLOCK));
		arcSmeltable.add(new ComparableStack(Blocks.QUARTZ_BLOCK));
		arcSmeltable.add(new ComparableStack(Items.IRON_INGOT));
		arcSmeltable.add(new ComparableStack(Items.GOLD_INGOT));
		arcSmeltable.add(new ComparableStack(Items.BRICK));
		arcSmeltable.add(new ComparableStack(Items.NETHERBRICK));
	}

	/**
	 * The ore dict face is a breeding ground for "compiles fine, starts fine, logs nothing, but nothing exists in game"-class defects,
	 * so the wrap-up counts a few probes out loud: the registration only counts as landed when all probes hit.
	 */
	public static void selfCheck() {

		String[] probe = { "dustIron", "ingotSteel", "plateTitanium", "nuggetUranium", "dustQuartz", "dyeRed", "oreNetherPlutonium" };

		int hits = 0;
		for(String name : probe) {
			if(!OreDictionary.getOres(name).isEmpty()) hits++;
			else MainRegistry.logger.warn("[hbm] ore-dict probe miss: " + name);
		}

		MainRegistry.logger.info("[hbm] ore-dict self-check: " + hits + "/" + probe.length + " probes resolved, arc-smeltable keys: " + arcSmeltable.size());
	}

	public static class DictFrame {
		public String[] mats;

		public DictFrame(String... mats) {
			this.mats = mats;
		}

		/*
		 * A shortcut for grabbing ore dict names while writing recipes
		 */
		public String any() {			return MaterialShapes.ANY.name()				+ mats[0]; }
		public String nugget() {		return MaterialShapes.NUGGET.name()			+ mats[0]; }
		public String tiny() {			return MaterialShapes.TINY.name()				+ mats[0]; }
		public String bolt() {			return MaterialShapes.BOLT.name()				+ mats[0]; }
		public String ingot() {			return MaterialShapes.INGOT.name()			+ mats[0]; }
		public String dustTiny() {		return MaterialShapes.DUSTTINY.name()			+ mats[0]; }
		public String dust() {			return MaterialShapes.DUST.name()				+ mats[0]; }
		public String gem() {			return MaterialShapes.GEM.name()				+ mats[0]; }
		public String crystal() {		return MaterialShapes.CRYSTAL.name()			+ mats[0]; }
		public String plate() {			return MaterialShapes.PLATE.name()			+ mats[0]; }
		public String plateCast() {		return MaterialShapes.CASTPLATE.name()		+ mats[0]; }
		public String plateWelded() {	return MaterialShapes.WELDEDPLATE.name()		+ mats[0]; }
		@Deprecated public String heavyComp() { return MaterialShapes.WELDEDPLATE.name() + mats[0]; }
		public String wireFine() {		return MaterialShapes.WIRE.name()				+ mats[0]; }
		public String wireDense() {		return MaterialShapes.DENSEWIRE.name()		+ mats[0]; }
		public String shell() {			return MaterialShapes.SHELL.name()			+ mats[0]; }
		public String pipe() {			return MaterialShapes.PIPE.name()				+ mats[0]; }
		public String billet() {		return MaterialShapes.BILLET.name()			+ mats[0]; }
		public String block() {			return MaterialShapes.BLOCK.name()			+ mats[0]; }
		public String ore() {			return MaterialShapes.ORE.name()				+ mats[0]; }
		public String fragment() {		return MaterialShapes.FRAGMENT.name()			+ mats[0]; }
		public String lightBarrel() {	return MaterialShapes.LIGHTBARREL.name()		+ mats[0]; }
		public String heavyBarrel() {	return MaterialShapes.HEAVYBARREL.name()		+ mats[0]; }
		public String lightReceiver() {	return MaterialShapes.LIGHTRECEIVER.name()	+ mats[0]; }
		public String heavyReceiver() {	return MaterialShapes.HEAVYRECEIVER.name()	+ mats[0]; }
		public String mechanism() {		return MaterialShapes.MECHANISM.name()		+ mats[0]; }
		public String stock() {			return MaterialShapes.STOCK.name()			+ mats[0]; }
		public String grip() {			return MaterialShapes.GRIP.name()				+ mats[0]; }
		public String[] all(MaterialShapes shape) {				return appendToAll(shape.prefixes); }

		private String[] appendToAll(String... prefix) {

			String[] names = new String[mats.length * prefix.length];

			for(int i = 0; i < mats.length; i++) {
				for(int j = 0; j < prefix.length; j++) {
					names[i * prefix.length + j] = prefix[j] + mats[i];
				}
			}
			return names;
		}

		/** Takes an item plus an ItemStack with the enum ordinal as meta; pure syntax-sugar shorthand */
		public static ItemStack fromOne(Item item, Enum<?> en) {
			return new ItemStack(item, 1, en.ordinal());
		}
		public static ItemStack fromOne(Block block, Enum<?> en) {
			return new ItemStack(block, 1, en.ordinal());
		}
		public static ItemStack fromOne(Item item, Enum<?> en, int stacksize) {
			return new ItemStack(item, stacksize, en.ordinal());
		}
		public static ItemStack fromOne(Block block, Enum<?> en, int stacksize) {
			return new ItemStack(block, stacksize, en.ordinal());
		}
		/** Like fromOne but returning an ItemStack array. The array type is Object[] so the ODM methods can swallow it. One entry per enum constant */
		public static Object[] fromAll(Item item, Class<? extends Enum> en) {
			Enum<?>[] vals = en.getEnumConstants();
			Object[] stacks = new Object[vals.length];

			for(int i = 0; i < vals.length; i++) {
				stacks[i] = new ItemStack(item, 1, vals[i].ordinal());
			}
			return stacks;
		}
		public static Object[] fromAll(Block block, Class<? extends Enum> en) {
			Enum<?>[] vals = en.getEnumConstants();
			Object[] stacks = new Object[vals.length];

			for(int i = 0; i < vals.length; i++) {
				stacks[i] = new ItemStack(block, 1, vals[i].ordinal());
			}
			return stacks;
		}

		/*
		 * Registration surface: the chained doors below hang pieces into the ore keys for each material name in this frame,
		 * also registering the hazards accumulated along the chain onto the same key. Data table: OreDictRegistration.
		 */
		private float hazMult = 1.0F;
		private final List<HazardEntry> hazards = new ArrayList<>();

		public DictFrame rad(float rad) {		return this.haz(new HazardEntry(HazardRegistry.RADIATION, rad)); }
		public DictFrame hot(float time) {		return this.haz(new HazardEntry(HazardRegistry.HOT, time)); }
		public DictFrame blinding(float time) {	return this.haz(new HazardEntry(HazardRegistry.BLINDING, time)); }
		public DictFrame asbestos(float asb) {	return this.haz(new HazardEntry(HazardRegistry.ASBESTOS, asb)); }
		public DictFrame hydro(float h) {		return this.haz(new HazardEntry(HazardRegistry.HYDROACTIVE, h)); }
		public DictFrame digamma(float dad) {	return this.haz(new HazardEntry(HazardRegistry.DIGAMMA, dad)); }
		public DictFrame coal(float time) {		return this.haz(new HazardEntry(HazardRegistry.COAL, time)); }
		public DictFrame explosive(float e) {	return this.haz(new HazardEntry(HazardRegistry.EXPLOSIVE, e)); }
		public DictFrame neutron(float neut) {	return this.haz(new HazardEntry(HazardRegistry.NEUTRON, neut)); }

		public DictFrame haz(HazardEntry hazard) {
			hazards.add(hazard);
			return this;
		}

		public DictFrame any(Object... thing) {
			return makeObject(MaterialShapes.ANY, thing);
		}
		public DictFrame nugget(Object... nugget) {
			hazMult = HazardRegistry.nugget;
			return makeObject(MaterialShapes.NUGGET, nugget).makeObject(MaterialShapes.TINY, nugget);
		}
		public DictFrame ingot(Object... ingot) {
			hazMult = HazardRegistry.ingot;
			return makeObject(MaterialShapes.INGOT, ingot);
		}
		public DictFrame dustSmall(Object... dustSmall) {
			hazMult = HazardRegistry.powder_tiny;
			return makeObject(MaterialShapes.DUSTTINY, dustSmall);
		}
		public DictFrame dust(Object... dust) {
			hazMult = HazardRegistry.powder;
			return makeObject(MaterialShapes.DUST, dust);
		}
		public DictFrame gem(Object... gem) {
			hazMult = HazardRegistry.gem;
			return makeObject(MaterialShapes.GEM, gem);
		}
		public DictFrame crystal(Object... crystal) {
			hazMult = HazardRegistry.gem;
			return makeObject(MaterialShapes.CRYSTAL, crystal);
		}
		public DictFrame plate(Object... plate) {
			hazMult = HazardRegistry.plate;
			return makeObject(MaterialShapes.PLATE, plate);
		}
		public DictFrame plateCast(Object... plate) {
			hazMult = HazardRegistry.plateCast;
			return makeObject(MaterialShapes.CASTPLATE, plate);
		}
		public DictFrame billet(Object... billet) {
			hazMult = HazardRegistry.billet;
			return makeObject(MaterialShapes.BILLET, billet);
		}
		public DictFrame block(Object... block) {
			hazMult = HazardRegistry.block;
			return makeObject(MaterialShapes.BLOCK, block);
		}
		public DictFrame ore(Object... ore) {
			hazMult = HazardRegistry.ore;
			return makeObject(MaterialShapes.ORE, ore);
		}
		/** Ignores metadata; takes the whole block/item family */
		public DictFrame oreAll(Object... ore) {
			hazMult = HazardRegistry.ore;
			return makeObject(MaterialShapes.ORE, OreDictionary.WILDCARD_VALUE, ore);
		}
		public DictFrame oreNether(Object... oreNether) {
			hazMult = HazardRegistry.ore;
			return makeObject(MaterialShapes.ORENETHER, oreNether);
		}

		public DictFrame makeObject(MaterialShapes shape, Object... objects) {
			return makeObject(shape, 0, objects);
		}

		public DictFrame makeObject(MaterialShapes shape, int meta, Object... objects) {

			String tag = shape.name();
			for(Object o : objects) {
				if(o instanceof Item)			registerStack(tag, new ItemStack((Item) o, 1, meta));
				if(o instanceof Block)			registerStack(tag, new ItemStack((Block) o, 1, meta));
				if(o instanceof ItemStack)		registerStack(tag, (ItemStack) o);
			}

			return this;
		}

		public DictFrame hazIngot() {
			hazMult = HazardRegistry.ingot;
			return autoRegHazard(MaterialShapes.INGOT);
		}

		// TODO:  rethink this. currently, keys are only registered on-demand if the dict frame has a valid entry, even though we can maximize compatibility
		// by simply registereing all known shapes in the haz reg, whether it exists or not
		public DictFrame autoRegHazard(MaterialShapes shape) {
			String tag = shape.name();
			for(String mat : mats) {
				registerHazards(hazards, hazMult, tag + mat);
			}
			return this;
		}

		public static void registerHazards(List<HazardEntry> hazards, float hazMult, String dictKey) {

			if(!hazards.isEmpty() && hazMult > 0F) {
				HazardData data = new HazardData().setMutex(0b1);

				for(HazardEntry hazard : hazards) {
					data.addEntry(hazard.clone(hazMult));
				}

				HazardSystem.register(dictKey, data);
			}
		}

		public void registerStack(String tag, ItemStack stack) {
			for(String mat : mats) {
				OreDictionary.registerOre(tag + mat, stack);
				registerHazards(hazards, hazMult, tag + mat);
			}

			/*
			 * GregTech's element ingots carry no ore prefix and uneven granularity (TH and U are 20 "tiny" per ingot, boron only 12),
			 * so these all register again as ingots, this time prefix-free.
			 */
			if("ingot".equals(tag)) {
				registerStack("", stack);
			}
		}
	}

	public static class DictGroup {

		private String groupName;
		private HashSet<String> names = new HashSet<>();

		public DictGroup(String groupName) {
			this.groupName = groupName;
		}
		public DictGroup(String groupName, String... names) {
			this(groupName);
			this.addNames(names);
		}
		public DictGroup(String groupName, DictFrame... frames) {
			this(groupName);
			this.addFrames(frames);
		}

		public DictGroup addNames(String... names) {
			for(String mat : names) this.names.add(mat);
			return this;
		}
		public DictGroup addFrames(DictFrame... frames) {
			for(DictFrame frame : frames) this.addNames(frame.mats);
			return this;
		}

		/**
		 * Registers one alias per material name of every joined DictFrame, prefixed with the given one
		 * @param prefix the prefix shared by both sides of the alias
		 * @return
		 */
		public DictGroup addPrefix(MaterialShapes shape, boolean inputPrefix) {

			String prefix = shape.name();
			String group = prefix + groupName;

			for(String name : names) {
				String original = (inputPrefix ? prefix : "") + name;
				addReRegistration(original, group);
			}

			return this;
		}
		/**
		 * Like addPrefix, but the alias's input side ignores prefix and material name
		 * @param prefix the result side's prefix (spelled out: prefix + group name)
		 * @param original the full original ore key, unconstrained by naming rules
		 * @return
		 */
		public DictGroup addFixed(String prefix, String original) {

			String group = prefix + groupName;
			addReRegistration(original, group);
			return this;
		}

		public String any() {			return MaterialShapes.ANY.name()				+ groupName; }
		public String nugget() {		return MaterialShapes.NUGGET.name()			+ groupName; }
		public String tiny() {			return MaterialShapes.TINY.name()				+ groupName; }
		public String bolt() {			return MaterialShapes.BOLT.name()				+ groupName; }
		public String ingot() {			return MaterialShapes.INGOT.name()			+ groupName; }
		public String dustTiny() {		return MaterialShapes.DUSTTINY	.name()		+ groupName; }
		public String dust() {			return MaterialShapes.DUST.name()				+ groupName; }
		public String gem() {			return MaterialShapes.GEM.name()				+ groupName; }
		public String crystal() {		return MaterialShapes.CRYSTAL.name()			+ groupName; }
		public String plate() {			return MaterialShapes.PLATE.name()			+ groupName; }
		public String plateCast() {		return MaterialShapes.CASTPLATE.name()		+ groupName; }
		public String plateWelded() {	return MaterialShapes.WELDEDPLATE.name()		+ groupName; }
		@Deprecated public String heavyComp() { return MaterialShapes.WELDEDPLATE.name() + groupName; }
		public String wireFine() {		return MaterialShapes.WIRE.name()				+ groupName; }
		public String wireDense() {		return MaterialShapes.DENSEWIRE.name()		+ groupName; }
		public String billet() {		return MaterialShapes.BILLET.name()			+ groupName; }
		public String block() {			return MaterialShapes.BLOCK.name()			+ groupName; }
		public String ore() {			return MaterialShapes.ORE.name()				+ groupName; }
		public String lightBarrel() {	return MaterialShapes.LIGHTBARREL.name()		+ groupName; }
		public String heavyBarrel() {	return MaterialShapes.HEAVYBARREL.name()		+ groupName; }
		public String lightReceiver() {	return MaterialShapes.LIGHTRECEIVER.name()	+ groupName; }
		public String heavyReceiver() {	return MaterialShapes.HEAVYRECEIVER.name()	+ groupName; }
		public String mechanism() {		return MaterialShapes.MECHANISM.name()		+ groupName; }
		public String stock() {			return MaterialShapes.STOCK.name()			+ groupName; }
		public String grip() {			return MaterialShapes.GRIP.name()				+ groupName; }
	}

	private static void addReRegistration(String original, String additional) {

		HashSet<String> strings = reRegistration.get(original);

		if(strings == null)
			strings = new HashSet<>();

		strings.add(additional);

		reRegistration.put(original, strings);
	}
}
