package com.hbm.items.armor;

import java.util.List;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The servo set: the same item on the chest vs the legs is two different effects, so addDesc and modUpdate both branch,
 *
 * The old version compared armorType == 1/2; armorType is the slot enum here, so the branch reads CHEST/LEGS;
 * the speed buff uses operation 2 (multiply), not colliding with the armor's own tier.
 */
public class ItemModServos extends ItemArmorMod {

	public ItemModServos() {
		super(ArmorModHandler.servos, false, true, true, false);
	}

	private static EntityEquipmentSlot slotOf(ItemStack armor) {
		return ((ItemArmor) armor.getItem()).armorType;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.servo_set) {
			list.add(TextFormatting.DARK_PURPLE + "Chestplate: Haste I / Damage +50%");
			list.add(TextFormatting.DARK_PURPLE + "Leggings: Speed +25% / Jump II");
		}
		if(this == ModItems.servo_set_desh) {
			list.add(TextFormatting.DARK_PURPLE + "Chestplate: Haste III / Damage +150%");
			list.add(TextFormatting.DARK_PURPLE + "Leggings: Speed +50% / Jump III");
		}

		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {

		if(slotOf(armor) == EntityEquipmentSlot.CHEST) {

			if(this == ModItems.servo_set)
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Haste I / Damage +50%)");
			if(this == ModItems.servo_set_desh)
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Haste III / Damage +150%)");
		}

		if(slotOf(armor) == EntityEquipmentSlot.LEGS) {

			if(this == ModItems.servo_set)
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Speed +25% / Jump II)");
			if(this == ModItems.servo_set_desh)
				list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (Speed +50% / Jump III)");
		}
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(slotOf(armor) == EntityEquipmentSlot.CHEST) {

			if(this == ModItems.servo_set)
				entity.addPotionEffect(new PotionEffect(MobEffects.HASTE, 60, 0));
			if(this == ModItems.servo_set_desh)
				entity.addPotionEffect(new PotionEffect(MobEffects.HASTE, 60, 2));
		}

		if(slotOf(armor) == EntityEquipmentSlot.LEGS) {

			if(this == ModItems.servo_set)
				entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 60, 1));
			if(this == ModItems.servo_set_desh)
				entity.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 60, 2));
		}
	}

	@Override
	public Multimap<String, AttributeModifier> getModifiers(ItemStack armor) {

		Multimap<String, AttributeModifier> multimap = super.getModifiers(armor);

		int slot = ModArmor.slotIndex(slotOf(armor));

		if(slotOf(armor) == EntityEquipmentSlot.CHEST) {

			if(this == ModItems.servo_set)
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[slot], "NTM Armor Mod Servos", 0.5, 2));

			if(this == ModItems.servo_set_desh)
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[slot], "NTM Armor Mod Servos", 1.5, 2));
		}

		if(slotOf(armor) == EntityEquipmentSlot.LEGS) {

			if(this == ModItems.servo_set)
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[slot], "NTM Armor Mod Servos", 0.25, 2));

			if(this == ModItems.servo_set_desh)
				multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
						new AttributeModifier(ArmorModHandler.UUIDs[slot], "NTM Armor Mod Servos", 0.5, 2));
		}

		return multimap;
	}
}
