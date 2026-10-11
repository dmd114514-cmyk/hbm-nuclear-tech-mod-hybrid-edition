package com.hbm.hazard.transformer;

import java.util.List;

import com.hbm.hazard.HazardEntry;

import net.minecraft.item.ItemStack;

/**
 * A second layer on top of the hazard table: turns dynamic facts like how much radiation this thing is carrying right now into a HazardEntry.
 * Runs once before and once after entries expand; the first can change the base, the second appends.
 */
public abstract class HazardTransformerBase {

	public abstract void transformPre(ItemStack stack, List<HazardEntry> entries);

	public abstract void transformPost(ItemStack stack, List<HazardEntry> entries);
}
