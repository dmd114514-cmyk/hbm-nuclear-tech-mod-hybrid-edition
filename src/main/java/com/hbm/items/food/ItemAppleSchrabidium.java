package com.hbm.items.food;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;

/**
 * The three-meta apple: 0 is the plain golden-apple set, 1 is "full power", 2 is the never-expires version (2147483647 ticks).
 *
 * The same code also covers the lead apple (apple_lead): 0/1 apply lead poisoning, 2 kills you outright by lead damage —
 * so branch by item first, then by meta; don't read meta alone.
 */
public class ItemAppleSchrabidium extends ItemFoodBase {

	public ItemAppleSchrabidium(int hunger, float saturation, boolean isDogFood) {
		super(hunger, saturation, isDogFood);
		this.setHasSubtypes(true);
		this.setAlwaysEdible();
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.getMetadata() == 2;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {

		if(world.isRemote)
			return;

		int meta = stack.getMetadata();
		int forever = Integer.MAX_VALUE;

		if(stack.getItem() == ModItems.apple_schrabidium) {

			if(meta == 0) {
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 600, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 6000, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 6000, 0));
			}

			if(meta == 1) {
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 1200, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 1200, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 1200, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 1200, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 1200, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 1200, 2));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 1200, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, 1200, 9));
				player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 1200, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 1200, 9));
			}

			if(meta == 2) {
				player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, forever, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, forever, 1));
				player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, forever, 0));
				player.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, forever, 9));
				player.addPotionEffect(new PotionEffect(MobEffects.HASTE, forever, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.SPEED, forever, 3));
				player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, forever, 4));
				player.addPotionEffect(new PotionEffect(MobEffects.HEALTH_BOOST, forever, 24));
				player.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION, forever, 14));
				player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, forever, 99));
			}
		}

		if(stack.getItem() == ModItems.apple_lead) {

			if(meta == 0)
				player.addPotionEffect(new PotionEffect(HbmPotion.lead, 15 * 20, 2));

			if(meta == 1)
				player.addPotionEffect(new PotionEffect(HbmPotion.lead, 60 * 20, 4));

			if(meta == 2)
				player.attackEntityFrom(ModDamageSource.lead, 500F);
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		int meta = stack.getMetadata();

		if(meta == 0) return EnumRarity.UNCOMMON;
		if(meta == 1) return EnumRarity.RARE;
		if(meta == 2) return EnumRarity.EPIC;

		return EnumRarity.COMMON;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(!isInCreativeTab(tab))
			return;

		list.add(new ItemStack(this, 1, 0));
		list.add(new ItemStack(this, 1, 1));
		list.add(new ItemStack(this, 1, 2));
	}
}
