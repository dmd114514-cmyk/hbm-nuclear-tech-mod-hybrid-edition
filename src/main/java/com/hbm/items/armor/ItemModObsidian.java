package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/*The obsidian coating: once applied, this armor dropped on the ground won't burn up. The actual check reads the mod on the dropped-item side */
public class ItemModObsidian extends ItemArmorMod {

	public ItemModObsidian() {
		super(ArmorModHandler.cladding, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.DARK_PURPLE + "Makes dropped armor indestructible");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Item indestructible)");
	}
}
