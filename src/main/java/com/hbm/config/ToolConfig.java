package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

/**
 * Tool capability switches (vein mining and the various abilities). Field names keep the original mod's spelling; tool classes read them by name.
 *
 * The whole family moved from Configuration to @Config. Defaults take the old read statements' def, not field initializers:
 * recursionDepth initializes to 500 but old configs ran 1000, so this file lands at 1000.
 * The ten ability* entries used the compact config.get(cat, key, def, comment).getBoolean(def) form in the old version; likewise folded into one-line declarations.
 */
@Config(modid = RefStrings.MODID, name = "hbm-tool")
public class ToolConfig {

	/* ---- vein mining and tool abilities ---- */

	@Name("Recursion Depth")
	@Comment("Limits veinminer's recursive function. Usually not an issue, unless you're using bukkit which is especially sensitive for some reason.")
	public static int recursionDepth = 1000;

	@Name("Recursive Stone")
	@Comment("Determines whether veinminer can break stone")
	public static boolean recursiveStone = false;

	@Name("Recursive Netherrack")
	@Comment("Determines whether veinminer can break netherrack")
	public static boolean recursiveNetherrack = false;

	@Name("Ability Hammer")
	@Comment("Allows AoE ability")
	public static boolean abilityHammer = true;

	@Name("Ability Vein")
	@Comment("Allows veinminer ability")
	public static boolean abilityVein = true;

	@Name("Ability Luck")
	@Comment("Allow luck (fortune) ability")
	public static boolean abilityLuck = true;

	@Name("Ability Silk")
	@Comment("Allow silk touch ability")
	public static boolean abilitySilk = true;

	@Name("Ability Furnace")
	@Comment("Allow auto-smelter ability")
	public static boolean abilityFurnace = true;

	@Name("Ability Shredder")
	@Comment("Allow auto-shredder ability")
	public static boolean abilityShredder = true;

	@Name("Ability Centrifuge")
	@Comment("Allow auto-centrifuge ability")
	public static boolean abilityCentrifuge = true;

	@Name("Ability Crystallizer")
	@Comment("Allow auto-crystallizer ability")
	public static boolean abilityCrystallizer = true;

	@Name("Ability Mercury")
	@Comment("Allow mercury touch ability (digging redstone gives mercury)")
	public static boolean abilityMercury = true;

	@Name("Ability Explosion")
	@Comment("Allow explosion ability")
	public static boolean abilityExplosion = true;
}
