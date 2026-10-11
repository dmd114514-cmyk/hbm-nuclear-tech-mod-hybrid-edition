package com.hbm.items.food;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ItemEnumMulti;
import com.hbm.potion.HbmPotion;

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
 * The flasks: two contents split by meta; one raises the shield cap, the other delivers a stim.
 *
 * The old version hung a suffixed texture per enum via registerIcons; that's the per-meta model JSONs' job here
 * (`flask_infusion_<enum lowercase>`), leaving only data and settlement.
 * The shield tier refuses to even start drinking when the shield is full, sparing an empty drinking animation.
 */
public class ItemFlask extends ItemEnumMulti {

	public static enum EnumInfusion {
		SHIELD,
		NITAN
	}

	public ItemFlask() {
		super(EnumInfusion.class, true, true);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 32;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		if(stack.getItemDamage() == EnumInfusion.SHIELD.ordinal() && HbmPlayerProps.getData(player).maxShield >= HbmPlayerProps.shieldCap)
			return new ActionResult<ItemStack>(EnumActionResult.PASS, stack);

		player.setActiveHand(hand);
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entity) {

		if(!(entity instanceof EntityPlayer))
			return stack;

		EntityPlayer player = (EntityPlayer) entity;

		if(!player.capabilities.isCreativeMode)
			stack.shrink(1);

		if(world.isRemote)
			return stack;

		HbmPlayerProps props = HbmPlayerProps.getData(player);

		if(stack.getItemDamage() == EnumInfusion.SHIELD.ordinal()) {
			float infusion = 5F;
			props.maxShield = Math.min(HbmPlayerProps.shieldCap, props.maxShield + infusion);
			props.shield = Math.min(props.shield + infusion, props.getEffectiveMaxShield());
		}

		if(stack.getItemDamage() == EnumInfusion.NITAN.ordinal()) {
			props.nitanCount = props.nitanCount + 1;
			player.addPotionEffect(new PotionEffect(HbmPotion.nitan, 60 * 20 * 3, 0));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 60 * 20 * 3, 3));
		}

		return stack;
	}
}
