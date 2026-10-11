package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The revive piece: durability is the remaining uses, spent means dead. Legs slot.
 *
 * Uses compute as getMaxDamage - metadata, so "repair once, lose a use" needs no extra field.
 */
public class ItemModRevive extends ItemArmorMod {

	public ItemModRevive(int durability) {
		super(ArmorModHandler.extra, false, false, true, false);
		this.setMaxDamage(durability);
	}

	private static int left(ItemStack stack) {
		return stack.getMaxDamage() - stack.getMetadata();
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.scrumpy) {
			list.add(TextFormatting.GOLD + "But how did you survive?");
			list.add(TextFormatting.RED + "I was drunk.");
		}
		if(this == ModItems.wild_p) {
			list.add(TextFormatting.DARK_GRAY + "Explosive " + TextFormatting.RED + "Reactive " + TextFormatting.DARK_GRAY + "Plot " + TextFormatting.RED + "Armor");
		}

		list.add("");
		list.add(TextFormatting.GOLD + "" + left(stack) + " revives left");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.GOLD + "  " + stack.getDisplayName() + " (" + left(stack) + " revives left)");
	}
}
