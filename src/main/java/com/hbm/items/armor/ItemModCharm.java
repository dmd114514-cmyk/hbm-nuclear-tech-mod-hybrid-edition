package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * The charm. The two differ only in revolver-damage reduction: protection_charm halves it, meteor_charm erases it.
 *
 * The tooltip lines about "meteors no longer break blocks" and "no more meteor spawns" are effects the weather/meteor side reads off this table —
 * the item itself only handles the reduction line.
 */
public class ItemModCharm extends ItemArmorMod {

	public ItemModCharm() {
		super(ArmorModHandler.helmet_only, true, true, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.AQUA + "You feel blessed.");

		if(this == ModItems.protection_charm) {
			list.add(TextFormatting.AQUA + "Diverts meteors away from the player.");
			list.add(TextFormatting.AQUA + "Meteors no longer destroy blocks.");
			list.add(TextFormatting.AQUA + "Halves broadcaster damage");
		}
		if(this == ModItems.meteor_charm) {
			list.add(TextFormatting.AQUA + "Disables meteorite spawning.");
			list.add(TextFormatting.AQUA + "Negates broadcaster damage");
		}

		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.GOLD + "  " + stack.getDisplayName());
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(event.getSource() == ModDamageSource.broadcast) {

			if(this == ModItems.protection_charm)
				event.setAmount(event.getAmount() * 0.5F);
			if(this == ModItems.meteor_charm)
				event.setAmount(0);
		}
	}
}
