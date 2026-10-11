package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** The graphite pile's rod stock. Half the tooltip is generic guidance, half this rod's own, split on `$`. */
public class ItemPileRod extends ItemBase {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		for(String loc : I18nUtil.resolveKey("desc.item.pileRod").split("\\$")) {
			list.add(loc);
		}

		for(String loc : I18nUtil.resolveKey(this.getUnlocalizedName() + ".desc").split("\\$")) {
			list.add(loc);
		}
	}
}
