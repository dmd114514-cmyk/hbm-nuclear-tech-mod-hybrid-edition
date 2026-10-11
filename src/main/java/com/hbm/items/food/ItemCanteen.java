package com.hbm.items.food;

import java.util.List;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

/**
 * The canteen: cooldown after drinking (damage pushed to full), climbing back one step at a time via {@link #onUpdate}.
 *
 * The cooldown seconds are a constructor argument, so each canteen is its own instance; this class matches only the existing one (vodka),
 * contents still branching via `this == ModItems.xxx`.
 */
public class ItemCanteen extends ItemBase {

	public ItemCanteen(int cooldown) {

		this.setMaxDamage(cooldown);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean b) {

		if(stack.getItemDamage() > 0 && entity.ticksExisted % 20 == 0)
			stack.setItemDamage(stack.getItemDamage() - 1);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {

		if(!(entity instanceof EntityPlayer))
			return stack;

		EntityPlayer player = (EntityPlayer) entity;

		stack.setItemDamage(stack.getMaxDamage());

		if(this == ModItems.canteen_vodka) {
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 10 * 20, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 30 * 20, 2));
		}

		VersatileConfig.applyPotionSickness(player, 5);

		return stack;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 10;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(stack.getItemDamage() == 0 && !VersatileConfig.hasPotionSickness(player)) {
			player.setActiveHand(hand);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}

		return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if(this == ModItems.canteen_vodka) {
			list.add("Cooldown: 3 minutes");
			list.add("Nausea I for 10 seconds");
			list.add("Strength III for 30 seconds");
			list.add("");

			if(ModItems.polaroidID == 11)
				list.add("Time to get hammered & sickled!");
			else
				list.add("Smells like disinfectant, tastes like disinfectant.");
		}
	}
}
