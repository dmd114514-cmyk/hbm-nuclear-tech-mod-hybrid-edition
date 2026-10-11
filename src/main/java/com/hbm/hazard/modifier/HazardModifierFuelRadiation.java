package com.hbm.hazard.modifier;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

/**
 * Fuel rods and the like get hotter as they burn: spent durability pushes the hazard level up towards target.
 * the 0.4 power makes it drop slowly early and catch up late - do not make it linear.
 */
public class HazardModifierFuelRadiation extends HazardModifier {

	float target;

	public HazardModifierFuelRadiation(float target) {
		this.target = target;
	}

	@Override
	public float modify(ItemStack stack, EntityLivingBase holder, float level) {
		double depletion = Math.pow(stack.getItem().getDurabilityForDisplay(stack), 0.4D);
		level = (float) (level + (this.target - level) * depletion);

		return level;
	}
}
