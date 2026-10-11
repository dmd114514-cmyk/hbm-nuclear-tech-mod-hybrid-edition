package com.hbm.hazard.modifier;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Hazard level converter: the same source reads differently through a pocket, with or without a mask, spent or fresh.
 * Modifiers run in the order they were attached, each one's output feeding the next.
 */
public abstract class HazardModifier {

	public abstract float modify(ItemStack stack, EntityLivingBase holder, float level);

	/** Runs every modifier in order. entity may be null (when only the tooltip is being built) */
	public static float evalAllModifiers(ItemStack stack, EntityLivingBase entity, float level, List<HazardModifier> mods) {

		for(HazardModifier mod : mods) {
			level = mod.modify(stack, entity, level);
		}

		return level;
	}
}
