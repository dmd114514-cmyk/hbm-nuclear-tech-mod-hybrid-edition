package com.hbm.config;

import java.util.ArrayList;

import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

/**
 * The same thing behaves three ways under three difficulty rules; the branching lives here so dozens of machines don't each copy it.
 *
 * The four LBSM readers all go through trueLBSM(): the old version rewrote enableLBSM at config load time
 * (528 on counts as LBSM off), so copying the effective value is what's equivalent — reading the raw toggle drifts.
 */
public class VersatileConfig {

	/** The converter's output: full-uranium tier yields U-233 ingots directly, otherwise only the intermediate scharanium ingots */
	public static Item getTransmutatorItem() {
		if(GeneralConfig.trueLBSM() && GeneralConfig.enableLBSMFullSchrab) return ModItems.ingot_schrabidium;
		return ModItems.ingot_schraranium;
	}

	/** Average uranium ore per U-233 ore; without LBSM it's the vanilla 250 */
	public static int getSchrabOreChance() {
		if(GeneralConfig.trueLBSM()) return GeneralConfig.schrabRate;
		return 250;
	}

	/** Potion addiction: withdrawal hits the moment the effect wears off */
	public static void applyPotionSickness(EntityLivingBase entity, int duration) {
		if(PotionConfig.potionSickness == 0) return;
		if(PotionConfig.potionSickness == 2) duration *= 12;

		PotionEffect effect = new PotionEffect(HbmPotion.potionsickness, duration * 20);
		effect.setCurativeItems(new ArrayList<ItemStack>());
		entity.addPotionEffect(effect);
	}

	public static boolean hasPotionSickness(EntityLivingBase entity) {
		return entity.isPotionActive(HbmPotion.potionsickness);
	}

	/** Whether RTG/betavoltaic fuels decay */
	public static boolean rtgDecay() {
		return GeneralConfig.enable528 || MachineConfig.doRTGsDecay;
	}

	/** Whether fuel power drops as it decays */
	public static boolean scaleRTGPower() {
		return GeneralConfig.enable528 || MachineConfig.scaleRTGPower;
	}

	private static final int MINUTE = 60 * 20;
	private static final int HOUR = 60 * MINUTE;

	/** Denominator of the long-lived-waste decay roll; smaller means faster decay */
	public static int getLongDecayChance() {
		if(GeneralConfig.enable528) return 15 * HOUR;
		if(GeneralConfig.trueLBSM() && GeneralConfig.enableLBSMShorterDecay) return 15 * MINUTE;
		return 3 * HOUR;
	}

	public static int getShortDecayChance() {
		if(GeneralConfig.enable528) return 3 * HOUR;
		if(GeneralConfig.trueLBSM() && GeneralConfig.enableLBSMShorterDecay) return 3 * MINUTE;
		return 15 * MINUTE;
	}
}
