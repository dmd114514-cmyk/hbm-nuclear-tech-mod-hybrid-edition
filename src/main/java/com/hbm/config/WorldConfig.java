package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

/**
 * Worldgen parameters: ores and veins, bedrock ore, nether/end, oil and gas fields, surface structures, meteorites.
 * Field names keep the original mod's spelling — worldgen and dimension code read them by name; renaming means every call site silently falls back to defaults.
 *
 * Migrating this whole family from the old Configuration path to @Config, four things to note:
 * One: defaults take the def of the old loadFromConfig read statement, not the field initializer. They disagree on six entries
 *     （uraniumSpawn、asbestosSpawn、mineralSpawn、nickelSpawn、meteoriteSpawn、bedrockCadmiumSpawn），
 *     and what actually took effect in old config files was the read statement's value.
 * Two: craterBiomeId / craterBiomeInnerId / craterBiomeOuterId didn't come over — 1.12.2 addresses biomes by registry name,
 *     numeric IDs are gone, and the EndlessIDs 12000 offset is retired with them (same handling as PotionConfig did for potion IDs).
 *     The crater biomes themselves stay as they were; the four radiation entries stay configurable.
 * Three: ironSpawn had no read statement in the old version either (the author missed it); it stays a constant marked @Ignore here,
 *     rather than gaining a knob that never existed.
 *
 * Four: @Name is always derived from the field name, not the old config keys — old keys stripped planet and dimension qualifiers
 *     (2.00_uraniumSpawnrate and 2.N00_uraniumSpawnrate both reduced to "Uranium Spawnrate"), and @Config uses the tag as the property name,
 *     so a collision merges two knobs into one — measured, 141 entries wrote out as 134 lines.
 * The descriptions keep their English originals: of these 142 entries nearly ninety are mineral names, hand-translating each would only breed mistranslations;
 * and it's user-facing config text, not code comments. Localization is a separate pass, with a reviewed glossary.
 */
@Config(modid = RefStrings.MODID, name = "hbm-world")
public class WorldConfig {

	/* ---- ores, veins, deposits, oil and gas fields ---- */

	@Name("Overworld Ore")
	@Comment("General switch for whether overworld ores should be generated. Does not include special structures like oil.")
	public static boolean overworldOre = true;

	@Name("Nether Ore")
	@Comment("General switch for whether nether ores should be generated.")
	public static boolean netherOre = true;

	@Name("End Ore")
	@Comment("General switch for whether end ores should be generated. Does not include special structures like trixite crystals.")
	public static boolean endOre = true;

	/** The old version never had a read statement for this one either; it stays a constant rather than becoming a new knob */
	@Config.Ignore
	public static int ironSpawn = 9;

	@Name("Uranium Spawn")
	@Comment("Amount of uranium ore veins per chunk")
	public static int uraniumSpawn = 7;

	@Name("Thorium Spawn")
	@Comment("Amount of thorium ore veins per chunk")
	public static int thoriumSpawn = 7;

	@Name("Titanium Spawn")
	@Comment("Amount of titanium ore veins per chunk")
	public static int titaniumSpawn = 8;

	@Name("Sulfur Spawn")
	@Comment("Amount of sulfur ore veins per chunk")
	public static int sulfurSpawn = 5;

	@Name("Aluminium Spawn")
	@Comment("Amount of aluminium ore veins per chunk")
	public static int aluminiumSpawn = 7;

	@Name("Copper Spawn")
	@Comment("Amount of copper ore veins per chunk")
	public static int copperSpawn = 12;

	@Name("Nickel Spawn")
	@Comment("Amount of nickel ore veins per chunk")
	public static int nickelSpawn = 12;

	@Name("Zinc Spawn")
	@Comment("Amount of zinc ore veins per chunk")
	public static int zincSpawn = 8;

	@Name("Mineral Spawn")
	@Comment("Amount of mineral ore veins per chunk")
	public static int mineralSpawn = 4;

	@Name("Fluorite Spawn")
	@Comment("Amount of fluorite ore veins per chunk")
	public static int fluoriteSpawn = 6;

	@Name("Niter Spawn")
	@Comment("Amount of niter ore veins per chunk")
	public static int niterSpawn = 6;

	@Name("Tungsten Spawn")
	@Comment("Amount of tungsten ore veins per chunk")
	public static int tungstenSpawn = 10;

	@Name("Lead Spawn")
	@Comment("Amount of lead ore veins per chunk")
	public static int leadSpawn = 6;

	@Name("Beryllium Spawn")
	@Comment("Amount of beryllium ore veins per chunk")
	public static int berylliumSpawn = 6;

	@Name("Lignite Spawn")
	@Comment("Amount of lignite ore veins per chunk")
	public static int ligniteSpawn = 2;

	@Name("Asbestos Spawn")
	@Comment("Amount of asbestos ore veins per chunk")
	public static int asbestosSpawn = 2;

	@Name("Rare Spawn")
	@Comment("Amount of rare earth ore veins per chunk")
	public static int rareSpawn = 6;

	@Name("Lithium Spawn")
	@Comment("Amount of schist lithium ore veins per chunk")
	public static int lithiumSpawn = 6;

	@Name("Cinnebar Spawn")
	@Comment("Amount of cinnebar ore veins per chunk")
	public static int cinnebarSpawn = 1;

	@Name("Gassshale Spawn")
	@Comment("Amount of oil shale veins per chunk")
	public static int gassshaleSpawn = 5;

	@Name("Gasbubble Spawn")
	@Comment("Spawns a gas bubble every nTH chunk")
	public static int gasbubbleSpawn = 12;

	@Name("Explosivebubble Spawn")
	@Comment("Spawns an explosive gas bubble every nTH chunk")
	public static int explosivebubbleSpawn = 0;

	@Name("Cobalt Spawn")
	@Comment("Amount of cobalt ore veins per chunk")
	public static int cobaltSpawn = 2;

	@Name("Oil Spawn")
	@Comment("Spawns an oil bubble every nTH chunk")
	public static int oilSpawn = 100;

	@Name("Bedrock Oil Spawn")
	@Comment("Spawns a bedrock oil node every nTH chunk")
	public static int bedrockOilSpawn = 200;

	@Name("Meteorite Spawn")
	@Comment("Spawns a fallen meteorite every nTH chunk")
	public static int meteoriteSpawn = 200;

	@Name("Duna Oil Spawn")
	@Comment("Spawns an oil bubble every nTH chunk (on Duna)")
	public static int dunaOilSpawn = 100;

	@Name("Eve Gas Spawn")
	@Comment("Spawns a natural gas bubble every nTH chunk (on Eve)")
	public static int eveGasSpawn = 100;

	@Name("Laythe Oil Spawn")
	@Comment("Spawns a DS oil bubble every nTH chunk (on Laythe)")
	public static int laytheOilSpawn = 100;

	@Name("Mun Brine Spawn")
	@Comment("Spawns a brine bubble every nTH chunk (on Mun)")
	public static int munBrineSpawn = 100;

	@Name("Minmus Brine Spawn")
	@Comment("Spawns a brine bubble every nTH chunk (on Minmus)")
	public static int minmusBrineSpawn = 100;

	@Name("Ike Brine Spawn")
	@Comment("Spawns a brine bubble every nTH chunk (on Ike)")
	public static int ikeBrineSpawn = 100;

	@Name("Bedrock Oil Per Deposit")
	@Comment("Oil extracted per bedrock oil block suck")
	public static int bedrockOilPerDeposit = 100;

	@Name("Bedrock Gas Per Deposit Min")
	@Comment("Minimum natural gas extracted per bedrock oil block suck")
	public static int bedrockGasPerDepositMin = 10;

	@Name("Bedrock Gas Per Deposit Max")
	@Comment("Maximum natural gas extracted per bedrock oil block suck")
	public static int bedrockGasPerDepositMax = 50;

	@Name("Earth Oil Per Deposit")
	@Comment("Oil extracted per Earth oil block suck")
	public static int earthOilPerDeposit = 500;

	@Name("Earth Gas Per Deposit Min")
	@Comment("Minimum natural gas extracted per Earth oil block suck")
	public static int earthGasPerDepositMin = 100;

	@Name("Earth Gas Per Deposit Max")
	@Comment("Maximum natural gas extracted per Earth oil block suck")
	public static int earthGasPerDepositMax = 500;

	@Name("Earth Oil Drain Chance")
	@Comment("Chance for an Earth oil block to become empty on suck")
	public static double earthOilDrainChance = 0.05D;

	@Name("Duna Oil Per Deposit")
	@Comment("Oil extracted per Duna oil block suck")
	public static int dunaOilPerDeposit = 200;

	@Name("Duna Gas Per Deposit Min")
	@Comment("Minimum natural gas extracted per Duna oil block suck")
	public static int dunaGasPerDepositMin = 100;

	@Name("Duna Gas Per Deposit Max")
	@Comment("Maximum natural gas extracted per Duna oil block suck")
	public static int dunaGasPerDepositMax = 500;

	@Name("Duna Oil Drain Chance")
	@Comment("Chance for a Duna oil block to become empty on suck")
	public static double dunaOilDrainChance = 0.1D;

	@Name("Laythe Oil Per Deposit")
	@Comment("Desulfurized Oil extracted per Laythe oil block suck")
	public static int laytheOilPerDeposit = 500;

	@Name("Laythe Gas Per Deposit Min")
	@Comment("Minimum natural gas extracted per Laythe oil block suck")
	public static int laytheGasPerDepositMin = 100;

	@Name("Laythe Gas Per Deposit Max")
	@Comment("Maximum natural gas extracted per Laythe oil block suck")
	public static int laytheGasPerDepositMax = 500;

	@Name("Laythe Oil Drain Chance")
	@Comment("Chance for a Laythe oil block to become empty on suck")
	public static double laytheOilDrainChance = 0.05D;

	@Name("Tekto Oil Spawn")
	@Comment("Spawns a Tekto oil bubble every nTH chunk (on Tekto)")
	public static int tektoOilSpawn = 100;

	@Name("Tekto Oil Per Deposit")
	@Comment("Oil extracted per Tekto oil block suck")
	public static int tektoOilPerDeposit = 500;

	@Name("Tekto Gas Per Deposit Min")
	@Comment("Minimum natural gas extracted per Tekto oil block suck")
	public static int tektoGasPerDepositMin = 100;

	@Name("Tekto Gas Per Deposit Max")
	@Comment("Maximum natural gas extracted per Tekto oil block suck")
	public static int tektoGasPerDepositMax = 500;

	@Name("Tekto Oil Drain Chance")
	@Comment("Chance for a Tekto oil block to become empty on suck")
	public static double tektoOilDrainChance = 0.05D;

	@Name("Tekto Bedrock Oil Spawn")
	@Comment("Spawns a Tekto bedrock oil bubble every nTH chunk (on Tekto)")
	public static int tektoBedrockOilSpawn = 200;

	@Name("Tekto Bedrock Oil Per Deposit")
	@Comment("Oil extracted per bedrock oil block suck")
	public static int tektoBedrockOilPerDeposit = 100;

	@Name("Tekto Bedrock Gas Per Deposit Min")
	@Comment("Minimum natural gas extracted per bedrock oil block suck")
	public static int tektoBedrockGasPerDepositMin = 10;

	@Name("Tekto Bedrock Gas Per Deposit Max")
	@Comment("Maximum natural gas extracted per bedrock oil block suck")
	public static int tektoBedrockGasPerDepositMax = 50;

	@Name("Eve Gas Per Deposit")
	@Comment("Natural Gas extracted per Eve gas block suck")
	public static int eveGasPerDeposit = 500;

	@Name("Eve Pet Per Deposit Min")
	@Comment("Minimum petroleum gas extracted per Eve oil block suck")
	public static int evePetPerDepositMin = 20;

	@Name("Eve Pet Per Deposit Max")
	@Comment("Maximum petroleum gas extracted per Eve oil block suck")
	public static int evePetPerDepositMax = 100;

	@Name("Eve Gas Drain Chance")
	@Comment("Chance for an Eve gas block to become empty on suck")
	public static double eveGasDrainChance = 0.05D;

	@Name("Mun Brine Per Deposit")
	@Comment("Brine extracted per Mun brine block suck")
	public static int munBrinePerDeposit = 300;

	@Name("Mun Brine Drain Chance")
	@Comment("Chance for an Mun brine block to become empty on suck")
	public static double munBrineDrainChance = 0.05D;

	@Name("Minmus Brine Per Deposit")
	@Comment("Brine extracted per Minmus brine block suck")
	public static int minmusBrinePerDeposit = 300;

	@Name("Minmus Brine Drain Chance")
	@Comment("Chance for an Minmus brine block to become empty on suck")
	public static double minmusBrineDrainChance = 0.05D;

	@Name("Ike Brine Per Deposit")
	@Comment("Brine extracted per Ike brine block suck")
	public static int ikeBrinePerDeposit = 300;

	@Name("Ike Brine Drain Chance")
	@Comment("Chance for an Ike brine block to become empty on suck")
	public static double ikeBrineDrainChance = 0.05D;

	@Name("Bedrock Iron Spawn")
	@Comment("Spawn weight for iron bedrock ore")
	public static int bedrockIronSpawn = 100;

	@Name("Bedrock Copper Spawn")
	@Comment("Spawn weight for copper bedrock ore")
	public static int bedrockCopperSpawn = 200;

	@Name("Bedrock Borax Spawn")
	@Comment("Spawn weight for borax bedrock ore")
	public static int bedrockBoraxSpawn = 50;

	@Name("Bedrock Chlorocalcite Spawn")
	@Comment("Spawn weight for chlorocalcite bedrock ore")
	public static int bedrockChlorocalciteSpawn = 35;

	@Name("Bedrock Asbestos Spawn")
	@Comment("Spawn weight for asbestos bedrock ore")
	public static int bedrockAsbestosSpawn = 50;

	@Name("Bedrock Niobium Spawn")
	@Comment("Spawn weight for niobium bedrock ore")
	public static int bedrockNiobiumSpawn = 50;

	@Name("Bedrock Neodymium Spawn")
	@Comment("Spawn weight for neodymium bedrock ore")
	public static int bedrockNeodymiumSpawn = 50;

	@Name("Bedrock Titanium Spawn")
	@Comment("Spawn weight for titanium bedrock ore")
	public static int bedrockTitaniumSpawn = 100;

	@Name("Bedrock Tungsten Spawn")
	@Comment("Spawn weight for tungsten bedrock ore")
	public static int bedrockTungstenSpawn = 100;

	@Name("Bedrock Gold Spawn")
	@Comment("Spawn weight for gold bedrock ore")
	public static int bedrockGoldSpawn = 50;

	@Name("Bedrock Uranium Spawn")
	@Comment("Spawn weight for uranium bedrock ore")
	public static int bedrockUraniumSpawn = 35;

	@Name("Bedrock Thorium Spawn")
	@Comment("Spawn weight for thorium bedrock ore")
	public static int bedrockThoriumSpawn = 50;

	@Name("Bedrock Coal Spawn")
	@Comment("Spawn weight for coal bedrock ore")
	public static int bedrockCoalSpawn = 200;

	@Name("Bedrock Niter Spawn")
	@Comment("Spawn weight for niter bedrock ore")
	public static int bedrockNiterSpawn = 50;

	@Name("Bedrock Fluorite Spawn")
	@Comment("Spawn weight for fluorite bedrock ore")
	public static int bedrockFluoriteSpawn = 50;

	@Name("Bedrock Redstone Spawn")
	@Comment("Spawn weight for redstone bedrock ore")
	public static int bedrockRedstoneSpawn = 50;

	@Name("Bedrock Bismuth Spawn")
	@Comment("Spawns a bedrock bismuth deposit every nTH chunk")
	public static int bedrockBismuthSpawn = 400;

	@Name("Bedrock Cadmium Spawn")
	@Comment("Spawns a bedrock cadmium deposit every nTH chunk")
	public static int bedrockCadmiumSpawn = 400;

	@Name("Bedrock Rare Earth Spawn")
	@Comment("Spawn weight for rare earth bedrock ore")
	public static int bedrockRareEarthSpawn = 50;

	@Name("Bedrock Bauxite Spawn")
	@Comment("Spawn weight for bauxite bedrock ore")
	public static int bedrockBauxiteSpawn = 100;

	@Name("Bedrock Emerald Spawn")
	@Comment("Spawn weight for emerald bedrock ore")
	public static int bedrockEmeraldSpawn = 50;

	@Name("Bedrock Glowstone Spawn")
	@Comment("Spawn weight for glowstone bedrock ore")
	public static int bedrockGlowstoneSpawn = 100;

	@Name("Bedrock Phosphorus Spawn")
	@Comment("Spawn weight for phosphorus bedrock ore")
	public static int bedrockPhosphorusSpawn = 50;

	@Name("Bedrock Quartz Spawn")
	@Comment("Spawn weight for quartz bedrock ore")
	public static int bedrockQuartzSpawn = 100;

	@Name("Iron Cluster Spawn")
	@Comment("Amount of iron cluster veins per chunk")
	public static int ironClusterSpawn = 4;

	@Name("Titanium Cluster Spawn")
	@Comment("Amount of titanium cluster veins per chunk")
	public static int titaniumClusterSpawn = 2;

	@Name("Aluminium Cluster Spawn")
	@Comment("Amount of aluminium cluster veins per chunk")
	public static int aluminiumClusterSpawn = 3;

	@Name("Copper Cluster Spawn")
	@Comment("Amount of copper cluster veins per chunk")
	public static int copperClusterSpawn = 4;

	@Name("Alexandrite Spawn")
	@Comment("Spawns an alexandrite vein every nTH chunk")
	public static int alexandriteSpawn = 100;

	@Name("Limestone Spawn")
	@Comment("Amount of limestone block veins per chunk")
	public static int limestoneSpawn = 1;

	@Name("Nether Uraniumu Spawn")
	@Comment("Amount of nether uranium per chunk")
	public static int netherUraniumuSpawn = 8;

	@Name("Nether Tungsten Spawn")
	@Comment("Amount of nether tungsten per chunk")
	public static int netherTungstenSpawn = 10;

	@Name("Nether Sulfur Spawn")
	@Comment("Amount of nether sulfur per chunk")
	public static int netherSulfurSpawn = 26;

	@Name("Nether Phosphorus Spawn")
	@Comment("Amount of nether phosphorus per chunk")
	public static int netherPhosphorusSpawn = 24;

	@Name("Nether Coal Spawn")
	@Comment("Amount of nether coal per chunk")
	public static int netherCoalSpawn = 8;

	@Name("Nether Plutonium Spawn")
	@Comment("Amount of nether plutonium per chunk, if enabled")
	public static int netherPlutoniumSpawn = 8;

	@Name("Nether Cobalt Spawn")
	@Comment("Amount of nether cobalt per chunk")
	public static int netherCobaltSpawn = 2;

	@Name("End Tikite Spawn")
	@Comment("Amount of end trixite per chunk")
	public static int endTikiteSpawn = 8;

	@Name("Enable Hematite")
	@Comment("Toggles hematite deposits")
	public static boolean enableHematite = true;

	@Name("Enable Malachite")
	@Comment("Toggles malachite deposits")
	public static boolean enableMalachite = true;

	@Name("Enable Bauxite")
	@Comment("Toggles bauxite deposits")
	public static boolean enableBauxite = true;

	@Name("Enable Sulfur Cave")
	@Comment("Toggles sulfur caves")
	public static boolean enableSulfurCave = true;

	@Name("Enable Asbestos Cave")
	@Comment("Toggles asbestos caves")
	public static boolean enableAsbestosCave = true;

	/* ---- surface and underground structures ---- */

	@Name("Antenna Structure")
	@Comment("Spawn antenna on every nTH chunk")
	public static int antennaStructure = 250;

	@Name("Atom Structure")
	@Comment("Spawn power plant on every nTH chunk")
	public static int atomStructure = 500;

	@Name("Dungeon Structure")
	@Comment("Spawn library dungeon on every nTH chunk")
	public static int dungeonStructure = 64;

	@Name("Satellite Structure")
	@Comment("Spawn satellite dish on every nTH chunk")
	public static int satelliteStructure = 500;

	@Name("Dud Structure")
	@Comment("Spawn dud on every nTH chunk")
	public static int dudStructure = 500;

	@Name("Spaceship Structure")
	@Comment("Spawn spaceship on every nTH chunk")
	public static int spaceshipStructure = 1000;

	@Name("Barrel Structure")
	@Comment("Spawn waste tank on every nTH chunk")
	public static int barrelStructure = 5000;

	@Name("Geyser Chlorine")
	@Comment("Spawn poison geyser on every nTH chunk")
	public static int geyserChlorine = 3000;

	@Name("Geyser Vapor")
	@Comment("Spawn vapor geyser on every nTH chunk")
	public static int geyserVapor = 250;

	@Name("Capsule Structure")
	@Comment("Spawn landing capsule on every nTH chunk")
	public static int capsuleStructure = 100;

	@Name("Arctic Structure")
	@Comment("Spawn arctic code vault on every nTH chunk")
	public static int arcticStructure = 500;

	@Name("Jungle Structure")
	@Comment("Spawn jungle dungeon on every nTH chunk")
	public static int jungleStructure = 2000;

	@Name("Pyramid Structure")
	@Comment("Spawn pyramid on every nTH chunk")
	public static int pyramidStructure = 4000;

	@Name("Broadcaster")
	@Comment("Spawn corrupt broadcaster on every nTH chunk")
	public static int broadcaster = 5000;

	@Name("Minefreq")
	@Comment("Spawn AP landmine on every nTH chunk")
	public static int minefreq = 64;

	@Name("Radfreq")
	@Comment("Spawn radiation hotspot on every nTH chunk")
	public static int radfreq = 5000;

	@Name("Vaultfreq")
	@Comment("Spawn locked safe on every nTH chunk")
	public static int vaultfreq = 2500;

	/* ---- meteorites ---- */

	@Name("Enable Meteor Strikes")
	@Comment("Toggles the spawning of meteors")
	public static boolean enableMeteorStrikes = true;

	@Name("Enable Meteor Showers")
	@Comment("Toggles meteor showers, which start with a 1% chance for every spawned meteor")
	public static boolean enableMeteorShowers = true;

	@Name("Enable Meteor Tails")
	@Comment("Toggles the particle effect created by falling meteors")
	public static boolean enableMeteorTails = true;

	@Name("Enable Special Meteors")
	@Comment("Toggles rare, special meteor types with different impact effects")
	public static boolean enableSpecialMeteors = true;

	@Name("Meteor Strike Chance")
	@Comment("The probability of a meteor spawning (an average of once every nTH ticks)")
	public static int meteorStrikeChance = 20 * 60 * 60 * 5;

	@Name("Meteor Shower Chance")
	@Comment("The probability of a meteor spawning during meteor shower (an average of once every nTH ticks)")
	public static int meteorShowerChance = 20 * 60 * 15;

	@Name("Meteor Shower Duration")
	@Comment("Max duration of meteor shower in ticks")
	public static int meteorShowerDuration = 20 * 60 * 30;

	/* ---- crater biomes ---- */

	@Name("Enable Crater Biomes")
	@Comment("Enables the biome change caused by nuclear explosions")
	public static boolean enableCraterBiomes = true;

	@Name("Crater Biome Rad")
	@Comment("RAD/s for the crater biome")
	public static float craterBiomeRad = 5F;

	@Name("Crater Biome Inner Rad")
	@Comment("RAD/s for the inner crater biome")
	public static float craterBiomeInnerRad = 25F;

	@Name("Crater Biome Outer Rad")
	@Comment("RAD/s for the outer crater biome")
	public static float craterBiomeOuterRad = 0.5F;

	@Name("Crater Biome Water Mult")
	@Comment("Multiplier for RAD/s in crater biomes when in water")
	public static float craterBiomeWaterMult = 5F;

	/**
	 * The guard at the end of the old loadFromConfig, kept as-is. These are all machine divisors; a non-positive value makes the next round's
	 * nextInt throw outright, so this isn't cosmetic. @Config has no post-load hook, and 1.12.2's @RangeInt only feeds the GUI slider
	 * (ConfigElement reads min/max; Property.getInt doesn't clamp), so the gate stays in code, called by ConfigManager after syncing.
	 */
	public static void clampRandomizers() {

		antennaStructure = CommonConfig.setDefZero(antennaStructure, 1000);
		atomStructure = CommonConfig.setDefZero(atomStructure, 1000);
		dungeonStructure = CommonConfig.setDefZero(dungeonStructure, 1000);
		satelliteStructure = CommonConfig.setDefZero(satelliteStructure, 1000);
		dudStructure = CommonConfig.setDefZero(dudStructure, 1000);
		spaceshipStructure = CommonConfig.setDefZero(spaceshipStructure, 1000);
		barrelStructure = CommonConfig.setDefZero(barrelStructure, 1000);
		geyserChlorine = CommonConfig.setDefZero(geyserChlorine, 1000);
		geyserVapor = CommonConfig.setDefZero(geyserVapor, 1000);
		broadcaster = CommonConfig.setDefZero(broadcaster, 1000);
		minefreq = CommonConfig.setDefZero(minefreq, 1000);
		radfreq = CommonConfig.setDefZero(radfreq, 1000);
		vaultfreq = CommonConfig.setDefZero(vaultfreq, 1000);
		jungleStructure = CommonConfig.setDefZero(jungleStructure, 1000);
		capsuleStructure = CommonConfig.setDefZero(capsuleStructure, 100);
		arcticStructure = CommonConfig.setDefZero(arcticStructure, 500);
		meteorStrikeChance = CommonConfig.setDef(meteorStrikeChance, 1000);
		meteorShowerChance = CommonConfig.setDef(meteorShowerChance, 1000);
	}
}
