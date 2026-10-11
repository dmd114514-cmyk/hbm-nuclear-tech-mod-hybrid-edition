package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.IColoredItem;
import com.hbm.items.special.ItemNuclearWaste;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Spent fuel. Two tiers per piece: 0 straight out of the reactor, 1 cooled, the cooled tier a full-texture tint
 */
public class ItemDepletedFuel extends ItemNuclearWaste implements IColoredItem {

	public ItemDepletedFuel() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(int i = 0; i < 2; i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {
		return stack.getItemDamage() > 0 ? 0xFFBFA5 : 0xFFFFFF;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(stack.getItemDamage() > 0)
			list.add(TextFormatting.GOLD + I18nUtil.resolveKey("desc.item.wasteCooling"));
	}
}
