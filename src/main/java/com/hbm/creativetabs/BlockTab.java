package com.hbm.creativetabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Block tab: ores, stone and building materials live here */
public class BlockTab extends CreativeTabs {

	public BlockTab(String label) {
		super(label);
	}

	@Override
	public ItemStack getTabIconItem() {
		// The old icon was uranium ore; that family hasn't landed, so a vanilla pickaxe holds the spot until it arrives
		return new ItemStack(Items.IRON_PICKAXE);
	}
}
