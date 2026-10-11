package com.hbm.items.food;

import com.hbm.items.IMultiModelItem;
import com.hbm.items.ItemBase;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/** The marshmallow. The toasted one is the same image recolored, so both metas share one model. */
public class ItemMarshmallow extends ItemBase implements IMultiModelItem {

	public ItemMarshmallow() {
		this.setHasSubtypes(true);
	}

	@Override
	public String[] subtypeModels() {
		String name = this.getRegistryName().getResourcePath();
		return new String[] { name, name };
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + (stack.getItemDamage() == 1 ? "_roasted" : "");
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
	}
}
