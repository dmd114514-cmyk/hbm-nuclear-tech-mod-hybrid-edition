package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The serum: swaps poison for a stretch of strength while mounted, re-checked every tick. */
public class ItemModSerum extends ItemArmorMod {

	public ItemModSerum() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.GREEN + "Cures poison and gives strength");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.BLUE + "  " + stack.getDisplayName() + " (replaces poison with strength)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!entity.world.isRemote && entity.isPotionActive(MobEffects.POISON)) {
			entity.removePotionEffect(MobEffects.POISON);
			entity.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 100, 4));
		}
	}
}
