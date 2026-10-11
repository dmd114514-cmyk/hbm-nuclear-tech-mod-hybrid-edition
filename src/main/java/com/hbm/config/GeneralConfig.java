package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

/**
 * General config. The old Configuration force-sorted with numbered keys like "0.01_xxx";
 * @Config relies on field order instead, so the keys are finally readable.
 */
@Config(modid = RefStrings.MODID, name = "hbm-general")
public class GeneralConfig {

	@Name("Debug Mode")
	@Comment("Opens a pile of logs only a developer can parse; leave it off otherwise")
	public static boolean enableDebugMode = false;

	@Name("Radiation")
	@Comment("Radiation system master switch; off, and every radiation source becomes a harmless prop")
	public static boolean enableRad = true;

	@Name("Plutonium Ore")
	@Comment("Whether plutonium ore generates")
	public static boolean enablePlutoniumOre = false;

	@Name("Mycelium")
	@Comment("Whether mycelium spreads through polluted zones")
	public static boolean enableMycelium = false;

	@Name("Dungeons")
	@Comment("Dungeon spawn density: 0 = off, 1 = rare, 2 = default, 3 = common")
	@RangeInt(min = 0, max = 3)
	public static int enableDungeons = 2;

	@Name("Metal Ores")
	@Comment("Whether metal veins generate in the overworld")
	public static boolean enableMDOres = true;

	@Name("Mines")
	@Comment("Whether minefields generate")
	public static boolean enableMines = true;

	@Name("Vaults")
	@Comment("Whether underground bunkers generate")
	public static boolean enableVaults = true;

	@Name("Cataclysm")
	@Comment("Cataclysm mode; on, and the world becomes miserable")
	public static boolean enableCataclysm = false;

	@Name("Extended Logging")
	@Comment("Extra logging for explosions, radiation and the like; for troubleshooting")
	public static boolean enableExtendedLogging = false;

	@Name("Guns")
	@Comment("Gun system master switch")
	public static boolean enableGuns = true;

	@Name("Expensive Mode")
	@Comment("Expensive mode: doubles the material cost of recipes; made for hardcore packs")
	public static boolean enableExpensiveMode = false;

	@Name("528 Mode")
	@Comment("528 mode: a high-difficulty ruleset closer to the original mod author's settings")
	public static boolean enable528 = false;

	@Name("528 - ReaSim Boilers")
	@Comment("In 528 mode, enables more realistic boiler simulation")
	public static boolean enable528ReasimBoilers = true;

	@Name("528 - Coltan Deposit")
	@Comment("In 528 mode, generates glue tin deposits: one random spot in the whole world spawns a mass of glue tin")
	public static boolean enable528ColtanDeposit = true;

	@Name("528 - Coltan Random Spawn")
	@Comment("In 528 mode, glue tin ore also scatters randomly like normal veins; unlike the tier above, this one doesn't concentrate in a single spot")
	public static boolean enable528ColtanSpawn = false;

	@Name("528 - Bosnia Simulator")
	@Comment("In 528 mode, anti-tank mines spawn all over the world")
	public static boolean enable528BosniaSimulator = true;

	@Name("528 - Nether Burn")
	@Comment("In 528 mode, players entering the Nether catch fire")
	public static boolean enable528NetherBurn = true;

	@Name("528 - Pressurized Recipes")
	@Comment("In 528 mode, some chemical plant recipes require pressurized input fluids")
	public static boolean enable528PressurizedRecipes = true;

	@Name("528 - Explosive Energistics")
	@Comment("In 528 mode, wrecks that AE2 stuff (crafting storage will blow up)")
	public static boolean enable528ExplosiveEnergistics = true;

	@Name("528 - Machine Gravity")
	@Comment("In 528 mode, machines are affected by gravity too")
	public static boolean enable528MachineGravity = true;

	@Name("Threaded Atmospheres")
	@Comment("Runs atmosphere/pressure math on a separate thread; a lifesaver when the server hosts many machines")
	public static boolean enableThreadedAtmospheres = true;

	@Name("Fluid Container Compat")
	@Comment("Allows converting between HBM fluid containers and other mods' generic fluid containers")
	public static boolean enableFluidContainerCompat = true;

	@Name("Guide Book")
	@Comment("Whether the mod manual is on")
	public static boolean enableGuideBook = true;

	@Name("Silent Comp Stack Errors")
	@Comment("Silences the log spam from unregistered items during ComparableStack comparisons")
	public static boolean enableSilentCompStackErrors = false;

	@Name("Server Recipe Sync")
	@Comment("On dedicated servers, server recipes override local ones, keeping modded packs consistent on both ends")
	public static boolean enableServerRecipeSync = false;

	/** Convenience reader for expensive mode, so call sites don't all write if(GeneralConfig.enableExpensiveMode) */
	public static boolean trueExp() {
		return enableExpensiveMode;
	}

	/** Same for 528 mode */
	public static boolean true528() {
		return enable528;
	}

	@Name("LBSM Mode")
	@Comment("Less-nonsense mode master switch; forced off when 528 is on (see trueLBSM)")
	public static boolean enableLBSM = false;

	@Name("LBSM Full Schrab")
	@Comment("When on, swaps the converter's output from scharanium ingots to full schrabidium ingots")
	public static boolean enableLBSMFullSchrab = true;

	@Name("LBSM Shorter Decay")
	@Comment("Massively speeds up the decay of the stuff inside nuke waste drums: 60x faster than 528 mode, 5-12x faster than default")
	public static boolean enableLBSMShorterDecay = true;

	@Name("LBSM Simple Armor Recipes")
	@Comment("Simplifies the starmetal/uranium-233 class of armor recipes")
	public static boolean enableLBSMSimpleArmorRecipes = true;

	@Name("LBSM Simple Tool Recipes")
	@Comment("Simplifies the starmetal/uranium-233 class of tool recipes")
	public static boolean enableLBSMSimpleToolRecipes = true;

	@Name("LBSM Simple Alloy")
	@Comment("Adds a few extra blast furnace recipes, making some things cheaper")
	public static boolean enableLBSMSimpleAlloy = true;

	@Name("LBSM Simple Chemistry")
	@Comment("Simplifies some chemical plant recipes")
	public static boolean enableLBSMSimpleChemsitry = true;

	@Name("LBSM Simple Centrifuge")
	@Comment("Boosts centrifuge output, making rare materials easier to get")
	public static boolean enableLBSMSimpleCentrifuge = true;

	@Name("LBSM Unlock Anvil")
	@Comment("Anvil recipes all unlock from tier one")
	public static boolean enableLBSMUnlockAnvil = true;

	@Name("LBSM Simple Crafting")
	@Comment("Adds simple recipes for the things that couldn't be crafted or cost more materials; uranium ore also becomes washable")
	public static boolean enableLBSMSimpleCrafting = true;

	@Name("LBSM Simple Medicine Recipes")
	@Comment("Makes some medicine recipes (the bismuth kind) much cheaper")
	public static boolean enableLBSMSimpleMedicineRecipes = true;

	@Name("LBSM Safe Crates")
	@Comment("Keeps crates from being irradiated into radioactive items")
	public static boolean enableLBSMSafeCrates = true;

	@Name("LBSM Safe ME Drives")
	@Comment("Keeps ME drives and portable batteries from being irradiated into radioactive items")
	public static boolean enableLBSMSafeMEDrives = true;

	@Name("LBSM Neutron Decon")
	@Comment("The player purifier can cleanse items made radioactive by neutron radiation")
	public static boolean enableLBSMNeutronDecon = true;

	@Name("LBSM Schrab Ore Rate")
	@Comment("How many uranium ores on average blast out one uranium-233 ore; the default tier uses 100, this is the LBSM tier value")
	public static int schrabRate = 20;

	/**
	 * LBSM's effective value.
	 *
	 * 528 and LBSM are two difficulty rulesets that contradict each other; the original mod handled it by rewriting
	 * enableLBSM to false at config load — which silently mangled what the player wrote in the file.
	 * The raw toggle stays here and the check happens at read time, so the config file and the effective value stop polluting each other.
	 */
	public static boolean trueLBSM() {
		return enableLBSM && !enable528;
	}
}
