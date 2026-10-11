package com.hbm.items.armor;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/*The heart container family: fixed HP onto the chest slot. The black diamond one is just a tooltip gag. */
public class ItemModHealth extends ItemArmorMod {

	float health;

	public ItemModHealth(float health) {
		super(ArmorModHandler.extra, false, true, false, false);
		this.health = health;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		String color = flicker(TextFormatting.RED, TextFormatting.LIGHT_PURPLE);

		list.add(color + "+" + (Math.round(health * 10) * 0.1) + " health");
		list.add("");

		if(this == ModItems.black_diamond) {
			list.add(TextFormatting.DARK_GRAY + "Nostalgia");
			list.add("");
		}

		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(flicker(TextFormatting.RED, TextFormatting.LIGHT_PURPLE) + "  " + stack.getDisplayName() + " (+" + (Math.round(health * 10) * 0.1) + " health)");
	}

	@Override
	public Multimap<String, AttributeModifier> getModifiers(ItemStack armor) {

		Multimap<String, AttributeModifier> multimap = super.getModifiers(armor);

		multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(),
				new AttributeModifier(ArmorModHandler.UUIDs[ModArmor.slotIndex(((ItemArmor) armor.getItem()).armorType)], "NTM Armor Mod Health", health, 0));

		return multimap;
	}
}
