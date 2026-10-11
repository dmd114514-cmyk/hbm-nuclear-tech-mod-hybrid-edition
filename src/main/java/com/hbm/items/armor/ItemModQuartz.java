package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** The quartz-plutonium piece: taking a hit counts as a cold shower, washing off one tier of radiation. */
public class ItemModQuartz extends ItemArmorMod {

	public ItemModQuartz() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.DARK_GRAY + "Taking damage removes 10 RAD");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.DARK_GRAY + "  " + stack.getDisplayName() + " (-10 RAD when hit)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(!event.getEntityLiving().world.isRemote) {
			float rad = HbmLivingProps.getRadiation(event.getEntityLiving());
			rad = Math.max(rad - 10, 0);
			HbmLivingProps.setRadiation(event.getEntityLiving(), rad);
		}
	}
}
