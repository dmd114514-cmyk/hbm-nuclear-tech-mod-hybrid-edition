package com.hbm.items.food;

import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * A can of drink. Forty ticks to drink, a glug every five early on, and a final murmur after the last gulp.
 * Vanilla stretched drinking by editing `itemInUseCount`; 1.12.2 hands that to setActiveHand/resetActiveHand.
 */
public class ItemBDCL extends ItemBase {

	private static final SoundEvent GULP = ItemSounds.resolve("player.gulp");
	private static final SoundEvent GROAN = ItemSounds.resolve("player.groan");

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		player.setActiveHand(hand);
		return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, net.minecraft.entity.EntityLivingBase entity) {

		if(entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) entity;

			if(!player.capabilities.isCreativeMode) stack.shrink(1);

			player.world.playSound(null, player.posX, player.posY, player.posZ, GROAN, SoundCategory.PLAYERS, 1F, 1F);
		}

		return stack;
	}

	@Override
	public void onUsingTick(ItemStack stack, net.minecraft.entity.EntityLivingBase entity, int count) {

		if(!(entity instanceof EntityPlayer)) return;

		EntityPlayer player = (EntityPlayer) entity;

		if(count % 5 == 0 && count >= 10) {
			player.world.playSound(null, player.posX, player.posY, player.posZ, GULP, SoundCategory.PLAYERS, 1F, 1F);
		}
	}
}
