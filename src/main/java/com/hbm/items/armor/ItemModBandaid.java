package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** The bandage: a small chance of negating a hit entirely and restoring full health. */
public class ItemModBandaid extends ItemArmorMod {

	public ItemModBandaid() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.RED + "3% chance for full heal when damaged");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.RED + "  " + stack.getDisplayName() + " (3% chance for full heal)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(event.getEntity().world.rand.nextInt(100) < 3) {
			event.setAmount(0);
			event.getEntityLiving().heal(event.getEntityLiving().getMaxHealth());
		}
	}
}
