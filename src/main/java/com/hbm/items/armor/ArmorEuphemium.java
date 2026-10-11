package com.hbm.items.armor;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.ArmorUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

/**
 * The euphemium armor. All four on and you're near-untouchable: regen, resistance, fireproof and food all maxed, plus fall damage suppressed.
 * One deliberate quirk — `setDamage` is an empty body, meaning this set **never loses durability**.
 */
public class ArmorEuphemium extends ItemArmor {

	public ArmorEuphemium(ArmorMaterial material, int armorType) {
		super(material, 0, ModArmor.slotOf(armorType));
		this.setCreativeTab(null);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String layer) {
		if(stack.getItem().equals(ModItems.euphemium_helmet) || stack.getItem().equals(ModItems.euphemium_plate) || stack.getItem().equals(ModItems.euphemium_boots)) {
			return (RefStrings.MODID + ":textures/armor/euphemium_1.png");
		}
		if(stack.getItem().equals(ModItems.euphemium_legs)) {
			return (RefStrings.MODID + ":textures/armor/euphemium_2.png");
		}

		return null;
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack armor) {

		if(ArmorUtil.checkArmor(player, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)) {

			player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 5, 127, true, true));
			player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 5, 127, true, true));
			player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 5, 127, true, true));
			player.addPotionEffect(new PotionEffect(MobEffects.SATURATION, 5, 127, true, true));

			if(player.motionY < -0.25D) {
				player.motionY = -0.25D;
				player.fallDistance = 0;
			}
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.EPIC;
	}

	//do literally nothing lole
	@Override
	public void setDamage(ItemStack stack, int damage) { }
}
