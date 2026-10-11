package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ItemSounds;
import com.hbm.potion.HbmPotion;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The 5-HTP injector: jumps out on its own once radiation piles up, steadying the player and restoring some health,
 * removing itself from the slot when spent — the only one of these that "self-destructs".
 */
public class ItemModAuto extends ItemArmorMod {

	public ItemModAuto() {
		super(ArmorModHandler.extra, false, true, false, false);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.BLUE + "Imported from Japsterdam.");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.BLUE + "  " + stack.getDisplayName());
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(entity.world.isRemote)
			return;

		if(HbmLivingProps.getDigamma(entity) >= 5F) {
			ArmorModHandler.removeMod(armor, ArmorModHandler.extra);
			entity.world.playSound(null, entity.posX, entity.posY, entity.posZ,
					ItemSounds.resolve("item.syringe"), SoundCategory.PLAYERS, 1.0F, 1.0F);
			HbmLivingProps.setDigamma(entity, HbmLivingProps.getDigamma(entity) - 5F);
			entity.addPotionEffect(new PotionEffect(HbmPotion.stability, 60 * 20, 0));
			entity.heal(20F);
		}
	}
}
