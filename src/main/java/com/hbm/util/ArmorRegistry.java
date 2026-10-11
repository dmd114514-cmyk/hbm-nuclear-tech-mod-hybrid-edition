package com.hbm.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import api.hbm.hazard.HazardClass;
import api.hbm.item.IGasMask;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The hazard protection registry. Protection keys off "what this piece of gear declares it blocks", not the item itself,
 * so new gear never needs the check logic touched (the old hard-coded ModItems checks are retired).
 *
 * Armor slot layout keeps the original mod's slot notation: 0=head 1=chest 2=legs 3=feet, reading gear takes slot+1.
 */
public class ArmorRegistry {

	public static final HashMap<Item, ArrayList<HazardClass>> hazardClasses = new HashMap<>();

	public static void registerHazard(Item item, HazardClass... hazards) {
		hazardClasses.put(item, new ArrayList<>(Arrays.asList(hazards)));
	}

	/** Whether the given slot's gear covers all listed hazards (the AI hazmat check goes through here) */
	public static boolean hasAllProtection(EntityLivingBase entity, int slot, HazardClass... clazz) {

		if(ArmorUtil.checkArmorNull(entity, slot)) return false;

		List<HazardClass> list = getProtectionFromItem(entity.getItemStackFromSlot(armorSlot(slot)), entity);
		return list.containsAll(Arrays.asList(clazz));
	}

	public static boolean hasAnyProtection(EntityLivingBase entity, int slot, HazardClass... clazz) {

		if(ArmorUtil.checkArmorNull(entity, slot)) return false;

		List<HazardClass> list = getProtectionFromItem(entity.getItemStackFromSlot(armorSlot(slot)), entity);
		for(HazardClass haz : clazz) {
			if(list.contains(haz)) return true;
		}
		return false;
	}

	public static boolean hasProtection(EntityLivingBase entity, int slot, HazardClass clazz) {

		if(ArmorUtil.checkArmorNull(entity, slot)) return false;
		return getProtectionFromItem(entity.getItemStackFromSlot(armorSlot(slot)), entity).contains(clazz);
	}

	private static net.minecraft.inventory.EntityEquipmentSlot armorSlot(int slot) {
		switch(slot) {
		case 0: return net.minecraft.inventory.EntityEquipmentSlot.HEAD;
		case 1: return net.minecraft.inventory.EntityEquipmentSlot.CHEST;
		case 2: return net.minecraft.inventory.EntityEquipmentSlot.LEGS;
		default: return net.minecraft.inventory.EntityEquipmentSlot.FEET;
		}
	}

	/** Sums one piece's protection: the registry + the mask's filter (minus the blacklist) */
	public static List<HazardClass> getProtectionFromItem(ItemStack stack, EntityLivingBase entity) {

		List<HazardClass> prot = new ArrayList<>();
		if(stack == null || stack.isEmpty()) return prot;

		Item item = stack.getItem();

		if(hazardClasses.containsKey(item)) prot.addAll(hazardClasses.get(item));

		if(item instanceof IGasMask) {
			IGasMask mask = (IGasMask) item;
			ItemStack filter = mask.getFilter(stack, entity);

			if(filter != null && hazardClasses.containsKey(filter.getItem())) {
				List<HazardClass> filProt = new ArrayList<>(hazardClasses.get(filter.getItem()));
				filProt.removeAll(mask.getBlacklist(stack, entity));
				prot.addAll(filProt);
			}
		}

		return prot;
	}
}
