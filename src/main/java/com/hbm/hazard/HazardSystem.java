package com.hbm.hazard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.hazard.transformer.HazardTransformerBase;
import com.hbm.hazard.type.HazardTypeBase;
import com.hbm.interfaces.Untested;
import com.hbm.inventory.RecipesCommon.ComparableStack;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

@Untested
public class HazardSystem {

	/*
	 * The ore dict table, always evaluated first. Be careful adding HazardData with doesOverride here - the order follows the item's own ore dict keys.
	 */
	public static final HashMap<String, HazardData> oreMap = new HashMap<>();
	/*
	 * Registered per item, either with a wildcard damage or because it genuinely has a pile of .damage values - tools, for instance.
	 */
	public static final HashMap<Item, HazardData> itemMap = new HashMap<>();
	/*
	 * Registered down to item plus damage. ComparableStack does not compare NBT; to scale hazards by NBT use a HazardModifier.
	 */
	public static final HashMap<ComparableStack, HazardData> stackMap = new HashMap<>();
	/*
	 * Things exempt from the hazard system entirely, for one reason or another.
	 */
	public static final HashSet<ComparableStack> stackBlacklist = new HashSet<>();
	public static final HashSet<String> dictBlacklist = new HashSet<>();
	/*
	 * The transform list, run in order before and after all HazardEntries are expanded.
	 */
	public static final List<HazardTransformerBase> trafos = new ArrayList<>();

	/** Worn things = main hand plus four armour pieces; the off hand does not count, matching the original logic's five equipment slots */
	private static final EntityEquipmentSlot[] CARRIED_SLOTS = { EntityEquipmentSlot.FEET, EntityEquipmentSlot.LEGS,
			EntityEquipmentSlot.CHEST, EntityEquipmentSlot.HEAD };

	/** Registers by whatever the first argument is, saving callers a cast */
	public static void register(Object o, HazardData data) {

		if(o instanceof String)
			oreMap.put((String)o, data);
		if(o instanceof Item)
			itemMap.put((Item)o, data);
		if(o instanceof Block)
			itemMap.put(Item.getItemFromBlock((Block)o), data);
		if(o instanceof ItemStack)
			stackMap.put(new ComparableStack((ItemStack)o), data);
		if(o instanceof ComparableStack)
			stackMap.put((ComparableStack)o, data);
	}

	/** Makes this slot return no HazardData at all */
	public static void blacklist(Object o) {

		if(o instanceof ItemStack) {
			stackBlacklist.add(new ComparableStack((ItemStack) o).makeSingular());
		} else if(o instanceof String) {
			dictBlacklist.add((String) o);
		}
	}

	public static boolean isItemBlacklisted(ItemStack stack) {

		ComparableStack comp = new ComparableStack(stack).makeSingular();
		if(stackBlacklist.contains(comp))
			return true;

		int[] ids = OreDictionary.getOreIDs(stack);
		for(int id : ids) {
			String name = OreDictionary.getOreName(id);

			if(dictBlacklist.contains(name)) {
				stackBlacklist.add(comp); // Computed once and remembered; ore dict lookups are not cheap
				return true;
			}
		}

		return false;
	}

	/**
	 * Collects every hazard entry that currently applies to this item.
	 * <br><br>Order: ore dict (in the item's own key order when there are several) -> item -> item stack.
	 * <br><br>Applicable means anything overridden or pushed out by a mutex is not in the result. An override entry wipes everything before it;
	 * a mutex entry does nothing when it collides with an occupied bit. The mutex mechanism assumes both entries rank the same, so order does not matter either way.
	 */
	public static List<HazardEntry> getHazardsFromStack(ItemStack stack) {

		if(isItemBlacklisted(stack)) {
			return new ArrayList<>();
		}

		List<HazardData> chronological = new ArrayList<>();

		///// ore dict
		int[] ids = OreDictionary.getOreIDs(stack);
		for(int id : ids) {
			String name = OreDictionary.getOreName(id);

			if(oreMap.containsKey(name))
				chronological.add(oreMap.get(name));
		}

		///// items
		if(itemMap.containsKey(stack.getItem()))
			chronological.add(itemMap.get(stack.getItem()));

		///// item stacks
		ComparableStack comp = new ComparableStack(stack).makeSingular();
		if(stackMap.containsKey(comp))
			chronological.add(stackMap.get(comp));

		List<HazardEntry> entries = new ArrayList<>();

		for(HazardTransformerBase trafo : trafos) {
			trafo.transformPre(stack, entries);
		}

		int mutex = 0;

		for(HazardData data : chronological) {
			// an override clears whatever piled up before it
			if(data.doesOverride)
				entries.clear();

			if((data.getMutex() & mutex) == 0) {
				entries.addAll(data.entries);
				mutex = mutex | data.getMutex();
			}
		}

		for(HazardTransformerBase trafo : trafos) {
			trafo.transformPost(stack, entries);
		}

		return entries;
	}

	public static float getHazardLevelFromStack(ItemStack stack, HazardTypeBase hazard) {
		List<HazardEntry> entries = getHazardsFromStack(stack);

		for(HazardEntry entry : entries) {
			if(entry.type == hazard) {
				return HazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods);
			}
		}

		return 0F;
	}

	/** Applies every hazard on this item to whoever is holding it */
	public static void applyHazards(ItemStack stack, EntityLivingBase entity) {
		List<HazardEntry> hazards = getHazardsFromStack(stack);

		for(HazardEntry hazard : hazards) {
			hazard.applyHazard(stack, entity);
		}
	}

	/** Walks everything carried, armour slots included */
	public static void updatePlayerInventory(EntityPlayer player) {

		NonNullList<ItemStack> main = player.inventory.mainInventory;
		for(int i = 0; i < main.size(); i++) {

			ItemStack stack = main.get(i);
			if(stack != null && !stack.isEmpty()) {
				applyHazards(stack, player);

				if(stack.getCount() == 0) {
					main.set(i, ItemStack.EMPTY);
				}
			}
		}

		for(ItemStack stack : player.inventory.armorInventory) {
			if(stack != null && !stack.isEmpty()) {
				applyHazards(stack, player);
			}
		}
	}

	public static void updateLivingInventory(EntityLivingBase entity) {

		ItemStack held = entity.getHeldItemMainhand();
		if(held != null && !held.isEmpty()) {
			applyHazards(held, entity);
		}

		for(EntityEquipmentSlot slot : CARRIED_SLOTS) {
			ItemStack stack = entity.getItemStackFromSlot(slot);

			if(stack != null && !stack.isEmpty()) {
				applyHazards(stack, entity);
			}
		}
	}

	public static void updateDroppedItem(EntityItem entity) {

		ItemStack stack = entity.getItem();

		if(entity.isDead || stack == null || stack.getItem() == null || stack.getCount() <= 0) return;

		List<HazardEntry> hazards = getHazardsFromStack(stack);
		for(HazardEntry entry : hazards) {
			entry.type.updateEntity(entity, HazardModifier.evalAllModifiers(stack, null, entry.baseLevel, entry.mods));
		}
	}

	@SideOnly(Side.CLIENT)
	public static void addFullTooltip(ItemStack stack, EntityPlayer player, List<String> list) {

		List<HazardEntry> hazards = getHazardsFromStack(stack);

		for(HazardEntry hazard : hazards) {
			hazard.type.addHazardInformation(player, list, hazard.baseLevel, stack, hazard.mods);
		}
	}
}
