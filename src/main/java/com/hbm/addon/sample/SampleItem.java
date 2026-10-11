package com.hbm.addon.sample;

import net.minecraft.item.Item;

/** Sample item: just here to prove the register-model-lang chain works. */
public class SampleItem extends Item {

	public SampleItem() {
		setUnlocalizedName("hbm.sample_item");
		setCreativeTab(SampleCreativeTab.INSTANCE);
		setMaxStackSize(64);
	}
}
