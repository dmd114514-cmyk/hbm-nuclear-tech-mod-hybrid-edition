package com.hbm.creativetabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Machine tab: machines and structural blocks live here */
public class MachineTab extends CreativeTabs {

	public MachineTab(String label) {
		super(label);
	}

	@Override
	public ItemStack getTabIconItem() {
		// The old icon was the power controller; that block is still a few batches out, holding the spot for now
		return new ItemStack(Items.IRON_PICKAXE);
	}
}
