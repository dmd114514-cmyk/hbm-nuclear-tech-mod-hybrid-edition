package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The radiation cladding: fits all four slots, adding one fixed decimal to radiation resistance.
 *
 * The number is the piece's entire behavior; the armor side sums cladding uniformly.
 */
public class ItemModCladding extends ItemArmorMod {

	public double rad;

	public ItemModCladding(double rad) {
		super(ArmorModHandler.cladding, true, true, true, true);
		this.rad = rad;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.YELLOW + "+" + rad + " rad-resistance");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + "  " + stack.getDisplayName() + " (+" + rad + " radiation resistance)");
	}
}
