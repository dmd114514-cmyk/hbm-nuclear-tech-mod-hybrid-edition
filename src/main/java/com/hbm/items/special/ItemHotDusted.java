package com.hbm.items.special;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** The dusted steel ingots. Each extra forging pass shaves ten degrees off the storable heat. */
public class ItemHotDusted extends ItemHot {

	public ItemHotDusted(int heat) {
		super(heat);
		this.setHasSubtypes(true);
	}

	@Override
	protected int getHeatCapacity(ItemStack stack) {
		return this.heat - stack.getItemDamage() * 10;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(I18nUtil.resolveKey("item.hot_dusted.forged", stack.getItemDamage()));
	}
}
