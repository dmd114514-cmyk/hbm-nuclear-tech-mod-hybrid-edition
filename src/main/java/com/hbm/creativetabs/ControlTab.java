package com.hbm.creativetabs;

import com.hbm.items.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ControlTab extends CreativeTabs {

	public ControlTab(String label) {
		super(label);
	}

	@Override
	public ItemStack getTabIconItem() {
		
		if(ModItems.pellet_rtg != null)
		{
				return new ItemStack(ModItems.pellet_rtg);
		}
		
			return new ItemStack(Items.IRON_PICKAXE);
	}
}
