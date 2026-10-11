package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The medal: chest slot, shaving a little radiation every tick — "mounted means reduced", not hit-triggered. */
public class ItemModMedal extends ItemArmorMod {

	public ItemModMedal() {
		super(ArmorModHandler.plate_only, true, true, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.GOLD + "-10 RAD/s");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.GOLD + "  " + stack.getDisplayName() + " (-10 RAD/s)");
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!entity.world.isRemote) {
			float rad = HbmLivingProps.getRadiation(entity);
			rad -= 0.5F;
			HbmLivingProps.setRadiation(entity, Math.max(rad, 0));
		}
	}
}
