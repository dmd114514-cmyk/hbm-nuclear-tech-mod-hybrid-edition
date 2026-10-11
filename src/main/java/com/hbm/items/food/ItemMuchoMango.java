package com.hbm.items.food;

import java.util.List;

import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/** A big jar of mango. Ten seconds of speed on finishing; the jar is absurdly large so it takes ages to drink. */
public class ItemMuchoMango extends ItemFoodBase {

	public ItemMuchoMango(int hunger) {
		super(hunger, true);
		this.setAlwaysEdible();
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
		if(!world.isRemote) {
			player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 200));
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if(this == ModItems.mucho_mango) {
			list.add("The Comically Large Can");
		}
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 200;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}
}
