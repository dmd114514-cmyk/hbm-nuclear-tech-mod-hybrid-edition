package com.hbm.items.armor;

import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * A "texture by item identity" universal armor. It serves several sets (steel, titanium, alloy, asbestos, security...),
 * branching by item identity — so when those armors haven't migrated, those branches simply don't run; nothing breaks.
 *
 * 1.12.2 swapped ItemArmor's third argument from "slot ordinal" to EntityEquipmentSlot,
 * converted once in the constructor; call sites still pass 0/1/2/3 as before.
 */
public class ModArmor extends ItemArmor {

	public ModArmor(ArmorMaterial material, int slot) {
		super(material, 0, slotOf(slot));
	}

	/** 0 head / 1 chest / 2 legs / 3 feet — the old notation to 1.12.2 enum conversion (sibling classes use it too) */
	public static EntityEquipmentSlot slotOf(int slot) {

		switch(slot) {
		case 0: return EntityEquipmentSlot.HEAD;
		case 1: return EntityEquipmentSlot.CHEST;
		case 2: return EntityEquipmentSlot.LEGS;
		case 3: return EntityEquipmentSlot.FEET;
		default: return EntityEquipmentSlot.CHEST;
		}
	}

	/**
	 * slotOf's inverse. Attribute modifier UUIDs key off this old numbering: the enum's own getIndex() is a different arrangement
	 * (feet 0 to head 3), and using it would strand modifiers already attached in old saves forever.
	 */
	public static int slotIndex(EntityEquipmentSlot slot) {

		switch(slot) {
		case HEAD: return 0;
		case CHEST: return 1;
		case LEGS: return 2;
		case FEET: return 3;
		default: return 1;
		}
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String layer) {
		if(stack.getItem().equals(ModItems.steel_helmet) || stack.getItem().equals(ModItems.steel_plate) || stack.getItem().equals(ModItems.steel_boots)) {
			return (RefStrings.MODID + ":textures/armor/steel_1.png");
		}
		if(stack.getItem().equals(ModItems.steel_legs)) {
			return (RefStrings.MODID + ":textures/armor/steel_2.png");
		}
		if(stack.getItem().equals(ModItems.titanium_helmet) || stack.getItem().equals(ModItems.titanium_plate) || stack.getItem().equals(ModItems.titanium_boots)) {
			return (RefStrings.MODID + ":textures/armor/titanium_1.png");
		}
		if(stack.getItem().equals(ModItems.titanium_legs)) {
			return (RefStrings.MODID + ":textures/armor/titanium_2.png");
		}
		if(stack.getItem().equals(ModItems.alloy_helmet) || stack.getItem().equals(ModItems.alloy_plate) || stack.getItem().equals(ModItems.alloy_boots)) {
			return (RefStrings.MODID + ":textures/armor/alloy_1.png");
		}
		if(stack.getItem().equals(ModItems.alloy_legs)) {
			return (RefStrings.MODID + ":textures/armor/alloy_2.png");
		}
		if(stack.getItem().equals(ModItems.cmb_helmet) || stack.getItem().equals(ModItems.cmb_plate) || stack.getItem().equals(ModItems.cmb_boots)) {
			return (RefStrings.MODID + ":textures/armor/cmb_1.png");
		}
		if(stack.getItem().equals(ModItems.cmb_legs)) {
			return (RefStrings.MODID + ":textures/armor/cmb_2.png");
		}
		if(stack.getItem().equals(ModItems.paa_plate) || stack.getItem().equals(ModItems.paa_boots)) {
			return (RefStrings.MODID + ":textures/armor/paa_1.png");
		}
		if(stack.getItem().equals(ModItems.paa_legs)) {
			return (RefStrings.MODID + ":textures/armor/paa_2.png");
		}
		if(stack.getItem().equals(ModItems.asbestos_helmet) || stack.getItem().equals(ModItems.asbestos_plate) || stack.getItem().equals(ModItems.asbestos_boots)) {
			return (RefStrings.MODID + ":textures/armor/asbestos_1.png");
		}
		if(stack.getItem().equals(ModItems.asbestos_legs)) {
			return (RefStrings.MODID + ":textures/armor/asbestos_2.png");
		}
		if(stack.getItem().equals(ModItems.jackt)) {
			return (RefStrings.MODID + ":textures/armor/jackt.png");
		}
		if(stack.getItem().equals(ModItems.jackt2)) {
			return (RefStrings.MODID + ":textures/armor/jackt2.png");
		}
		if(stack.getItem().equals(ModItems.security_helmet) || stack.getItem().equals(ModItems.security_plate) || stack.getItem().equals(ModItems.security_boots)) {
			return (RefStrings.MODID + ":textures/armor/security_1.png");
		}
		if(stack.getItem().equals(ModItems.security_legs)) {
			return (RefStrings.MODID + ":textures/armor/security_2.png");
		}
		if(stack.getItem().equals(ModItems.mask_rag)) {
			return (RefStrings.MODID + ":textures/armor/rag_damp.png");
		}
		if(stack.getItem().equals(ModItems.mask_piss)) {
			return (RefStrings.MODID + ":textures/armor/rag_piss.png");
		}

		return null;
	}
}
