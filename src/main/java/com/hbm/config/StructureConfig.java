package com.hbm.config;

import java.util.Locale;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

/**
 * Ruins, crashes and structure generation weights. Field names keep the original mod's spelling; the structure-gen side reads them by name.
 *
 * The whole family moved from Configuration to @Config; defaults always take the old read statements' def. They disagree on two entries:
 * structureMaxChunks(12/16)、lighthouseSpawnWeight(4/1)、dishSpawnWeight(20/10)、radioSpawnWeight(30/25)、
 * plainsNullWeight(4/20), oceanNullWeight(15/35) — what old config files actually ran was the def value, so this file lands on it.
 *
 * enableStructures is the only non-direct mapping: the old version read a "true|false|flag" string and folded it into 1/0/2 with parseStructureFlag.
 * Here the string goes to @Config (new field structureFlag, same allowed values), while the int still exposes the original name enableStructures to callers,
 * folding plus the old two guards collected into sanitize(), called by ConfigManager after syncing — the call surface untouched.
 */
@Config(modid = RefStrings.MODID, name = "hbm-structure")
public class StructureConfig {

	/** The three-state value the old version folded out of the "true|false|flag" string via parseStructureFlag; that's what callers read */
	@Name("Enable Structures")
	@Comment("Flag for whether modern NTM structures will spawn. Valid values are true|false|flag - flag will respect the \"Generate Structures\" world flag.")
	public static String structureFlag = "flag";

	/** The old parseStructureFlag folded the string above into 1/0/2; callers always read this int */
	@Config.Ignore
	public static int enableStructures = 2;

	/* ---- structure master switch and weights ---- */

	@Name("Structure Min Chunks")
	@Comment("Minimum non-zero distance between structures in chunks (Settings lower than 8 may be problematic).")
	public static int structureMinChunks = 4;

	@Name("Structure Max Chunks")
	@Comment("Maximum non-zero distance between structures in chunks.")
	public static int structureMaxChunks = 16;

	@Name("Loot Amount Factor")
	@Comment("General factor for loot spawns. Applies to spawned IInventories, not loot blocks.")
	public static double lootAmountFactor = 1D;

	@Name("Debug Structures")
	@Comment("If enabled, special structure blocks like jigsaw blocks will not be transformed after generating")
	public static boolean debugStructures = false;

	@Name("Enable Ruins")
	@Comment("Toggle for all ruin structures (A through J)")
	public static boolean enableRuins = true;

	@Name("Enable Ocean Structures")
	@Comment("Toggle for ocean structures. (Aircraft carrier, oil rig, lighthouse.)")
	public static boolean enableOceanStructures = true;

	@Name("Ruins A Spawn Weight")
	@Comment("Spawn weight for ruin A structure.")
	public static int ruinsASpawnWeight = 10;

	@Name("Ruins B Spawn Weight")
	@Comment("Spawn weight for ruin B structure.")
	public static int ruinsBSpawnWeight = 12;

	@Name("Ruins C Spawn Weight")
	@Comment("Spawn weight for ruin C structure.")
	public static int ruinsCSpawnWeight = 12;

	@Name("Ruins D Spawn Weight")
	@Comment("Spawn weight for ruin D structure.")
	public static int ruinsDSpawnWeight = 12;

	@Name("Ruins E Spawn Weight")
	@Comment("Spawn weight for ruin E structure.")
	public static int ruinsESpawnWeight = 12;

	@Name("Ruins F Spawn Weight")
	@Comment("Spawn weight for ruin F structure.")
	public static int ruinsFSpawnWeight = 12;

	@Name("Ruins G Spawn Weight")
	@Comment("Spawn weight for ruin G structure.")
	public static int ruinsGSpawnWeight = 12;

	@Name("Ruins H Spawn Weight")
	@Comment("Spawn weight for ruin H structure.")
	public static int ruinsHSpawnWeight = 12;

	@Name("Ruins I Spawn Weight")
	@Comment("Spawn weight for ruin I structure.")
	public static int ruinsISpawnWeight = 12;

	@Name("Ruins J Spawn Weight")
	@Comment("Spawn weight for ruin J structure.")
	public static int ruinsJSpawnWeight = 12;

	@Name("Plane1 Spawn Weight")
	@Comment("Spawn weight for crashed plane 1 structure.")
	public static int plane1SpawnWeight = 25;

	@Name("Plane2 Spawn Weight")
	@Comment("Spawn weight for crashed plane 2 structure.")
	public static int plane2SpawnWeight = 25;

	@Name("Desert Shack1 Spawn Weight")
	@Comment("Spawn weight for desert shack 1 structure.")
	public static int desertShack1SpawnWeight = 18;

	@Name("Desert Shack2 Spawn Weight")
	@Comment("Spawn weight for desert shack 2 structure.")
	public static int desertShack2SpawnWeight = 20;

	@Name("Desert Shack3 Spawn Weight")
	@Comment("Spawn weight for desert shack 3 structure.")
	public static int desertShack3SpawnWeight = 22;

	@Name("Laboratory Spawn Weight")
	@Comment("Spawn weight for laboratory structure/")
	public static int laboratorySpawnWeight = 20;

	@Name("Lighthouse Spawn Weight")
	@Comment("Spawn weight for lighthouse structure.")
	public static int lighthouseSpawnWeight = 1;

	@Name("Oil Rig Spawn Weight")
	@Comment("Spawn weight for oil rig structure.")
	public static int oilRigSpawnWeight = 5;

	@Name("Broadcasting Tower Spawn Weight")
	@Comment("Spawn weight for broadcasting tower structure.")
	public static int broadcastingTowerSpawnWeight = 25;

	@Name("Water Pump Spawn Weight")
	@Comment("Spawn weight for water pump structure.")
	public static int waterPumpSpawnWeight = 15;

	@Name("Dead Dish Small Spawn Weight")
	@Comment("Spawn weight for dead dish small structure.")
	public static int deadDishSmallSpawnWeight = 15;

	@Name("Beached Patrol Spawn Weight")
	@Comment("Spawn weight for beached patrol structure.")
	public static int beachedPatrolSpawnWeight = 15;

	@Name("Vertibird Spawn Weight")
	@Comment("Spawn weight for vertibird structure.")
	public static int vertibirdSpawnWeight = 6;

	@Name("Vertibird Crashed Spawn Weight")
	@Comment("Spawn weight for crashed vertibird structure.")
	public static int vertibirdCrashedSpawnWeight = 10;

	@Name("Factory Spawn Weight")
	@Comment("Spawn weight for factory structure.")
	public static int factorySpawnWeight = 40;

	@Name("Radio Spawn Weight")
	@Comment("Spawn weight for radio structure.")
	public static int radioSpawnWeight = 25;

	@Name("Forest Chem Spawn Weight")
	@Comment("Spawn weight for forest chemical plant structure.")
	public static int forestChemSpawnWeight = 30;

	@Name("Forest Post Spawn Weight")
	@Comment("Spawn weight for forest post structure.")
	public static int forestPostSpawnWeight = 30;

	@Name("Tower Base Spawn Weight")
	@Comment("Spawn weight for tower base.")
	public static int towerBaseSpawnWeight = 30;

	@Name("Spire Spawn Weight")
	@Comment("Spawn weight for spire structure.")
	public static int spireSpawnWeight = 2;

	@Name("Crane Spawn Weight")
	@Comment("Spawn weight for crane structure.")
	public static int craneSpawnWeight = 20;

	@Name("Bunker Spawn Weight")
	@Comment("Spawn weight for bunker structure.")
	public static int bunkerSpawnWeight = 6;

	@Name("Dish Spawn Weight")
	@Comment("Spawn weight for dish structures.")
	public static int dishSpawnWeight = 10;

	@Name("Features Spawn Weight")
	@Comment("Spawn weight for misc structures (ex. Houses, offices.)")
	public static int featuresSpawnWeight = 50;

	@Name("Aircraft Carrier Spawn Weight")
	@Comment("Spawn weight for aircraft carrier structure.")
	public static int aircraftCarrierSpawnWeight = 3;

	@Name("Plains Null Weight")
	@Comment("Null spawn weight for plains biome")
	public static int plainsNullWeight = 20;

	@Name("Ocean Null Weight")
	@Comment("Null spawn weight for ocean biomes")
	public static int oceanNullWeight = 35;

	/**
	 * The three things the old loadFromConfig did at its end, collected into this one method, called by ConfigManager after syncing:
	 * fold "true|false|flag" into the int callers read, guard both step ranges' lower bounds, and catch self-contradictory min>max configs.
	 * Done in code rather than annotations because @Config has no post-load hook, and 1.12.2's @RangeInt only feeds the GUI slider
	 * (ConfigElement reads min/max; Property.getInt doesn't clamp).
	 */
	public static void sanitize() {

		enableStructures = CommonConfig.parseStructureFlag(structureFlag);

		structureMinChunks = CommonConfig.setDef(structureMinChunks, 4);
		structureMaxChunks = CommonConfig.setDef(structureMaxChunks, 12);

		if(structureMinChunks > structureMaxChunks) {
			MainRegistry.logger.error("Fatal error config: Minimum value has been set higher than the maximum value!");
			MainRegistry.logger.error(String.format(Locale.US, "Errored values will default back to %1$d and %2$d respectively, PLEASE REVIEW CONFIGURATION DESCRIPTION BEFORE MEDDLING WITH VALUES!", 8, 24));
			structureMinChunks = 8;
			structureMaxChunks = 24;
		}
	}
}
