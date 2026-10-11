package com.hbm.items.machine;

import java.util.Arrays;
import java.util.Locale;

import com.hbm.items.IMultiModelItem;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * The stamp booklet. Eight pages for eight print dies, the page count in meta with a page-suffixed name —
 * the body has one texture, so all eight metas share one model.
 */
public class ItemStampBook extends ItemStamp implements IMultiModelItem {

	public ItemStampBook() {
		super(0, null);

		// all eight pages register into the die table at construction, so machines can look up print dies from the booklet too
		for(int i = 0; i < 8; i++) {
			this.addStampToList(this, i, this.getStampType(this, i));
		}
	}

	@Override
	public String[] subtypeModels() {
		String name = this.getRegistryName().getResourcePath();
		String[] names = new String[8];
		Arrays.fill(names, name);
		return names;
	}

	@Override
	public StampType getStampType(Item item, int meta) {
		meta %= 8;
		return StampType.values()[StampType.PRINTING1.ordinal() + meta];
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(int i = 0; i < 8; i++) list.add(new ItemStack(this, 1, i));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + this.getStampType(stack.getItem(), stack.getItemDamage()).name().toLowerCase(Locale.US);
	}
}
