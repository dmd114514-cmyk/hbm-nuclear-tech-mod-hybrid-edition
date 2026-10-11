package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

/**
 * Server-side tunables.
 *
 * The original mod built its own "JSON + ConfigWrapper + /ntmserver command" runtime-writable config,
 * but 1.12.2's @Config already does that job (readable, writable, hot-reloadable, in-game editor),
 * so this uses the official mechanism and the wrapper is gone entirely — call sites went from `ServerConfig.X.get()` to `ServerConfig.X`.
 * Field names keep the original ALL_CAPS style; too many places reference them.
 */
@Config(modid = RefStrings.MODID, name = "hbm-server", category = "server")
public class ServerConfig {

	@Name("Damage Compatibility Mode")
	@Comment("Compatibility mode: damage numbers follow the old algorithm, a safety net for old packs")
	public static boolean DAMAGE_COMPATIBILITY_MODE = false;

	@Name("Mine AP Damage")
	@Comment("AP mine damage")
	public static float MINE_AP_DAMAGE = 10F;

	@Name("Mine HE Damage")
	@Comment("HE mine damage")
	public static float MINE_HE_DAMAGE = 35F;

	@Name("Mine Shrapnel Damage")
	@Comment("frag mine damage")
	public static float MINE_SHRAP_DAMAGE = 7.5F;

	@Name("Mine Nuclear Damage")
	@Comment("nuke mine damage")
	public static float MINE_NUKE_DAMAGE = 100F;

	@Name("Mine Naval Damage")
	@Comment("sea mine damage")
	public static float MINE_NAVAL_DAMAGE = 60F;

	@Name("Taint Trails")
	@Comment("Whether polluted creatures leave contamination traces behind as they walk")
	public static boolean TAINT_TRAILS = false;

	@Name("Crate Open Held")
	@Comment("Right-click in hand opens the crate")
	public static boolean CRATE_OPEN_HELD = true;

	@Name("Crate Keep Contents")
	@Comment("Crates keep their contents when broken")
	public static boolean CRATE_KEEP_CONTENTS = true;

	@Name("Item Hazard Drop Tickrate")
	@Comment("Hazard settlement interval for dropped items (ticks); higher saves performance but dulls hazard detection")
	@RangeInt(min = 1, max = 100)
	public static int ITEM_HAZARD_DROP_TICKRATE = 2;

	@Name("Enable MKU")
	@Comment("Allows the MKU upgrade module")
	public static boolean ENABLE_MKU = true;

	@Name("Structure Debug")
	@Comment("Structure generation debug output")
	public static boolean STRUCTURE_DEBUG = false;

	@Name("Autocal Max Clock")
	@Comment("Max ticks the criticality calculator advances per pass")
	@RangeInt(min = 1, max = 200)
	public static int AUTOCAL_MAX_CLOCK = 20;
}
