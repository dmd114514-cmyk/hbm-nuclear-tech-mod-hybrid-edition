package com.hbm.items.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/*The euphemium apple. Eating it grants permanent resistance and fireproofing — if anyone can bear to eat one, that rare. */
public class ItemAppleEuphemium extends ItemFoodBase {

	public ItemAppleEuphemium(int hunger, float saturation, boolean isDogFood) {
		super(hunger, saturation, isDogFood);
		this.setAlwaysEdible();
		this.setCreativeTab(null);
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return true;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote) return;

		player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, Integer.MAX_VALUE, 120));
		player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
		player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, Integer.MAX_VALUE, 120));
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.EPIC;
	}
}
