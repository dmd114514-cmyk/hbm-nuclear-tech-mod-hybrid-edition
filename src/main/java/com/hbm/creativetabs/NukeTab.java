package com.hbm.creativetabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class NukeTab extends CreativeTabs {

	public NukeTab(String label) {
		super(label);
		this.setBackgroundImageName("nuke.png");
	}

	@Override
	public ItemStack getTabIconItem() {
		
			return new ItemStack(Items.IRON_PICKAXE);
	}

}
