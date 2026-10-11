package com.hbm.items.special;

import java.util.List;

import com.hbm.items.IMultiModelItem;
import com.hbm.items.ItemBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The starmetal ingots. Four metas, each a star's name, with texture names bearing no relation to the meta number,
 * so it reports its own model names — a hand mapping nothing can derive.
 */
public class ItemStarmetal extends ItemBase implements IMultiModelItem {

	private static final String[] MODELS = {
			"ingot_starmetal", "ingot_starmetal_astra", "ingot_starmetal_ursa", "ingot_starmetal_orion"
	};

	public ItemStarmetal() {
		this.setHasSubtypes(true);
	}

	@Override
	public String[] subtypeModels() {
		return MODELS;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		switch(stack.getItemDamage()) {
		case 1: list.add(TextFormatting.ITALIC + "Astra"); break;
		case 2: list.add(TextFormatting.ITALIC + "Ursa"); break;
		case 3: list.add(TextFormatting.ITALIC + "Orion"); break;
		}
	}
}
