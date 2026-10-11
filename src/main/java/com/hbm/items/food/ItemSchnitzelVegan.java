package com.hbm.items.food;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** The "vegan" schnitzel. Blindness, nausea, faster hunger, and bursting into flames — the price of virtue. */
public class ItemSchnitzelVegan extends ItemFoodBase {

	public ItemSchnitzelVegan(int hunger, boolean isDogFood) {
		super(hunger, isDogFood);
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote) return;

		player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 10 * 20, 0));
		player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 30 * 20, 0));

		// The hunger effect's level rides the food domain's `hungerEffect` knob (0 = not applied); duration still this item's own
		int hungerAmp = Math.round(tweakFood(stack, "hungerEffect", 4F));
		if(hungerAmp > 0) player.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 3 * 60 * 20, hungerAmp));

		player.addPotionEffect(new PotionEffect(MobEffects.WITHER, 3 * 20, 0));

		player.setFire(5 * 20);
		player.motionY = 2;
	}
}
