package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

/**
 * Radiation, hazard and pollution parameters. Field names keep the original mod's spelling — plenty of outside code reads them by name,
 * and renaming means every call site silently falls back to defaults.
 */
@Config(modid = RefStrings.MODID, name = "hbm-radiation")
public class RadiationConfig {

	/* ---- radiation fog and atmosphere ---- */

	@Name("Fog Threshold")
	@Comment("Ambient radiation level in RAD at which fog starts")
	public static int fogRad = 100;

	@Name("Fog Chance Divisor")
	@Comment("Per-second fog chance is 1:n")
	public static int fogCh = 20;

	@Name("Nether Ambient")
	@Comment("Nether's background radiation, in RAD/s")
	public static double hellRad = 0.1;

	/* ---- world radiation alteration ---- */

	@Name("World Effects")
	@Comment("Whether high radiation may remodel the world (dead grass/mycelium etc.)")
	public static boolean worldRadEffects = true;

	// unread in the old version too = dead key, not wired here: no reader of RadiationConfig.worldRad anywhere in 1.7.10, none in this tree either
	@Name("Block Ops Per Tick")
	@Comment("Max block operations radiation may perform per tick; raising it is expensive")
	public static int worldRad = 10;

	// unread in the old version too = dead key, not wired here: same story — only worldRadEffects ever got read by the Manager, this key and the one above both sat idle
	@Name("World Effect Threshold")
	@Comment("Minimum RAD needed to trigger block remodeling")
	public static int worldRadThreshold = 20;

	@Name("Regrow Dead Dirt")
	@Comment("Whether dead grass and mycelium keep rotting into dirt")
	public static boolean cleanupDeadDirt = false;

	@Name("Celestial Multiplier")
	@Comment("Radiation multiplier on celestial bodies, 1 = 1 rad/s on the planet surface")
	public static double celestialRadMultiplier = 0.25;

	/* ---- system switches ---- */

	@Name("Player Contamination")
	@Comment("Whether player contamination and radiation sickness are on")
	public static boolean enableContamination = true;

	@Name("Chunk Radiation")
	@Comment("Whether the world radiation system is on (chunk radiation; some blocks add area damage of their own)")
	public static boolean enableChunkRads = true;

	// unread in the old version too = dead key, not wired here: the old PRISM implementation (ChunkRadiationHandlerPRISM) never read it, and the class isn't in this tree
	@Name("PRISM System")
	@Comment("Whether the new tri-axis resistance-aware PRISM radiation system is on")
	public static boolean enablePRISM = false;

	/* ---- per-hazard switches ---- */

	@Name("Disable Asbestos")
	public static boolean disableAsbestos = false;

	@Name("Disable Coal Dust")
	public static boolean disableCoal = false;

	@Name("Disable Hot")
	public static boolean disableHot = false;

	@Name("Disable Explosive")
	public static boolean disableExplosive = false;

	@Name("Disable Hydroactive")
	public static boolean disableHydro = false;

	@Name("Disable Blinding")
	public static boolean disableBlinding = false;

	// unread in the old version too = dead key, not wired here: every other disable* in the group has a hazard registration reading it; this key alone had no reader
	@Name("Disable Fibrosis")
	public static boolean disableFibrosis = false;

	@Name("Disable Neutron")
	public static boolean disableNeutron = true;

	/* ---- pollution ---- */

	@Name("Enable Pollution")
	@Comment("Master switch; off, and every pollution mechanism grinds to a halt")
	public static boolean enablePollution = true;

	@Name("Lead From Blocks")
	@Comment("Whether breaking blocks in heavy-metal pollution zones poisons you")
	public static boolean enableLeadFromBlocks = true;

	@Name("Lead Poisoning")
	@Comment("Whether standing in a heavy-metal pollution zone poisons you")
	public static boolean enableLeadPoisoning = true;

	@Name("Soot Fog")
	@Comment("Whether soot fog renders")
	public static boolean enableSootFog = true;

	@Name("Poison Areas")
	@Comment("Whether standing in a poisoned zone affects you")
	public static boolean enablePoison = true;

	@Name("Mob Buff Threshold")
	@Comment("Soot level at which naturally spawned creatures gain buffs")
	public static double buffMobThreshold = 15D;

	@Name("Soot Fog Threshold")
	@Comment("Soot level at which it becomes visible")
	public static double sootFogThreshold = 35D;

	@Name("Soot Fog Divisor")
	@Comment("Soot fog divisor; the higher, the more soot needed for the same density")
	public static double sootFogDivisor = 120D;

	// unread in the old version too = dead key, not wired here: the old sootFog* trio was read by client render events; nobody read this one
	@Name("Smokestack Soot Multiplier")
	@Comment("Chimney's multiplier on soot; below 1 means scrubbing")
	public static double smokeStackSootMult = 0.8;
}
