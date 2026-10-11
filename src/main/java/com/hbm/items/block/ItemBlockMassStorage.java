package com.hbm.items.block;

import com.hbm.items.IMultiModelItem;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/** The mass storage's block item: all four capacity subtypes should list in the creative tab */
public class ItemBlockMassStorage extends ItemBlockMeta implements IMultiModelItem {

	public ItemBlockMassStorage(Block block) {
		super(block);
	}

	/** Meta order = Variant enum order: iron / desh / standard / wood */
	@Override
	public String[] subtypeModels() {
		return new String[] { "mass_storage_iron", "mass_storage_desh", "mass_storage", "mass_storage_wood" };
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		//order follows 1.7.10: wood/iron/desh/standard
		list.add(new ItemStack(this, 1, 3));
		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
		list.add(new ItemStack(this, 1, 2));
	}
}
