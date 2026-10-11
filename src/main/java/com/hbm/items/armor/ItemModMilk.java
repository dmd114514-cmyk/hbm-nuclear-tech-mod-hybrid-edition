package com.hbm.items.armor;

import java.util.ArrayList;
import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The spider milk: clears every negative potion on the body; all four slots, riding the add-on slots.
 *
 * Good/bad is each potion's own isBadEffect (a public method now, no more reflection into private fields);
 * collect first, then remove — don't mutate the active-effects map while iterating it.
 */
public class ItemModMilk extends ItemArmorMod {

	public ItemModMilk() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.WHITE + "Removes bad potion effects");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.WHITE + "  " + stack.getDisplayName() + " (Removes bad potion effects)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		List<Potion> bad = new ArrayList<Potion>();

		for(PotionEffect eff : entity.getActivePotionEffects()) {
			if(eff.getPotion().isBadEffect()) {
				bad.add(eff.getPotion());
			}
		}

		for(Potion potion : bad) {
			entity.removePotionEffect(potion);
		}
	}
}
