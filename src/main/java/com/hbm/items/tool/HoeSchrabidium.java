package com.hbm.items.tool;

import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;

/** The schrabidium hoe. Nothing special beyond a blue rarity — same as any other hoe. */
public class HoeSchrabidium extends ItemHoe {

	public HoeSchrabidium(ToolMaterial mat) {
		super(mat);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.RARE;
	}
}
