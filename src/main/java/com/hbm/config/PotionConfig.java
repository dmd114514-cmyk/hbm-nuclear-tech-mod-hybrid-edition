package com.hbm.config;

import com.hbm.lib.RefStrings;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeInt;

/**
 * Potion behavior tunables. The old "8.xx_xxxPotionID" family didn't come over:
 * 1.12.2 addresses potion effects by registry name; there are no numeric IDs left to configure.
 */
@Config(modid = RefStrings.MODID, name = "hbm-potion", category = "potion")
public class PotionConfig {

	@Name("Potion Sickness")
	@Comment("Addiction penalty for drinking potions. 0 = off (OFF in the old config), 1 = normal (NORMAL), 2 = twelve times the duration (TERRARIA)")
	@RangeInt(min = 0, max = 2)
	public static int potionSickness = 0;
}
