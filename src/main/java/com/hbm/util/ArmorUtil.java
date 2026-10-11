package com.hbm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.HazmatRegistry;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;

import api.hbm.hazard.HazardClass;
import api.hbm.net.Tuple.Pair;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import api.hbm.item.IGasMask;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/** Armor/protection odd jobs. Checks always go through ArmorRegistry; no gear is hard-coded here. */
public final class ArmorUtil {

	private ArmorUtil() { }

	private static EntityEquipmentSlot[] ARMOR = {
			EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET
	};

	/** slot uses the original mod's 0..3 notation (head/chest/legs/feet) */
	public static boolean checkArmorNull(EntityLivingBase entity, int slot) {
		if(slot < 0 || slot >= ARMOR.length) return true;
		ItemStack stack = entity.getItemStackFromSlot(ARMOR[slot]);
		return stack == null || stack.isEmpty();
	}

	/**
	 * Full-protection check: all four pieces must block the target hazard.
	 * The proper replacement for the old hard-coded gear lists — hazmat sets declare what they block in the registry; this only asks.
	 */
	public static boolean checkForHazmat(EntityLivingBase entity) {
		for(int slot = 0; slot < ARMOR.length; slot++) {
			if(checkArmorNull(entity, slot)) return false;
		}
		return ArmorRegistry.hasAllProtection(entity, 0, HazardClass.GAS_LUNG)
				&& ArmorRegistry.hasAllProtection(entity, 1, HazardClass.GAS_LUNG)
				&& ArmorRegistry.hasAllProtection(entity, 2, HazardClass.GAS_LUNG)
				&& ArmorRegistry.hasAllProtection(entity, 3, HazardClass.GAS_LUNG);
	}

	/**
	 * Drains the worn mask's filter durability; no mask, or no filter installed, means nothing happens.
	 *
	 * The second argument is the **damage amount**, not a slot — the old same-named method had exactly this shape (always reading the helmet and passing
	 * the damage down to {@link #damageGasMaskFilter(ItemStack, int)}). An earlier version of this file treated it as a slot with damage hard-set to 1,
	 * while the four call sites (HazardTypeCoal / HazardTypeAsbestos / ContaminationUtil / FT_Toxin) all pass damage numbers,
	 * so passing 3 would touch the FEET piece and anything larger would return out of bounds — coal dust and asbestos would never wear the filter.
	 * Delegating instead of IGasMask.damageFilter also follows the old version: that interface method was itself just a forward here.
	 */
	public static void damageGasMaskFilter(EntityLivingBase entity, int damage) {

		ItemStack mask = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if(mask == null || mask.isEmpty()) return;

		if(!(mask.getItem() instanceof IGasMask) && ArmorModHandler.hasMods(mask)) {

			ItemStack mods[] = ArmorModHandler.pryMods(mask);
			ItemStack mod = mods[ArmorModHandler.helmet_only];

			if(mod != null && !mod.isEmpty() && mod.getItem() instanceof IGasMask)
				mask = mod;
		}

		damageGasMaskFilter(mask, damage);
	}

	/**
	 * The key the filter is stored under in the mask's NBT.
	 * The stray K in the constant name (FILTERK) is an old typo; the key value must not change — moving it makes saved filters vanish into thin air.
	 */
	public static final String FILTERK_KEY = "hfrFilter";

	/** Screwing a filter in: overwrites the old one outright; whether the old one returns to the player is the caller's call */
	public static void installGasMaskFilter(ItemStack mask, ItemStack filter) {

		if(mask == null || mask.isEmpty() || filter == null || filter.isEmpty())
			return;

		NBTTagCompound tag = mask.getTagCompound();

		if(tag == null) {
			tag = new NBTTagCompound();
			mask.setTagCompound(tag);
		}

		NBTTagCompound attach = new NBTTagCompound();
		filter.writeToNBT(attach);
		tag.setTag(FILTERK_KEY, attach);
	}

	/** Pulls the filter out, not returned to the player */
	public static void removeFilter(ItemStack mask) {

		if(mask == null || mask.isEmpty() || !mask.hasTagCompound())
			return;

		mask.getTagCompound().removeTag(FILTERK_KEY);
	}

	/**
	 * Reads the mask's filter, null when none installed.
	 *
	 * The mask chain keeps IGasMask's "null when absent" convention, a different check from ArmorModHandler's EMPTY-prefilled
	 * mod table — don't mix them: isEmpty() here NPEs, and null there silently drops pieces.
	 */
	public static ItemStack getGasMaskFilter(ItemStack mask) {

		if(mask == null || mask.isEmpty() || !mask.hasTagCompound())
			return null;

		NBTTagCompound attach = mask.getTagCompound().getCompoundTag(FILTERK_KEY);
		if(attach.getString("id").isEmpty())
			return null;

		return new ItemStack(attach);
	}

	/**
	 * Zeroes the "ticks spent airborne" counter. Jetpack takeoff and landing must report through this,
	 * or the server's flight check kicks legitimate flyers as cheaters.
	 */
	public static void resetFlightTime(EntityPlayer player) {

		if(!(player instanceof EntityPlayerMP))
			return;

		ReflectionHelper.setPrivateValue(NetHandlerPlayServer.class, ((EntityPlayerMP) player).connection, 0, "floatingTickCount", "field_147365_f");
	}

	/**
	 * The mask tooltip: first reports whether a filter is installed, then appends the filter's own description when it is.
	 *
	 * The whole method can only live client-side: ITooltipFlag is @SideOnly(Side.CLIENT) in this version,
	 * and a dedicated server loading it would trip the side check outright.
	 *
	 * The old version called Item.addInformation directly; that method is protected now, so this goes through
	 * ItemStack.getTooltip(player, flag). A bonus: Forge's ItemTooltipEvent fires inside it,
	 * so other mods' additions to the filter description still show; the cost is the first line being the item name (already printed above), hence appending from line two.
	 */
	@SideOnly(Side.CLIENT)
	public static void addGasMaskTooltip(ItemStack mask, EntityPlayer player, List<String> list, boolean ext) {

		if(mask == null || mask.isEmpty() || !(mask.getItem() instanceof IGasMask))
			return;

		ItemStack filter = ((IGasMask) mask.getItem()).getFilter(mask, player);

		if(filter == null) {
			list.add(TextFormatting.RED + "No filter installed!");
			return;
		}

		list.add(TextFormatting.GOLD + "Installed filter:");

		int used = filter.getItemDamage();
		int max = filter.getMaxDamage();
		String append = max > 0 ? " (" + ((max - used) * 100 / max) + "%)" : "";
		list.add("  " + filter.getDisplayName() + append);

		List<String> lore = filter.getTooltip(player, ext ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);

		for(int i = 1; i < lore.size(); i++)
			list.add(TextFormatting.YELLOW + "  " + lore.get(i));
	}

	/*
	 * The "full set counts" checks below: the four pieces must be exactly that set; one short and it doesn't count.
	 * These go by gear lists, not resistance thresholds — thresholds look equivalent but would let the 99% tier into heavy hazmat.
	 */

	/** Heavy hazmat: one of PAA / neutralizer / euphemium / RPA / FAU / DNS */
	public static boolean checkForHaz2(EntityLivingBase entity) {

		return checkSet(entity, ModItems.hazmat_paa_helmet, ModItems.hazmat_paa_plate, ModItems.hazmat_paa_legs, ModItems.hazmat_paa_boots)
				|| checkSet(entity, ModItems.liquidator_helmet, ModItems.liquidator_plate, ModItems.liquidator_legs, ModItems.liquidator_boots)
				|| checkSet(entity, ModItems.euphemium_helmet, ModItems.euphemium_plate, ModItems.euphemium_legs, ModItems.euphemium_boots)
				|| checkSet(entity, ModItems.rpa_helmet, ModItems.rpa_plate, ModItems.rpa_legs, ModItems.rpa_boots)
				|| checkSet(entity, ModItems.fau_helmet, ModItems.fau_plate, ModItems.fau_legs, ModItems.fau_boots)
				|| checkSet(entity, ModItems.dns_helmet, ModItems.dns_plate, ModItems.dns_legs, ModItems.dns_boots);
	}

	/** Digamma protection: a full FAU/DNS set, or stabilizers on board */
	public static boolean checkForDigamma(EntityLivingBase entity) {

		if(checkSet(entity, ModItems.fau_helmet, ModItems.fau_plate, ModItems.fau_legs, ModItems.fau_boots)) return true;
		if(checkSet(entity, ModItems.dns_helmet, ModItems.dns_plate, ModItems.dns_legs, ModItems.dns_boots)) return true;

		return entity.isPotionActive(HbmPotion.stability);
	}

	/**
	 * Faraday cage: all four pieces conductive counts as worn; mixing in one insulator somehow completes grounding? No — the old rule is "all metal".
	 * A piece wearing lead cladding (hfr_cladding) counts as metal too.
	 */
	public static boolean checkForFaraday(EntityLivingBase entity) {

		for(EntityEquipmentSlot slot : ARMOR) {
			ItemStack stack = entity.getItemStackFromSlot(slot);
			if(stack == null || stack.isEmpty() || !isFaradayArmor(stack)) return false;
		}

		return true;
	}

	public static boolean isFaradayArmor(ItemStack item) {

		String name = item.getItem().getUnlocalizedName().toLowerCase(Locale.US);

		for(String metal : METALS) {
			if(name.contains(metal)) return true;
		}

		return HazmatRegistry.getCladding(item) > 0;
	}

	/** This list deliberately mixes conductive metals with the rubber/HEV "insulating but hazmat-counting" specials */
	public static final String[] METALS = {
			"chainmail", "iron", "silver", "gold", "platinum", "tin", "lead",
			"liquidator", "schrabidium", "euphemium", "steel", "cmb", "titanium",
			"alloy", "copper", "bronze", "electrum", "t45", "t51", "bj", "starmetal",
			"hazmat", "rubber", "hev", "ajr", "rpa", "spacesuit"
	};

	private static boolean checkSet(EntityLivingBase entity, Item head, Item chest, Item legs, Item feet) {
		return matches(entity, EntityEquipmentSlot.HEAD, head) && matches(entity, EntityEquipmentSlot.CHEST, chest)
				&& matches(entity, EntityEquipmentSlot.LEGS, legs) && matches(entity, EntityEquipmentSlot.FEET, feet);
	}

	private static boolean matches(EntityLivingBase entity, EntityEquipmentSlot slot, Item expected) {
		ItemStack stack = entity.getItemStackFromSlot(slot);
		return stack != null && !stack.isEmpty() && stack.getItem() == expected;
	}

	/** Four pieces exactly matching the passed set, arguments ordered head/chest/legs/feet (equivalent to the old 3-i after switching to the enum slot table) */
	public static boolean checkArmor(EntityLivingBase entity, Item... armor) {

		for(int i = 0; i < ARMOR.length; i++) {
			if(!checkArmorPiece(entity, armor[i], i)) return false;
		}

		return true;
	}

	/** Single-piece comparison, slot in 0..3 head/chest/legs/feet notation, same numbering as checkArmorNull */
	public static boolean checkArmorPiece(EntityLivingBase entity, Item armor, int slot) {
		if(checkArmorNull(entity, slot)) return false;
		return matches(entity, ARMOR[slot], armor);
	}

	/**
	 * The hazmat sets' two class tables, for gear classes to pass as arguments (`setHazardClass(ArmorUtil.FULL_PACKAGE)`),
	 * so each armor doesn't copy the list and miss a tier — one missed line, one unblocked hazard.
	 *
	 * The two differ only in the `LIGHT` tier: FULL_NO_LIGHT excludes the bright-flash blinding — the no light in its name means exactly that,
	 * nothing to do with lighting, brightness or rendering.
	 */
	public static final HazardClass[] FULL_NO_LIGHT = { HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.SAND };
	public static final HazardClass[] FULL_PACKAGE = { HazardClass.PARTICLE_COARSE, HazardClass.PARTICLE_FINE, HazardClass.GAS_LUNG, HazardClass.BACTERIA, HazardClass.GAS_BLISTERING, HazardClass.GAS_MONOXIDE, HazardClass.LIGHT, HazardClass.SAND };

	/**
	 * The externally mounted hazmat list: gear classes stuff in a pair at construction (item → the hazard classes it blocks),
	 * and register() funnels it into ArmorRegistry. Calling ArmorRegistry directly would work, but registration
	 * timing would scatter — one step late and the table has already been read once.
	 */
	public static List<Pair<Item, HazardClass[]>> external = new ArrayList();

	/** Consumes the external list. Must wait until item fields are filled, hence init-phase, same as the hazmat table */
	public static void register() {

		for(Pair<Item, HazardClass[]> pair : external) {
			if(pair.getKey() == null || pair.getValue() == null) continue;
			ArmorRegistry.registerHazard(pair.getKey(), pair.getValue());
		}

		LoggingUtil.info("[hbm] hazmat addon table registered: {} items", external.size());
	}

	/**
	 * The robe tier: four robes, stabilizers on board, and every worn modded armor piece carrying iron cladding,
	 * with max HP already down below 3 (digamma's doing).
	 * The third condition isn't optional — checking only the first two would mark "robes plus someone else's mods" as protected.
	 */
	public static boolean checkForDigamma2(EntityLivingBase entity) {

		if(!checkArmor(entity, ModItems.robes_helmet, ModItems.robes_plate, ModItems.robes_legs, ModItems.robes_boots)) return false;
		if(!entity.isPotionActive(HbmPotion.stability)) return false;

		for(EntityEquipmentSlot slot : ARMOR) {
			ItemStack armor = entity.getItemStackFromSlot(slot);
			if(armor == null || armor.isEmpty() || !ArmorModHandler.hasMods(armor)) continue;

			ItemStack[] mods = ArmorModHandler.pryMods(armor);
			// This version's mod arrays come prefilled with EMPTY (the old ones held null), so emptiness checks go through isEmpty()
			if(mods[ArmorModHandler.cladding].isEmpty() || mods[ArmorModHandler.cladding].getItem() != ModItems.cladding_iron)
				return false;
		}

		return entity.getMaxHealth() < 3;
	}

	/**
	 * Wears down the given slot's armor durability, removing it from the body when it breaks.
	 *
	 * slot matches the rest of this file: 0..3 head/chest/legs/feet. The old version used `getEquipmentInSlot(slot + 1)`
	 * integer slots (1=feet...4=head); no integer entry exists in this version, so everything goes through EntityEquipmentSlot.
	 * Every call site in the tree loops 0..3 anyway (barbed wire, chaos explosions — "wear the whole body down"), so tier order doesn't affect semantics.
	 */
	public static void damageSuit(EntityLivingBase entity, int slot, int amount) {

		if(slot < 0 || slot >= ARMOR.length) return;

		ItemStack stack = entity.getItemStackFromSlot(ARMOR[slot]);
		if(stack == null || stack.isEmpty()) return;

		stack.damageItem(amount, entity);

		if(stack.isEmpty())
			entity.setItemStackToSlot(ARMOR[slot], ItemStack.EMPTY);
	}

	/**
	 * The mask's filter, with mod fallback: first this stack's own NBT, then the real mask sitting in an armor mod slot.
	 * Returns follow this file's mask-chain "null when absent" convention, distinct from ArmorModHandler's EMPTY-prefilled table.
	 */
	public static ItemStack getGasMaskFilterRecursively(ItemStack mask, EntityLivingBase entity) {

		ItemStack filter = getGasMaskFilter(mask);

		if(filter == null && ArmorModHandler.hasMods(mask)) {

			ItemStack mods[] = ArmorModHandler.pryMods(mask);
			ItemStack mod = mods[ArmorModHandler.helmet_only];

			if(mod != null && !mod.isEmpty() && mod.getItem() instanceof IGasMask)
				filter = ((IGasMask) mod.getItem()).getFilter(mod, entity);
		}

		return filter;
	}

	/** Drains the given mask stack's filter durability; past its limit the filter comes off rather than the mask blocking with a dead one */
	public static void damageGasMaskFilter(ItemStack mask, int damage) {

		ItemStack filter = getGasMaskFilter(mask);

		if(filter == null && ArmorModHandler.hasMods(mask)) {
			ItemStack mods[] = ArmorModHandler.pryMods(mask);
			ItemStack mod = mods[ArmorModHandler.helmet_only];

			if(mod != null && !mod.isEmpty() && mod.getItem() instanceof IGasMask)
				filter = getGasMaskFilter(mod);
		}

		if(filter == null || filter.getMaxDamage() == 0)
			return;

		filter.setItemDamage(filter.getItemDamage() + damage);

		if(filter.getItemDamage() > filter.getMaxDamage())
			removeFilter(mask);
		else
			installGasMaskFilter(mask, filter);
	}

	/** A mask is genuinely worn but carries no filter — separates "wearing it for nothing" from "not wearing one at all" */
	public static boolean isWearingEmptyMask(EntityPlayer player) {

		ItemStack mask = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

		if(mask == null || mask.isEmpty())
			return false;

		if(mask.getItem() instanceof IGasMask)
			return getGasMaskFilter(mask) == null;

		ItemStack mod = ArmorModHandler.pryMods(mask)[ArmorModHandler.helmet_only];

		if(mod != null && !mod.isEmpty() && mod.getItem() instanceof IGasMask)
			return getGasMaskFilter(mod) == null;

		return false;
	}
}
