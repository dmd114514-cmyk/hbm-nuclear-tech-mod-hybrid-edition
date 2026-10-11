package com.hbm.compat.crafttweaker;

import com.hbm.main.MainRegistry;
import com.hbm.tweak.HbmTweaks;

import crafttweaker.annotations.ZenRegister;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * The script facade for `mods.hbm.tweaks`. The six methods **write the tweak table directly**, no queueing, no IAction —
 * scripts run during the registration phase, so deferring effect to postInit would leave pack authors staring at nothing.
 *
 * Without CraftTweaker installed this class is never loaded: it has no registration entry; CT itself scans `@ZenRegister`
 * to touch it, and no scan means it never loads, hence no NoClassDefFoundError to fear.
 * So this package is the mod's CT boundary; core code must never import it.
 */
@ZenRegister
@ZenClass("mods.hbm.tweaks")
public class TweaksCT {

	private TweaksCT() { }

	/** A multiplier on the default; repeat calls on the same entry don't stack */
	@ZenMethod
	public static void multiply(String domain, String target, String field, float factor) {
		HbmTweaks.multiply(domain, target, field, factor);
	}

	/** Absolute value, unit follows the field (damage = points, radius = blocks, hunger = food points) */
	@ZenMethod
	public static void set(String domain, String target, String field, float value) {
		HbmTweaks.set(domain, target, field, value);
	}

	/** The catch-all domain, equivalent to adding a default tweak for every target */
	@ZenMethod
	public static void global(String field, float value) {
		HbmTweaks.global(field, value);
	}

	/** Clears the whole override table */
	@ZenMethod
	public static void reset() {
		HbmTweaks.reset();
	}

	/** Clears one domain's overrides only */
	@ZenMethod
	public static void reset(String domain) {
		HbmTweaks.reset(domain);
	}

	/** Prints the current override table; entries untouched by subclass targets don't appear */
	@ZenMethod
	public static void dump() {
		MainRegistry.logger.info(HbmTweaks.dump());
	}
}
