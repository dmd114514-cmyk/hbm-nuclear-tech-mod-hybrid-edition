package com.hbm.items.special;

import java.util.List;

import com.hbm.config.GeneralConfig;
import com.hbm.items.ItemCustomLore;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * The schraranium ingot. With "less nonsense mode" on it becomes the nikonium ingot — name and tooltips both change.
 *
 * The original swapped the texture too, which needs runtime model binding in 1.12.2; the matching
 * `models/item/ingot_nikonium.json` isn't generated yet — wire it when the model batch lands (the texture already exists).
 */
public class ItemSchraranium extends ItemCustomLore {

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		if(GeneralConfig.trueLBSM() && GeneralConfig.enableLBSMFullSchrab) {
			return "Nikonium Ingot";
		}

		return super.getItemStackDisplayName(stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(GeneralConfig.trueLBSM() && GeneralConfig.enableLBSMFullSchrab) {
			list.add("pankæk");
		}
	}
}
