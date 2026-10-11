package com.hbm.items.registry;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ItemAmmoEnums.Ammo240Shell;
import com.hbm.items.ItemAmmoEnums.AmmoFireExt;
import com.hbm.items.ItemAmmoEnums.AmmoMisc;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.GunB92Cell;
import com.hbm.items.weapon.ItemAmmo;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.grenade.ItemGrenadeShell;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;

import net.minecraft.item.Item;

/**
 * The weapons subdomain registration unit.
 *
 * Only the ballistic missile body has landed — it extends `ItemCustomLore` and reads only type/fuel enums, the one
 * self-contained piece of this slice. The gun family (`ItemGunBase`/`WeaponSpecial`/the Sedna set) waits on A2's `ability.*`
 * and `NTMSounds`, plus A4's ballistic entities; add them here once those land.
 */
public class ItemsWeapon implements IRegistrar {

	private final List<Item> created = new ArrayList<>();

	@Override
	public String id() {
		return "items.weapon";
	}

	@Override
	public void preInit() {
		created.add(ModItems.missile_generic = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.V2, ItemMissile.MissileTier.TIER1).setUnlocalizedName("missile_generic").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_generic"));
		created.add(ModItems.missile_anti_ballistic = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.ABM, ItemMissile.MissileTier.TIER1).setUnlocalizedName("missile_anti_ballistic").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_anti_ballistic"));
		created.add(ModItems.missile_incendiary = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.V2, ItemMissile.MissileTier.TIER1).setUnlocalizedName("missile_incendiary").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_incendiary"));
		created.add(ModItems.missile_cluster = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.V2, ItemMissile.MissileTier.TIER1).setUnlocalizedName("missile_cluster").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_cluster"));
		created.add(ModItems.missile_buster = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.V2, ItemMissile.MissileTier.TIER1).setUnlocalizedName("missile_buster").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_buster"));
		created.add(ModItems.missile_decoy = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.V2, ItemMissile.MissileTier.TIER1).setUnlocalizedName("missile_decoy").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_decoy"));
		created.add(ModItems.missile_strong = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.STRONG, ItemMissile.MissileTier.TIER2).setUnlocalizedName("missile_strong").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_strong"));
		created.add(ModItems.missile_incendiary_strong = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.STRONG, ItemMissile.MissileTier.TIER2).setUnlocalizedName("missile_incendiary_strong").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_incendiary_strong"));
		created.add(ModItems.missile_cluster_strong = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.STRONG, ItemMissile.MissileTier.TIER2).setUnlocalizedName("missile_cluster_strong").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_cluster_strong"));
		created.add(ModItems.missile_buster_strong = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.STRONG, ItemMissile.MissileTier.TIER2).setUnlocalizedName("missile_buster_strong").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_buster_strong"));
		created.add(ModItems.missile_emp_strong = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.STRONG, ItemMissile.MissileTier.TIER2).setUnlocalizedName("missile_emp_strong").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_emp_strong"));
		created.add(ModItems.missile_burst = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.HUGE, ItemMissile.MissileTier.TIER3).setUnlocalizedName("missile_burst").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_burst"));
		created.add(ModItems.missile_inferno = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.HUGE, ItemMissile.MissileTier.TIER3).setUnlocalizedName("missile_inferno").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_inferno"));
		created.add(ModItems.missile_rain = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.HUGE, ItemMissile.MissileTier.TIER3).setUnlocalizedName("missile_rain").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_rain"));
		created.add(ModItems.missile_drill = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.HUGE, ItemMissile.MissileTier.TIER3).setUnlocalizedName("missile_drill").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_drill"));
		created.add(ModItems.missile_nuclear = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.ATLAS, ItemMissile.MissileTier.TIER4).setUnlocalizedName("missile_nuclear").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_nuclear"));
		created.add(ModItems.missile_nuclear_cluster = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.ATLAS, ItemMissile.MissileTier.TIER4).setUnlocalizedName("missile_nuclear_cluster").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_nuclear_cluster"));
		created.add(ModItems.missile_volcano = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.ATLAS, ItemMissile.MissileTier.TIER4).setUnlocalizedName("missile_volcano").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_volcano"));
		created.add(ModItems.missile_doomsday = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.ATLAS, ItemMissile.MissileTier.TIER4).setUnlocalizedName("missile_doomsday").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_doomsday"));
		created.add(ModItems.missile_doomsday_rusted = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.ATLAS, ItemMissile.MissileTier.TIER4).notLaunchable().setUnlocalizedName("missile_doomsday_rusted").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_doomsday_rusted"));
		created.add(ModItems.missile_taint = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.MICRO, ItemMissile.MissileTier.TIER0).setUnlocalizedName("missile_taint").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_taint"));
		created.add(ModItems.missile_micro = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.MICRO, ItemMissile.MissileTier.TIER0).setUnlocalizedName("missile_micro").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_micro"));
		created.add(ModItems.missile_bhole = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.MICRO, ItemMissile.MissileTier.TIER0).setUnlocalizedName("missile_bhole").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_bhole"));
		created.add(ModItems.missile_schrabidium = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.MICRO, ItemMissile.MissileTier.TIER0).setUnlocalizedName("missile_schrabidium").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_schrabidium"));
		created.add(ModItems.missile_emp = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.MICRO, ItemMissile.MissileTier.TIER0).setUnlocalizedName("missile_emp").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_emp"));
		created.add(ModItems.missile_shuttle = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.OTHER, ItemMissile.MissileTier.TIER3, ItemMissile.MissileFuel.KEROSENE_PEROXIDE).setUnlocalizedName("missile_shuttle").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_shuttle"));
		created.add(ModItems.missile_stealth = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.STRONG, ItemMissile.MissileTier.TIER1).setUnlocalizedName("missile_stealth").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_stealth"));
		created.add(ModItems.missile_test = HbmRegistry.item(new ItemMissile(ItemMissile.MissileFormFactor.MICRO, ItemMissile.MissileTier.TIER0).setUnlocalizedName("missile_test").setMaxStackSize(1).setCreativeTab(null), "missile_test"));
		created.add(ModItems.ammo_shell = (ItemEnumMulti) HbmRegistry.item(new ItemAmmo(Ammo240Shell.class).setCreativeTab(ModItems.weaponTab).setUnlocalizedName("ammo_shell"), "ammo_shell"));
		created.add(ModItems.ammo_fireext = (ItemEnumMulti) HbmRegistry.item(new ItemAmmo(AmmoFireExt.class).setCreativeTab(ModItems.weaponTab).setUnlocalizedName("ammo_fireext"), "ammo_fireext"));
		created.add(ModItems.ammo_misc = (ItemEnumMulti) HbmRegistry.item(new ItemAmmo(AmmoMisc.class).setUnlocalizedName("ammo_misc"), "ammo_misc"));

		// The grenades' tiers live in their own enum; the B92 battery cell is a "meta 1 = charged" item sharing one texture
		created.add(ModItems.grenade_shell = HbmRegistry.item(new ItemGrenadeShell().setUnlocalizedName("grenade_shell").setCreativeTab(ModItems.weaponTab), "grenade_shell"));
		created.add(ModItems.gun_b92_ammo = HbmRegistry.item(new GunB92Cell().setUnlocalizedName("gun_b92_ammo").setMaxStackSize(1).setCreativeTab(ModItems.weaponTab), "gun_b92_ammo"));
	}

	@Override
	public void registerModels() {
		for(Item item : created) ItemModels.registerAll(item);

		// the charged tier (meta 1) shares the empty tier's texture, and registerAll only reaches 0
		ItemModels.registerRange(ModItems.gun_b92_ammo, 2);
	}
}
