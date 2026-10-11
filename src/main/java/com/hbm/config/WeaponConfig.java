package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

/**
 * Weapon and dropped-item behavior switches. Field names keep the original mod's spelling; ammo and held-item logic read them by name.
 *
 * The whole family moved from Configuration to @Config: ciwsHitrate was a three-part Property in the old version, the rest createConfigBool;
 * all 7 initial values match the old defs, not a single number changed.
 */
@Config(modid = RefStrings.MODID, name = "hbm-weapon")
public class WeaponConfig {

	/* ---- CIWS ---- */

	@Name("Ciws Hitrate")
	@Comment("Additional modifier for CIWS accuracy")
	public static int ciwsHitrate = 50;

	/* ---- dropped items ---- */

	@Name("Drop Cell")
	@Comment("Whether antimatter cells should explode when dropped")
	public static boolean dropCell = true;

	@Name("Drop Sing")
	@Comment("Whether singularities and black holes should spawn when dropped")
	public static boolean dropSing = true;

	@Name("Drop Star")
	@Comment("Whether rigged star blaster cells should explode when dropped")
	public static boolean dropStar = true;

	@Name("Drop Crys")
	@Comment("Whether xen crystals should move blocks when dropped")
	public static boolean dropCrys = true;

	@Name("Drop Dead")
	@Comment("Whether dead man's explosives should explode when dropped")
	public static boolean dropDead = true;

	/* ---- held-item animations ---- */

	@Name("Linear Animations")
	@Comment("Should heavily stylised weapon animations be replaced with more conventional ones?")
	public static boolean linearAnimations = false;
}
