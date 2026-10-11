package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The punch add-on: occupies the servos slot; the effect rolls on the melee side — the item layer only carries the tooltip
 */
public class ItemModTwoKick extends ItemArmorMod {

	public ItemModTwoKick() {
		super(ArmorModHandler.servos, false, true, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.ITALIC + "\"I've had worse\"");
		list.add(TextFormatting.YELLOW + "Punches fire 12 gauge shells");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + "  " + stack.getDisplayName() + " (Shotgun punches)");
	}
}
