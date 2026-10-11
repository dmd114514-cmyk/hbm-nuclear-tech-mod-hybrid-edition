package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;

/** Mob-related parameters: pollution buffs, swarm harassment strength and friends. */
@Config(modid = RefStrings.MODID, name = "hbm-mob")
public class MobConfig {

	@Name("Pollution Multiplier")
	@Comment("Overall multiplier for pollution's effects on creatures; 0 disables pollution buffs")
	@RangeDouble(min = 0D, max = 100D)
	public static float pollutionMult = 1F;

	@Name("Rampant Glyphid Guidance")
	@Comment("Makes the raging swarm advance toward pollution sources in an organized way")
	public static boolean rampantGlyphidGuidance = true;

	@Name("Rampant Natural Scout Spawn")
	@Comment("Allows swarm scouts to spawn naturally")
	public static boolean rampantNaturalScoutSpawn = true;

	@Name("Rampant Scout Spawn Chance")
	@Comment("Scout spawn weight; the higher, the more frequent")
	@RangeInt(min = 0, max = 10000)
	public static int rampantScoutSpawnChance = 10;

	@Name("Rampant Scout Spawn Threshold")
	@Comment("Pollution threshold needed to trigger scout spawns")
	@RangeDouble(min = 0D, max = 100000D)
	public static float rampantScoutSpawnThresh = 50F;
}
