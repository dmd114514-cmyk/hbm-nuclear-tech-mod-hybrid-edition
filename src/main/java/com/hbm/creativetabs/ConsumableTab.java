package com.hbm.creativetabs;

import com.hbm.items.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ConsumableTab extends CreativeTabs {

	public ConsumableTab(String label) {
		super(label);
	}

	@Override
	public ItemStack getTabIconItem() {
		
		if(ModItems.bottle_nuka != null)
		{
				return new ItemStack(ModItems.bottle_nuka);
		}
		
			return new ItemStack(Items.IRON_PICKAXE);
	}
}
