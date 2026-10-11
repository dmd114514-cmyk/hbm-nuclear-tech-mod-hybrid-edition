package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The insert mods. The shield value is simply "extra HP", and the color jitters by clock milliseconds — the old writing;
 * it looks like an easter egg but is meant to make inserts flicker in the tooltip.
 */
public class ItemModShield extends ItemArmorMod {

	public final float shield;

	public ItemModShield(float shield) {
		super(ArmorModHandler.kevlar, false, true, false, false);
		this.shield = shield;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(flicker(TextFormatting.YELLOW, TextFormatting.GOLD) + "+" + (Math.round(shield * 10) * 0.1) + " shield");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(flicker(TextFormatting.YELLOW, TextFormatting.GOLD) + "  " + stack.getDisplayName() + " (+" + (Math.round(shield * 10) * 0.1) + " health)");
	}
}
