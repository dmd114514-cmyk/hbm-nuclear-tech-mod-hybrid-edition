package com.hbm.items.armor;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The iron cladding: half a tier of knockback resistance, riding the armor slot's UUID. */
public class ItemModIron extends ItemArmorMod {

	public ItemModIron() {
		super(ArmorModHandler.cladding, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.WHITE + "+0.5 knockback resistance");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.WHITE + "  " + stack.getDisplayName() + " (+0.5 knockback resistence)");
	}

	@Override
	public Multimap<String, AttributeModifier> getModifiers(ItemStack armor) {

		Multimap<String, AttributeModifier> multimap = super.getModifiers(armor);

		multimap.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
				new AttributeModifier(ArmorModHandler.UUIDs[ModArmor.slotIndex(((ItemArmor) armor.getItem()).armorType)], "NTM Armor Mod Knockback", 0.5, 0));

		return multimap;
	}
}
