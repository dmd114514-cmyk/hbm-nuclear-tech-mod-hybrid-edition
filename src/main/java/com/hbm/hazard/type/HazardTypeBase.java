package com.hbm.hazard.type;

import java.util.List;

import com.hbm.hazard.modifier.HazardModifier;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * An effect that goes off just from carrying it.
 * Three hooks: trigger on a living thing, trigger on the ground, and describe itself in the tooltip.
 */
public abstract class HazardTypeBase {

	/** Where the hazard actually fires; level is the value after all modifiers */
	public abstract void onUpdate(EntityLivingBase target, float level, ItemStack stack);

	/** Dropped items count too: water and fire triggers only go off once it is on the ground */
	public abstract void updateEntity(EntityItem item, float level);

	/** Tooltip. level is the base value before modifiers, and the modifiers are handed over separately so a subclass decides whether to use them */
	@SideOnly(Side.CLIENT)
	public abstract void addHazardInformation(EntityPlayer player, List<String> list, float level, ItemStack stack, List<HazardModifier> modifiers);
}
