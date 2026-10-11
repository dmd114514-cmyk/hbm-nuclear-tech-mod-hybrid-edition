package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemEnums.EnumExpensiveType;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The "expensive" family of parts: all shapes live in the enum; the item itself adds one tooltip line. */
public class ItemExpensive extends ItemEnumMulti {

	public ItemExpensive() {
		super(EnumExpensiveType.class, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.RED + "Expensive mode item");
	}
}
