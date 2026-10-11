package com.hbm.creativetabs;

import com.hbm.items.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class TemplateTab extends CreativeTabs {

	public TemplateTab(String label) {
		super(label);
		setBackgroundImageName("item_search.png");
	}

	@Override
	public ItemStack getTabIconItem() {
		
		if(ModItems.blueprints != null) {
				return new ItemStack(ModItems.blueprints);
		}
		
			return new ItemStack(Items.IRON_PICKAXE);
	}

	@Override
	public boolean hasSearchBar() {
		return true;
	}

}
