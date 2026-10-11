package com.hbm.items.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** The cotton candy (the colored one). Poison first, healing after — sweetness with a price. */
public class ItemCottonCandy extends ItemFoodBase {

	public ItemCottonCandy(int hunger, boolean isDogFood) {
		super(hunger, isDogFood);
		this.setAlwaysEdible();
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote) return;

		player.addPotionEffect(new PotionEffect(MobEffects.POISON, 15 * 20, 0));
		player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 5 * 20, 0));
		player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 25 * 20, 2));
		player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 25 * 20, 2));
		player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 30 * 20, 4));
	}
}
