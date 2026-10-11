package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.render.model.ModelArmorDesh;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The steam suit (the old ArmorDesh, registered as steamsuit_*): steam-fed powered armor, worn with ModelArmorDesh.
 *
 * Its one behavioral difference is also slowing you: -2.5% speed per piece, the debuff on the same fixed UUID set as ArmorModHandler,
 * so same-slot insert stacking isn't misread by vanilla as two independent modifiers.
 */
public class ArmorDesh extends ArmorFSBFueled {

	public ArmorDesh(ArmorMaterial material, int slot, String texture, FluidType fuelType, int maxFuel, int fillRate, int consumption, int drain) {
		super(material, slot, texture, fuelType, maxFuel, fillRate, consumption, drain);
	}

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {

		Multimap<String, AttributeModifier> map = super.getItemAttributeModifiers(equipmentSlot);

		if(equipmentSlot == this.armorType) {
			map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ArmorModHandler.fixedUUIDs[ModArmor.slotIndex(this.armorType)], "Armor modifier", -0.025D, 1));
		}

		return map;
	}

	@SideOnly(Side.CLIENT)
	private ModelArmorDesh[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entity, ItemStack stack, EntityEquipmentSlot slotType, ModelBiped defaultModel) {

		if(models == null) {
			models = new ModelArmorDesh[4];

			for(int i = 0; i < 4; i++)
				models[i] = new ModelArmorDesh(i);
		}

		return models[ModArmor.slotIndex(slotType)];
	}
}
