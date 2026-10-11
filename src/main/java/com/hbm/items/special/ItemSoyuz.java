package com.hbm.items.special;

import java.util.List;

import com.hbm.items.IMultiModelItem;
import com.hbm.items.ItemBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The Soyuz. Three metas are three paint jobs, rarity climbing with them. */
public class ItemSoyuz extends ItemBase implements IMultiModelItem {

	public ItemSoyuz() {
		this.setHasSubtypes(true);
	}

	@Override
	public String[] subtypeModels() {
		String name = this.getRegistryName().getResourcePath();
		return new String[] { name, name, name };
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(int i = 0; i < 3; i++) list.add(new ItemStack(this, 1, i));
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		switch(stack.getItemDamage()) {
		case 0: return EnumRarity.UNCOMMON;
		case 1: return EnumRarity.RARE;
		case 2: return EnumRarity.EPIC;
		default: return EnumRarity.COMMON;
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add("Skin:");

		switch(stack.getItemDamage()) {
		case 0: list.add(TextFormatting.GOLD + "Original"); break;
		case 1: list.add(TextFormatting.BLUE + "Luna Space Center"); break;
		case 2: list.add(TextFormatting.GREEN + "Post War"); break;
		}
	}
}
