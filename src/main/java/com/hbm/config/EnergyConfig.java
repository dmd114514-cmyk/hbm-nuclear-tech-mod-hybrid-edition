package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;

/**
 * Tunables for the energy unit conversion.
 *
 * HE is our own unit, FE is the Forge ecosystem's common one; the two default to 1:1,
 * and if you want FE to be worth more or less in your pack, this is where — takes effect immediately,
 * since conversion happens at every cross-unit read/write, with no startup snapshot.
 */
@Config(modid = RefStrings.MODID, name = "hbm-energy", category = "energy")
public class EnergyConfig {

	@Name("HE per FE")
	@Comment({"How much HE one FE converts to (default 1:1)",
			"Raising it means incoming external FE converts into more HE on our side; lowering, less"})
	@RangeDouble(min = 0.0001D, max = 1000000D)
	public static double HE_PER_FE = 1.0D;
}
