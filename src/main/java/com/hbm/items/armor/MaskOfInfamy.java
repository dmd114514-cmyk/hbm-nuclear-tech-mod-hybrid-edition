package com.hbm.items.armor;

import com.hbm.lib.RefStrings;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/*The mask of infamy. One texture for everything, no effects at all — purely an outfit for wastelanders. */
public class MaskOfInfamy extends ItemArmor {

	public MaskOfInfamy(ArmorMaterial mat, EntityEquipmentSlot slot) {
		super(mat, 0, slot);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		return RefStrings.MODID + ":textures/armor/MaskOfInfamy.png";
	}
}
