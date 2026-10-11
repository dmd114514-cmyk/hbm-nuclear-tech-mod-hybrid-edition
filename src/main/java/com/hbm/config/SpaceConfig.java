package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

/**
 * Planet dimensions and spaceflight switches. Field names keep the original mod's spelling; dimension registration and planet gen read them by name.
 *
 * The whole family moved from Configuration to @Config. The old version carried 49 entries here; this one lands 17:
 * <br><br>
 * One: all 31 *Biome entries deleted wholesale, plus crashOnBiomeConflict (it only served those IDs). Same reasoning as
 * WorldConfig's three craterBiome* entries: 1.12.2 addresses biomes by registry name with IDs assigned by Forge,
 * there are no numeric biome IDs to configure, and a duplicate-name registration is simply an error. The old block — counting down from 126 to dodge
 * other mods' favorite IDs, the registeredIds collision table, BiomeCollisionException — all existed to service ID crashes; with the mechanism gone, so are their readers.
 * <b>Biome identity is expressed by registry name</b>; packs wanting different biomes should change the registration side, not stuff IDs into this file.
 * <br><br>
 * Two: the 11 *Dimension entries land as-is: dimensions still register as int in this version, they're real knobs. The old comment said "anything above four million, help yourself",
 * and 1.12.2 is still int, so the 4130xx numbering carries on.
 * <br><br>
 * Three: maxStationDistance's old 1024..MAX was a Property boundary feeding only the GUI slider, never clamped at load,
 */
@Config(modid = RefStrings.MODID, name = "hbm-space")
public class SpaceConfig {

	/* ---- planet dimensions ---- */

	@Name("Moon Dimension")
	@Comment("Mun dimension ID")
	public static int moonDimension = 413_015;

	@Name("Duna Dimension")
	@Comment("Duna dimension ID")
	public static int dunaDimension = 413_016;

	@Name("Ike Dimension")
	@Comment("Ike dimension ID")
	public static int ikeDimension = 413_017;

	@Name("Eve Dimension")
	@Comment("Eve dimension ID")
	public static int eveDimension = 413_018;

	@Name("Dres Dimension")
	@Comment("Dres dimension ID")
	public static int dresDimension = 413_019;

	@Name("Moho Dimension")
	@Comment("Moho dimension ID")
	public static int mohoDimension = 413_020;

	@Name("Minmus Dimension")
	@Comment("Minmus dimension ID")
	public static int minmusDimension = 413_021;

	@Name("Laythe Dimension")
	@Comment("Laythe dimension ID")
	public static int laytheDimension = 413_022;

	@Name("Orbit Dimension")
	@Comment("Orbital dimension ID")
	public static int orbitDimension = 413_023;

	@Name("Tekto Dimension")
	@Comment("Tekto dimension ID")
	public static int tektoDimension = 413_024;

	@Name("Thatmo Dimension")
	@Comment("Thatmo dimension ID")
	public static int thatmoDimension = 413_025;

	@Name("Allow Nether Portals")
	@Comment("Should Nether portals function on other celestial bodies?")
	public static boolean allowNetherPortals = false;

	/* ---- planet generation ---- */

	@Name("Enable Volcano Gen")
	@Comment("Should volcanoes be active when spawning, disabling will prevent natural volcanoes from spewing lava and growing")
	public static boolean enableVolcanoGen = true;

	@Name("Max Probe Distance")
	@Comment("How far from the center of the dimension can probes generate landing coordinates")
	public static int maxProbeDistance = 32_000;

	@Name("Max Station Distance")
	@Comment("How far from the center of the dimension can orbital stations be generated")
	@RangeInt(min = 1024)
	public static int maxStationDistance = 32_000;

	/* ---- spaceflight gameplay switches ---- */

	@Name("Show Ore Locations")
	@Comment("Should ores indicate which planets they can be found on.")
	public static boolean showOreLocations = true;

	@Name("Combat Pod Despawn")
	@Comment("wether combat pods should despawn after a certian amount of time.")
	public static boolean combatPodDespawn = false;
}
