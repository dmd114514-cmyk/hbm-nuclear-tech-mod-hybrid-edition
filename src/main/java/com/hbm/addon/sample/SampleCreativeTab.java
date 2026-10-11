package com.hbm.addon.sample;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/**
 * Sample creative tab. Gets replaced once the real creative tab system ships.
 */
public class SampleCreativeTab extends CreativeTabs {

	public static final SampleCreativeTab INSTANCE = new SampleCreativeTab();

	public SampleCreativeTab() {
		super("hbm.sample");
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(SampleModule.SAMPLE_ITEM);
	}
}
