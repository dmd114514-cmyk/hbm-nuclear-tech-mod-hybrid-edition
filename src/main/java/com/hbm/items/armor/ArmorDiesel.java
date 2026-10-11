package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.render.model.ModelArmorDiesel;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The diesel suit (registered as dieselsuit_*): the same fuel base as the steam suit, burning diesel and shrugging knockback.
 *
 * The +25% knockback resistance rides the same fixed UUID as the inserts, so same-slot stacking isn't read as two independent modifiers.
 */
public class ArmorDiesel extends ArmorFSBFueled {

	public ArmorDiesel(ArmorMaterial material, int slot, String texture, FluidType fuelType, int maxFuel, int fillRate, int consumption, int drain) {
		super(material, slot, texture, fuelType, maxFuel, fillRate, consumption, drain);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {

		Multimap<String, AttributeModifier> map = super.getItemAttributeModifiers(equipmentSlot);

		if(equipmentSlot == this.armorType) {
			map.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(), new AttributeModifier(ArmorModHandler.fixedUUIDs[ModArmor.slotIndex(this.armorType)], "Armor modifier", 0.25D, 1));
		}

		return map;
	}

	@SideOnly(Side.CLIENT)
	private ModelArmorDiesel[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorDiesel[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorDiesel(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}
}
