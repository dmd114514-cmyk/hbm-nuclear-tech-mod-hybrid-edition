package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;

/**
 * Machine-side tunables. Only the two RTG entries for now; machine-wide switches go here from now on,
 * not into GeneralConfig — that one is mod-global difficulty and worldgen.
 */
@Config(modid = RefStrings.MODID, name = "hbm-machine", category = "machine")
public class MachineConfig {

	@Name("Scale RTG Power")
	@Comment("RTG/betavoltaic fuel output falls along with the depletion")
	public static boolean scaleRTGPower = false;

	@Name("RTGs Decay")
	@Comment("Whether RTG/betavoltaic fuel depletes at all; off means it never does")
	public static boolean doRTGsDecay = true;
}
