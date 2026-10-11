package com.hbm.items.registry;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ItemGenericPart;
import com.hbm.items.ModItems;
import com.hbm.items.bomb.ItemFleija;
import com.hbm.items.bomb.ItemN2;
import com.hbm.items.bomb.ItemSolinium;
import com.hbm.items.machine.ItemCassette;
import com.hbm.items.special.ItemDigamma;
import com.hbm.items.special.ItemModRecord;
import com.hbm.items.special.ItemSimpleConsumable;
import com.hbm.items.special.ItemAMSCore;
import com.hbm.items.special.ItemBedrockOre;
import com.hbm.items.special.ItemByproduct;
import com.hbm.items.special.ItemCigarette;
import com.hbm.items.special.ItemCircuitStarComponent;
import com.hbm.items.special.ItemFusionShield;
import com.hbm.items.special.ItemHot;
import com.hbm.items.special.ItemHotDusted;
import com.hbm.items.special.ItemKitCustom;
import com.hbm.items.special.ItemNuclearWaste;
import com.hbm.items.special.ItemSyringe;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemDemonCore;
import com.hbm.items.special.ItemDrop;
import com.hbm.items.special.ItemExpensive;
import com.hbm.items.special.ItemPlasticScrap;
import com.hbm.items.special.ItemPlasticScrap.ScrapType;
import com.hbm.items.special.ItemRag;
import com.hbm.items.special.ItemSchraranium;
import com.hbm.items.special.ItemSoyuz;
import com.hbm.items.special.ItemStarmetal;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The special items registration unit. Their common thread: "carry their own little logic" — tinting, per-meta textures,
 * custom display names — while leaning on no machine or tool subsystem.
 *
 * The twelve bedrock ore pieces (bedrock → centrifuge → washed → separated → purified → nitrated → nitro crystal → deep-washed → roasted → enriched) are one whole line:
 * miss one link and the processing chain breaks, so they land together, unsplit.
 */
public class ItemsSpecial implements IRegistrar {

	private final List<Item> created = new ArrayList<>();

	@Override
	public String id() {
		return "items.special";
	}

	@Override
	public void preInit() {
		created.add(ModItems.ingot_starmetal = HbmRegistry.item(new ItemStarmetal().setUnlocalizedName("ingot_starmetal").setCreativeTab(ModItems.partsTab), "ingot_starmetal"));
		created.add(ModItems.ingot_schraranium = HbmRegistry.item(new ItemSchraranium().setUnlocalizedName("ingot_schraranium").setCreativeTab(ModItems.partsTab), "ingot_schraranium"));
		created.add(ModItems.ore_byproduct = HbmRegistry.item(new ItemByproduct().setUnlocalizedName("ore_byproduct").setCreativeTab(null), "ore_byproduct"));
		created.add(ModItems.scrap_plastic = HbmRegistry.item(new ItemPlasticScrap().setUnlocalizedName("scrap_plastic"), "scrap_plastic"));

		created.add(ModItems.ore_bedrock = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_bedrock").setCreativeTab(null), "ore_bedrock"));
		created.add(ModItems.ore_centrifuged = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_centrifuged").setCreativeTab(null), "ore_centrifuged"));
		created.add(ModItems.ore_cleaned = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_cleaned").setCreativeTab(null), "ore_cleaned"));
		created.add(ModItems.ore_separated = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_separated").setCreativeTab(null), "ore_separated"));
		created.add(ModItems.ore_purified = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_purified").setCreativeTab(null), "ore_purified"));
		created.add(ModItems.ore_nitrated = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_nitrated").setCreativeTab(null), "ore_nitrated"));
		created.add(ModItems.ore_nitrocrystalline = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_nitrocrystalline").setCreativeTab(null), "ore_nitrocrystalline"));
		created.add(ModItems.ore_deepcleaned = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_deepcleaned").setCreativeTab(null), "ore_deepcleaned"));
		created.add(ModItems.ore_seared = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_seared").setCreativeTab(null), "ore_seared"));
		created.add(ModItems.ore_enriched = HbmRegistry.item(new ItemBedrockOre().setUnlocalizedName("ore_enriched").setCreativeTab(null), "ore_enriched"));

		created.add(ModItems.ingot_meteorite = HbmRegistry.item(new ItemHot(200).setUnlocalizedName("ingot_meteorite").setCreativeTab(ModItems.partsTab), "ingot_meteorite"));
		created.add(ModItems.ingot_meteorite_forged = HbmRegistry.item(new ItemHot(200).setUnlocalizedName("ingot_meteorite_forged").setCreativeTab(ModItems.partsTab), "ingot_meteorite_forged"));
		created.add(ModItems.blade_meteorite = HbmRegistry.item(new ItemHot(200).setUnlocalizedName("blade_meteorite").setCreativeTab(ModItems.partsTab), "blade_meteorite"));
		created.add(ModItems.ingot_chainsteel = HbmRegistry.item(new ItemHot(100).setUnlocalizedName("ingot_chainsteel").setCreativeTab(ModItems.partsTab), "ingot_chainsteel"));
		created.add(ModItems.ingot_steel_dusted = HbmRegistry.item(new ItemHotDusted(200).setUnlocalizedName("ingot_steel_dusted").setCreativeTab(ModItems.partsTab), "ingot_steel_dusted"));

		created.add(ModItems.fusion_shield_tungsten = HbmRegistry.item(new ItemFusionShield(60 * 60 * 60 * 5, 3500).setUnlocalizedName("fusion_shield_tungsten").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "fusion_shield_tungsten"));
		created.add(ModItems.fusion_shield_desh = HbmRegistry.item(new ItemFusionShield(60 * 60 * 60 * 10, 4500).setUnlocalizedName("fusion_shield_desh").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "fusion_shield_desh"));
		created.add(ModItems.fusion_shield_chlorophyte = HbmRegistry.item(new ItemFusionShield(60 * 60 * 60 * 15, 9000).setUnlocalizedName("fusion_shield_chlorophyte").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "fusion_shield_chlorophyte"));
		created.add(ModItems.fusion_shield_vaporwave = HbmRegistry.item(new ItemFusionShield(60 * 60 * 60 * 10, 1916169).setUnlocalizedName("fusion_shield_vaporwave").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "fusion_shield_vaporwave"));

		created.add(ModItems.rag = HbmRegistry.item(new ItemRag().setUnlocalizedName("rag").setCreativeTab(ModItems.partsTab), "rag"));
		// The chain's final return type is ItemBase; narrowing back to ItemEnumMulti needs an explicit cast (that's how the fields are declared)
		created.add(ModItems.circuit_star_piece = (ItemEnumMulti) HbmRegistry.item(new ItemEnumMulti(ScrapType.class, true, true).setUnlocalizedName("circuit_star_piece").setCreativeTab(null), "circuit_star_piece"));
		created.add(ModItems.circuit_star_component = (ItemEnumMulti) HbmRegistry.item(new ItemCircuitStarComponent().setUnlocalizedName("circuit_star_component").setCreativeTab(null), "circuit_star_component"));

		created.add(ModItems.nuclear_waste_short = HbmRegistry.item(new ItemWasteShort().setUnlocalizedName("nuclear_waste_short").setCreativeTab(ModItems.partsTab), "nuclear_waste_short"));
		created.add(ModItems.nuclear_waste_short_tiny = HbmRegistry.item(new ItemWasteShort().setUnlocalizedName("nuclear_waste_short_tiny").setCreativeTab(ModItems.partsTab), "nuclear_waste_short_tiny"));
		created.add(ModItems.nuclear_waste_short_depleted = HbmRegistry.item(new ItemWasteShort().setUnlocalizedName("nuclear_waste_short_depleted").setCreativeTab(ModItems.partsTab), "nuclear_waste_short_depleted"));
		created.add(ModItems.nuclear_waste_short_depleted_tiny = HbmRegistry.item(new ItemWasteShort().setUnlocalizedName("nuclear_waste_short_depleted_tiny").setCreativeTab(ModItems.partsTab), "nuclear_waste_short_depleted_tiny"));

		// Long-half-life and regular nuclear waste: doesn't melt on the ground, can't be picked up — carried by ItemNuclearWaste's custom drop entity
		created.add(ModItems.nuclear_waste_long = HbmRegistry.item(new ItemWasteLong().setUnlocalizedName("nuclear_waste_long").setCreativeTab(ModItems.partsTab), "nuclear_waste_long"));
		created.add(ModItems.nuclear_waste_long_tiny = HbmRegistry.item(new ItemWasteLong().setUnlocalizedName("nuclear_waste_long_tiny").setCreativeTab(ModItems.partsTab), "nuclear_waste_long_tiny"));
		created.add(ModItems.nuclear_waste_long_depleted = HbmRegistry.item(new ItemWasteLong().setUnlocalizedName("nuclear_waste_long_depleted").setCreativeTab(ModItems.partsTab), "nuclear_waste_long_depleted"));
		created.add(ModItems.nuclear_waste_long_depleted_tiny = HbmRegistry.item(new ItemWasteLong().setUnlocalizedName("nuclear_waste_long_depleted_tiny").setCreativeTab(ModItems.partsTab), "nuclear_waste_long_depleted_tiny"));

		created.add(ModItems.trinitite = HbmRegistry.item(new ItemNuclearWaste().setUnlocalizedName("trinitite").setCreativeTab(ModItems.partsTab), "trinitite"));
		created.add(ModItems.nuclear_waste = HbmRegistry.item(new ItemNuclearWaste().setUnlocalizedName("nuclear_waste").setCreativeTab(ModItems.partsTab), "nuclear_waste"));
		created.add(ModItems.nuclear_waste_tiny = HbmRegistry.item(new ItemNuclearWaste().setUnlocalizedName("nuclear_waste_tiny").setCreativeTab(ModItems.partsTab), "nuclear_waste_tiny"));
		created.add(ModItems.nuclear_waste_vitrified = HbmRegistry.item(new ItemNuclearWaste().setUnlocalizedName("nuclear_waste_vitrified").setCreativeTab(ModItems.partsTab), "nuclear_waste_vitrified"));
		created.add(ModItems.nuclear_waste_vitrified_tiny = HbmRegistry.item(new ItemNuclearWaste().setUnlocalizedName("nuclear_waste_vitrified_tiny").setCreativeTab(ModItems.partsTab), "nuclear_waste_vitrified_tiny"));

		// The "explode on landing" family (ItemDrop): the old four setTextureName aliases find their models by registry name here,
		// and the model jsons already point at the aliases (black_hole→singularity_4 etc.), so setTextureName isn't ported
		created.add(ModItems.cell_antimatter = HbmRegistry.item(new ItemDrop().setUnlocalizedName("cell_antimatter").setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.cell_empty), "cell_antimatter"));
		created.add(ModItems.cell_anti_schrabidium = HbmRegistry.item(new ItemDrop().setUnlocalizedName("cell_anti_schrabidium").setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.cell_empty), "cell_anti_schrabidium"));
		created.add(ModItems.pellet_antimatter = HbmRegistry.item(new ItemDrop().setUnlocalizedName("pellet_antimatter").setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.cell_empty), "pellet_antimatter"));
		created.add(ModItems.singularity = HbmRegistry.item(new ItemDrop().setUnlocalizedName("singularity").setMaxStackSize(1).setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.nuclear_waste), "singularity"));
		created.add(ModItems.singularity_counter_resonant = HbmRegistry.item(new ItemDrop().setUnlocalizedName("singularity_counter_resonant").setMaxStackSize(1).setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.nuclear_waste), "singularity_counter_resonant"));
		created.add(ModItems.singularity_super_heated = HbmRegistry.item(new ItemDrop().setUnlocalizedName("singularity_super_heated").setMaxStackSize(1).setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.nuclear_waste), "singularity_super_heated"));
		created.add(ModItems.singularity_spark = HbmRegistry.item(new ItemDrop().setUnlocalizedName("singularity_spark").setMaxStackSize(1).setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.nuclear_waste), "singularity_spark"));
		created.add(ModItems.black_hole = HbmRegistry.item(new ItemDrop().setUnlocalizedName("black_hole").setMaxStackSize(1).setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.nuclear_waste), "black_hole"));
		created.add(ModItems.crystal_xen = HbmRegistry.item(new ItemDrop().setUnlocalizedName("crystal_xen").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "crystal_xen"));
		// The two detonators rode the nuke tab with setFull3D (held in grip pose) in the old version
		created.add(ModItems.detonator_deadman = HbmRegistry.item(new ItemDrop().setUnlocalizedName("detonator_deadman").setMaxStackSize(1).setFull3D().setCreativeTab(ModItems.nukeTab), "detonator_deadman"));
		created.add(ModItems.detonator_de = HbmRegistry.item(new ItemDrop().setUnlocalizedName("detonator_de").setMaxStackSize(1).setFull3D().setCreativeTab(ModItems.nukeTab), "detonator_de"));
		// beta is this family's easter egg: it deletes itself on landing, so it entered no tab in the old version either; no tab here likewise
		created.add(ModItems.beta = HbmRegistry.item(new ItemDrop().setUnlocalizedName("beta").setMaxStackSize(1), "beta"));

		created.add(ModItems.ams_core_sing = HbmRegistry.item(new ItemAMSCore(1000000000L, 200, 10).setUnlocalizedName("ams_core_sing").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "ams_core_sing"));
		created.add(ModItems.ams_core_wormhole = HbmRegistry.item(new ItemAMSCore(1500000000L, 200, 15).setUnlocalizedName("ams_core_wormhole").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "ams_core_wormhole"));
		created.add(ModItems.ams_core_eyeofharmony = HbmRegistry.item(new ItemAMSCore(2500000000L, 300, 10).setUnlocalizedName("ams_core_eyeofharmony").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "ams_core_eyeofharmony"));
		created.add(ModItems.ams_core_thingy = HbmRegistry.item(new ItemAMSCore(5000000000L, 250, 5).setUnlocalizedName("ams_core_thingy").setCreativeTab(null), "ams_core_thingy"));

		created.add(ModItems.part_generic = HbmRegistry.item(new ItemGenericPart().setUnlocalizedName("part_generic").setCreativeTab(ModItems.partsTab), "part_generic"));

		created.add(ModItems.record_lc = HbmRegistry.item(new ItemModRecord("lc").setUnlocalizedName("record_lc").setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC), "record_lc"));
		created.add(ModItems.record_ss = HbmRegistry.item(new ItemModRecord("ss").setUnlocalizedName("record_ss").setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC), "record_ss"));
		created.add(ModItems.record_vc = HbmRegistry.item(new ItemModRecord("vc").setUnlocalizedName("record_vc").setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC), "record_vc"));
		created.add(ModItems.record_gs = HbmRegistry.item(new ItemModRecord("gs").setUnlocalizedName("record_gs").setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC), "record_gs"));
		created.add(ModItems.record_gp = HbmRegistry.item(new ItemModRecord("gp").setUnlocalizedName("record_gp").setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC), "record_gp"));
		created.add(ModItems.record_el = HbmRegistry.item(new ItemModRecord("el").setUnlocalizedName("record_el").setCreativeTab(net.minecraft.creativetab.CreativeTabs.MISC), "record_el"));
		created.add(ModItems.record_glass = HbmRegistry.item(new ItemModRecord("glass").setUnlocalizedName("record_glass").setCreativeTab(null), "record_glass"));

		// The jukebox's tape: ItemCassette is already in this tree, just never registered.
		// The old setTextureName goes away — JSON models here, texture from models/item/siren_track.json.
		created.add(ModItems.siren_track = HbmRegistry.item(new ItemCassette().setUnlocalizedName("siren_track").setMaxStackSize(1).setCreativeTab(ModItems.templateTab), "siren_track"));

		created.add(ModItems.missile_soyuz = HbmRegistry.item(new ItemSoyuz().setUnlocalizedName("missile_soyuz").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "missile_soyuz"));

		// The one that closes itself on the ground, plus the "expensive" family parts
		created.add(ModItems.demon_core_open = HbmRegistry.item(new ItemDemonCore().setUnlocalizedName("demon_core_open").setCreativeTab(ModItems.nukeTab), "demon_core_open"));
		created.add(ModItems.item_expensive = HbmRegistry.item(new ItemExpensive().setUnlocalizedName("item_expensive").setCreativeTab(ModItems.partsTab), "item_expensive"));

		// The toolbox that dumps its NBT-stored contents into your inventory; contents and tint both written on the stack
		created.add(ModItems.kit_custom = HbmRegistry.item(new ItemKitCustom().setUnlocalizedName("kit_custom").setCreativeTab(null), "kit_custom"));

		/*
		 * The syringe family: all ten share ItemSyringe, branching inside via `this == ModItems.xxx`,
		 * so this batch must land together — miss one and it's just a mute item with no behavior.
		 */
		created.add(ModItems.syringe_metal_stimpak = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_metal_stimpak").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_metal_stimpak"));
		created.add(ModItems.syringe_metal_medx = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_metal_medx").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_metal_medx"));
		created.add(ModItems.syringe_metal_psycho = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_metal_psycho").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_metal_psycho"));
		created.add(ModItems.syringe_metal_super = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_metal_super").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_metal_super"));
		created.add(ModItems.syringe_taint = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_taint").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_taint"));
		created.add(ModItems.syringe_mkunicorn = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_mkunicorn").setFull3D().setCreativeTab(null), "syringe_mkunicorn"));
		// Three regular injectable drugs: behavior lives in ItemSyringe's right-click branches; the tooltip side has had its strings all along
		created.add(ModItems.syringe_antidote = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_antidote").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_antidote"));
		created.add(ModItems.syringe_awesome = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_awesome").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_awesome"));
		created.add(ModItems.syringe_poison = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("syringe_poison").setFull3D().setCreativeTab(ModItems.consumableTab), "syringe_poison"));

		// Cigarette and pipe share ItemCigarette (this == branching inside); both register together or that branch never runs
		created.add(ModItems.cigarette = HbmRegistry.item(new ItemCigarette().setUnlocalizedName("cigarette").setFull3D().setMaxStackSize(16).setCreativeTab(ModItems.consumableTab), "cigarette"));
		created.add(ModItems.crackpipe = HbmRegistry.item(new ItemCigarette().setUnlocalizedName("crackpipe").setFull3D().setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "crackpipe"));

		// Dirac particles: carrying them adds dose; the old "spawn a singularity on landing" isn't ported yet (EntityQuasar not migrated, see the class comment)
		created.add(ModItems.particle_digamma = HbmRegistry.item(new ItemDigamma(60).setUnlocalizedName("particle_digamma").setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.particle_empty), "particle_digamma"));
		created.add(ModItems.med_bag = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("med_bag").setCreativeTab(ModItems.consumableTab), "med_bag"));
		created.add(ModItems.cbt_device = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("cbt_device").setMaxStackSize(1).setCreativeTab(null), "cbt_device"));

		// These two are attachment cans for refilling gear, not consumables, so no stack return — contents transfer into the armor
		created.add(ModItems.jetpack_tank = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("jetpack_tank").setMaxStackSize(16).setCreativeTab(ModItems.consumableTab), "jetpack_tank"));
		created.add(ModItems.lox_tank = HbmRegistry.item(new ItemSyringe().setUnlocalizedName("lox_tank").setMaxStackSize(16).setCreativeTab(ModItems.consumableTab), "lox_tank"));
		// The ballistic missile's propellant section: ItemFleija landed in items/bomb long ago, just never registered,
		// so its own `this == ModItems.fleija_propellant` kept comparing against null and rarity stayed COMMON
		created.add(ModItems.fleija_propellant = HbmRegistry.item(new ItemFleija().setUnlocalizedName("fleija_propellant").setMaxStackSize(1).setCreativeTab(ModItems.nukeTab), "fleija_propellant"));

		// The remaining Fleija and Solinium warhead parts: the three classes (ItemFleija / ItemSolinium / ItemN2) are all in this tree,
		// but the old ModItems assignment lines never moved into a registrar, so the fields stayed null. Nothing else in this tree references them yet
		// (the reference surface lives in the un-migrated nuke assembly tables), so this entry just clears a registration debt without unlocking anything else.
		// Shapes follow the old assignment lines, minus setTextureName (JSON models here).
		created.add(ModItems.fleija_igniter = HbmRegistry.item(new ItemFleija().setUnlocalizedName("fleija_igniter").setMaxStackSize(1).setCreativeTab(ModItems.nukeTab), "fleija_igniter"));
		created.add(ModItems.fleija_core = HbmRegistry.item(new ItemFleija().setUnlocalizedName("fleija_core").setMaxStackSize(1).setCreativeTab(ModItems.nukeTab), "fleija_core"));
		created.add(ModItems.solinium_igniter = HbmRegistry.item(new ItemSolinium().setUnlocalizedName("solinium_igniter").setMaxStackSize(1).setCreativeTab(ModItems.nukeTab), "solinium_igniter"));
		created.add(ModItems.solinium_propellant = HbmRegistry.item(new ItemSolinium().setUnlocalizedName("solinium_propellant").setMaxStackSize(1).setCreativeTab(ModItems.nukeTab), "solinium_propellant"));
		created.add(ModItems.solinium_core = HbmRegistry.item(new ItemSolinium().setUnlocalizedName("solinium_core").setMaxStackSize(1).setCreativeTab(ModItems.nukeTab), "solinium_core"));
		created.add(ModItems.n2_charge = HbmRegistry.item(new ItemN2().setUnlocalizedName("n2_charge").setMaxStackSize(1).setCreativeTab(ModItems.nukeTab), "n2_charge"));

		// Three rad pills + two blood bags: behavior delegated to ItemSimpleConsumable;
		// the old ones were built in place inside init too; going through the registrar gets models/names into the registry together.
		// Note iv_empty is radaway's "empty container", so it must exist before radaway itself.
		created.add(ModItems.iv_empty = HbmRegistry.item(new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			ItemSimpleConsumable.giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.iv_blood));
			user.setHealth(Math.max(user.getHealth() - 5F, 0F));
		}).setUnlocalizedName("iv_empty"), "iv_empty"));
		created.add(ModItems.iv_blood = HbmRegistry.item(new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			ItemSimpleConsumable.giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.iv_empty));
			user.heal(5F);
		}).setUnlocalizedName("iv_blood"), "iv_blood"));
		created.add(ModItems.radaway = HbmRegistry.item(new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			ItemSimpleConsumable.doRadaway(stack, user, 140);
		}).setUnlocalizedName("radaway"), "radaway"));
		created.add(ModItems.radaway_strong = HbmRegistry.item(new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			ItemSimpleConsumable.doRadaway(stack, user, 350);
		}).setUnlocalizedName("radaway_strong"), "radaway_strong"));
		created.add(ModItems.radaway_flush = HbmRegistry.item(new ItemSimpleConsumable().setUseActionServer((stack, user) -> {
			ItemSimpleConsumable.doRadaway(stack, user, 500);
		}).setUnlocalizedName("radaway_flush"), "radaway_flush"));
	}

	@Override
	public void registerModels() {
		for(Item item : created) ItemModels.registerAll(item);

		// Long-half-life waste's meta is the nuclide kind with one shared look, so every tier points at the same model
		int wasteLong = ItemWasteLong.WasteClass.values().length;
		ItemModels.registerRange(ModItems.nuclear_waste_long, wasteLong);
		ItemModels.registerRange(ModItems.nuclear_waste_long_tiny, wasteLong);
		ItemModels.registerRange(ModItems.nuclear_waste_long_depleted, wasteLong);
		ItemModels.registerRange(ModItems.nuclear_waste_long_depleted_tiny, wasteLong);
	}
}
