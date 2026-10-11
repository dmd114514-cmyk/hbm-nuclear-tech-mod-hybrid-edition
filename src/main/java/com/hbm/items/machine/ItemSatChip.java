package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ISatChip;
import com.hbm.items.ItemBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** The satellite frequency chip. The frequency itself is ISatChip's default NBT implementation; this only displays it. */
public class ItemSatChip extends ItemBase implements ISatChip {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(I18nUtil.resolveKey("satchip.frequency") + ": " + getFreq(stack));
	}
}
