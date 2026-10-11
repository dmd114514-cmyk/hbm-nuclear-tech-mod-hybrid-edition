package com.hbm.creativetabs;

import com.hbm.items.ModItems;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class WeaponTab extends CreativeTabs {

	public WeaponTab(int index, String name) {
		super(index, name);
	}

	@Override
	public ItemStack getTabIconItem() {
		if(ModItems.gun_greasegun != null) 	return new ItemStack(ModItems.gun_greasegun);
			return new ItemStack(Items.IRON_PICKAXE);
	}
}
