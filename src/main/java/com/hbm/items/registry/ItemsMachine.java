package com.hbm.items.registry;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.BrokenItem;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBattery;
import com.hbm.items.machine.ItemBatteryCreative;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.items.machine.ItemBatterySC;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.machine.ItemArcElectrodeBurnt;
import com.hbm.items.machine.ItemBlades;
import com.hbm.items.machine.ItemBreedingRod;
import com.hbm.items.machine.ItemBlueprintFolder;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemCanister;
import com.hbm.items.machine.ItemCatalyst;
import com.hbm.items.machine.ItemChemicalDye;
import com.hbm.items.machine.ItemCircuit;
import com.hbm.items.machine.ItemDepletedFuel;
import com.hbm.items.machine.ItemDrillbit;
import com.hbm.items.machine.ItemInfiniteFluid;
import com.hbm.items.machine.ItemPACoil;
import com.hbm.items.machine.ItemPileRodMK2;
import com.hbm.items.machine.ItemPWRFuel;
import com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel;
import com.hbm.items.machine.ItemStampBook;
import com.hbm.items.machine.ItemTurretChip;
import com.hbm.items.machine.ItemWatzPellet;
import com.hbm.items.machine.ItemZirnoxRod;
import com.hbm.items.machine.ItemFELCrystal;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.items.machine.ItemGasTank;
import com.hbm.items.machine.ItemGear;
import com.hbm.items.machine.ItemLens;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemMetaUpgrade;
import com.hbm.items.machine.ItemMuffler;
import com.hbm.items.machine.ItemPileRod;
import com.hbm.items.machine.ItemPistons;
import com.hbm.items.machine.ItemPlateFuel;
import com.hbm.items.machine.ItemPlateFuel.FunctionEnum;
import com.hbm.items.special.ItemPotatos;
import com.hbm.items.machine.ItemRTGPelletDepleted;
import com.hbm.items.machine.ItemScraps;
import com.hbm.items.machine.ItemSatChip;
import com.hbm.items.machine.ItemStamp;
import com.hbm.items.machine.ItemStamp.StampType;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;

import net.minecraft.item.Item;

/**
 * The machine parts registration unit. Everything here is a piece that plugs into or feeds a machine:
 * stamping dies, batteries, dyes, fluid cans and barrels.
 *
 * Note it depends on the generic batch (`setContainerItem` points at empty cans/barrels), so assembly order must come after theirs.
 */
public class ItemsMachine implements IRegistrar {

	private final List<Item> created = new ArrayList<>();
	private final List<Item> fluidTanks = new ArrayList<>();
	private final List<Item> depletedFuels = new ArrayList<>();

	@Override
	public String id() {
		return "items.machine";
	}

	@Override
	public void preInit() {
		created.add(ModItems.chemical_dye = HbmRegistry.item(new ItemChemicalDye().setUnlocalizedName("chemical_dye").setCreativeTab(ModItems.partsTab), "chemical_dye"));

		created.add(ModItems.stamp_stone_flat = HbmRegistry.item(new ItemStamp(32, StampType.FLAT).setUnlocalizedName("stamp_stone_flat").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_stone_flat"));
		created.add(ModItems.stamp_stone_plate = HbmRegistry.item(new ItemStamp(32, StampType.PLATE).setUnlocalizedName("stamp_stone_plate").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_stone_plate"));
		created.add(ModItems.stamp_stone_wire = HbmRegistry.item(new ItemStamp(32, StampType.WIRE).setUnlocalizedName("stamp_stone_wire").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_stone_wire"));
		created.add(ModItems.stamp_stone_circuit = HbmRegistry.item(new ItemStamp(32, StampType.CIRCUIT).setUnlocalizedName("stamp_stone_circuit").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_stone_circuit"));
		created.add(ModItems.stamp_iron_flat = HbmRegistry.item(new ItemStamp(64, StampType.FLAT).setUnlocalizedName("stamp_iron_flat").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_iron_flat"));
		created.add(ModItems.stamp_iron_plate = HbmRegistry.item(new ItemStamp(64, StampType.PLATE).setUnlocalizedName("stamp_iron_plate").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_iron_plate"));
		created.add(ModItems.stamp_iron_wire = HbmRegistry.item(new ItemStamp(64, StampType.WIRE).setUnlocalizedName("stamp_iron_wire").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_iron_wire"));
		created.add(ModItems.stamp_iron_circuit = HbmRegistry.item(new ItemStamp(64, StampType.CIRCUIT).setUnlocalizedName("stamp_iron_circuit").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_iron_circuit"));
		created.add(ModItems.stamp_steel_flat = HbmRegistry.item(new ItemStamp(192, StampType.FLAT).setUnlocalizedName("stamp_steel_flat").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_steel_flat"));
		created.add(ModItems.stamp_steel_plate = HbmRegistry.item(new ItemStamp(192, StampType.PLATE).setUnlocalizedName("stamp_steel_plate").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_steel_plate"));
		created.add(ModItems.stamp_steel_wire = HbmRegistry.item(new ItemStamp(192, StampType.WIRE).setUnlocalizedName("stamp_steel_wire").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_steel_wire"));
		created.add(ModItems.stamp_steel_circuit = HbmRegistry.item(new ItemStamp(192, StampType.CIRCUIT).setUnlocalizedName("stamp_steel_circuit").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_steel_circuit"));
		created.add(ModItems.stamp_titanium_flat = HbmRegistry.item(new ItemStamp(256, StampType.FLAT).setUnlocalizedName("stamp_titanium_flat").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_titanium_flat"));
		created.add(ModItems.stamp_titanium_plate = HbmRegistry.item(new ItemStamp(256, StampType.PLATE).setUnlocalizedName("stamp_titanium_plate").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_titanium_plate"));
		created.add(ModItems.stamp_titanium_wire = HbmRegistry.item(new ItemStamp(256, StampType.WIRE).setUnlocalizedName("stamp_titanium_wire").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_titanium_wire"));
		created.add(ModItems.stamp_titanium_circuit = HbmRegistry.item(new ItemStamp(256, StampType.CIRCUIT).setUnlocalizedName("stamp_titanium_circuit").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_titanium_circuit"));
		created.add(ModItems.stamp_obsidian_flat = HbmRegistry.item(new ItemStamp(512, StampType.FLAT).setUnlocalizedName("stamp_obsidian_flat").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_obsidian_flat"));
		created.add(ModItems.stamp_obsidian_plate = HbmRegistry.item(new ItemStamp(512, StampType.PLATE).setUnlocalizedName("stamp_obsidian_plate").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_obsidian_plate"));
		created.add(ModItems.stamp_obsidian_wire = HbmRegistry.item(new ItemStamp(512, StampType.WIRE).setUnlocalizedName("stamp_obsidian_wire").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_obsidian_wire"));
		created.add(ModItems.stamp_obsidian_circuit = HbmRegistry.item(new ItemStamp(512, StampType.CIRCUIT).setUnlocalizedName("stamp_obsidian_circuit").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_obsidian_circuit"));
		created.add(ModItems.stamp_desh_flat = HbmRegistry.item(new ItemStamp(0, StampType.FLAT).setUnlocalizedName("stamp_desh_flat").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_flat"));
		created.add(ModItems.stamp_desh_plate = HbmRegistry.item(new ItemStamp(0, StampType.PLATE).setUnlocalizedName("stamp_desh_plate").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_plate"));
		created.add(ModItems.stamp_desh_wire = HbmRegistry.item(new ItemStamp(0, StampType.WIRE).setUnlocalizedName("stamp_desh_wire").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_wire"));
		created.add(ModItems.stamp_desh_circuit = HbmRegistry.item(new ItemStamp(0, StampType.CIRCUIT).setUnlocalizedName("stamp_desh_circuit").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_circuit"));
		created.add(ModItems.stamp_357 = HbmRegistry.item(new ItemStamp(1000, StampType.C357).setUnlocalizedName("stamp_357").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_357"));
		created.add(ModItems.stamp_44 = HbmRegistry.item(new ItemStamp(1000, StampType.C44).setUnlocalizedName("stamp_44").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_44"));
		created.add(ModItems.stamp_9 = HbmRegistry.item(new ItemStamp(1000, StampType.C9).setUnlocalizedName("stamp_9").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_9"));
		created.add(ModItems.stamp_50 = HbmRegistry.item(new ItemStamp(1000, StampType.C50).setUnlocalizedName("stamp_50").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_50"));
		created.add(ModItems.stamp_desh_357 = HbmRegistry.item(new ItemStamp(0, StampType.C357).setUnlocalizedName("stamp_desh_357").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_357"));
		created.add(ModItems.stamp_desh_44 = HbmRegistry.item(new ItemStamp(0, StampType.C44).setUnlocalizedName("stamp_desh_44").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_44"));
		created.add(ModItems.stamp_desh_9 = HbmRegistry.item(new ItemStamp(0, StampType.C9).setUnlocalizedName("stamp_desh_9").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_9"));
		created.add(ModItems.stamp_desh_50 = HbmRegistry.item(new ItemStamp(0, StampType.C50).setUnlocalizedName("stamp_desh_50").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "stamp_desh_50"));

		created.add(ModItems.cube_power = HbmRegistry.item(new ItemBattery(1000000000000000000L, 1000000000000000L, 1000000000000000L).setUnlocalizedName("cube_power").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "cube_power"));
		created.add(ModItems.core_angel = HbmRegistry.item(new ItemBattery(16_000_000L, 16_000_000L, 800_000L).setUnlocalizedName("core_angel").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "core_angel"));
		created.add(ModItems.battery_potato = HbmRegistry.item(new ItemBattery(1000, 0, 100).setUnlocalizedName("battery_potato").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "battery_potato"));
		created.add(ModItems.memory = HbmRegistry.item(new ItemBattery(Long.MAX_VALUE / 100L, 100000000000000L, 100000000000000L).setUnlocalizedName("memory").setMaxStackSize(1), "memory"));

		fluidTanks.add(ModItems.fluid_tank_full = HbmRegistry.item(new ItemFluidTank().setUnlocalizedName("fluid_tank_full").setContainerItem(ModItems.fluid_tank_empty).setCreativeTab(ModItems.controlTab), "fluid_tank_full"));
		fluidTanks.add(ModItems.fluid_tank_lead_full = HbmRegistry.item(new ItemFluidTank().setUnlocalizedName("fluid_tank_lead_full").setContainerItem(ModItems.fluid_tank_lead_empty).setCreativeTab(ModItems.controlTab), "fluid_tank_lead_full"));
		fluidTanks.add(ModItems.fluid_barrel_full = HbmRegistry.item(new ItemFluidTank().setUnlocalizedName("fluid_barrel_full").setContainerItem(ModItems.fluid_barrel_empty).setCreativeTab(ModItems.controlTab), "fluid_barrel_full"));
		fluidTanks.add(ModItems.fluid_pack_full = HbmRegistry.item(new ItemFluidTank().setUnlocalizedName("fluid_pack_full").setContainerItem(ModItems.fluid_pack_empty).setCreativeTab(ModItems.controlTab), "fluid_pack_full"));
		fluidTanks.add(ModItems.canister_full = HbmRegistry.item(new ItemCanister().setUnlocalizedName("canister_full").setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.canister_empty), "canister_full"));
		fluidTanks.add(ModItems.fluid_icon = HbmRegistry.item(new ItemFluidIcon().setUnlocalizedName("fluid_icon").setCreativeTab(null), "fluid_icon"));
		fluidTanks.add(ModItems.gas_full = HbmRegistry.item(new ItemGasTank().setUnlocalizedName("gas_full").setCreativeTab(ModItems.controlTab).setContainerItem(ModItems.gas_empty), "gas_full"));

		created.add(ModItems.circuit = HbmRegistry.item(new ItemCircuit().setUnlocalizedName("circuit").setCreativeTab(ModItems.partsTab), "circuit"));
		created.add(ModItems.drillbit = HbmRegistry.item(new ItemDrillbit().setUnlocalizedName("drillbit").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "drillbit"));
		created.add(ModItems.pa_coil = HbmRegistry.item(new ItemPACoil().setUnlocalizedName("pa_coil").setCreativeTab(ModItems.controlTab), "pa_coil"));
		created.add(ModItems.stamp_book = HbmRegistry.item(new ItemStampBook().setUnlocalizedName("stamp_book").setMaxStackSize(1).setCreativeTab(null), "stamp_book"));
		created.add(ModItems.arc_electrode = HbmRegistry.item(new ItemArcElectrode().setUnlocalizedName("arc_electrode").setCreativeTab(ModItems.controlTab), "arc_electrode"));
		created.add(ModItems.arc_electrode_burnt = HbmRegistry.item(new ItemArcElectrodeBurnt().setUnlocalizedName("arc_electrode_burnt").setCreativeTab(ModItems.controlTab), "arc_electrode_burnt"));
		created.add(ModItems.turret_chip = HbmRegistry.item(new ItemTurretChip().setUnlocalizedName("turret_chip").setMaxStackSize(1).setCreativeTab(ModItems.weaponTab), "turret_chip"));

		created.add(ModItems.laser_crystal_co2 = HbmRegistry.item(new ItemFELCrystal(EnumWavelengths.IR).setUnlocalizedName("laser_crystal_co2").setCreativeTab(ModItems.controlTab), "laser_crystal_co2"));
		created.add(ModItems.laser_crystal_bismuth = HbmRegistry.item(new ItemFELCrystal(EnumWavelengths.VISIBLE).setUnlocalizedName("laser_crystal_bismuth").setCreativeTab(ModItems.controlTab), "laser_crystal_bismuth"));
		created.add(ModItems.laser_crystal_cmb = HbmRegistry.item(new ItemFELCrystal(EnumWavelengths.UV).setUnlocalizedName("laser_crystal_cmb").setCreativeTab(ModItems.controlTab), "laser_crystal_cmb"));
		created.add(ModItems.laser_crystal_iron = HbmRegistry.item(new ItemFELCrystal(EnumWavelengths.XRAY).setUnlocalizedName("laser_crystal_iron").setCreativeTab(ModItems.controlTab), "laser_crystal_iron"));
		created.add(ModItems.laser_crystal_dnt = HbmRegistry.item(new ItemFELCrystal(EnumWavelengths.GAMMA).setUnlocalizedName("laser_crystal_dnt").setCreativeTab(ModItems.controlTab), "laser_crystal_dnt"));
		created.add(ModItems.laser_crystal_digamma = HbmRegistry.item(new ItemFELCrystal(EnumWavelengths.DRX).setUnlocalizedName("laser_crystal_digamma").setCreativeTab(ModItems.controlTab), "laser_crystal_digamma"));

		created.add(ModItems.gear_large = HbmRegistry.item(new ItemGear().setUnlocalizedName("gear_large").setCreativeTab(ModItems.partsTab), "gear_large"));
		created.add(ModItems.piston_set = HbmRegistry.item(new ItemPistons().setUnlocalizedName("piston_set").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "piston_set"));
		created.add(ModItems.pellet_rtg_depleted = HbmRegistry.item(new ItemRTGPelletDepleted().setContainerItem(ModItems.plate_iron).setUnlocalizedName("pellet_rtg_depleted").setCreativeTab(ModItems.controlTab), "pellet_rtg_depleted"));
		created.add(ModItems.ams_lens = HbmRegistry.item(new ItemLens(60 * 60 * 60 * 20 * 100).setUnlocalizedName("ams_lens").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "ams_lens"));
		created.add(ModItems.sat_chip = HbmRegistry.item(new ItemSatChip().setUnlocalizedName("sat_chip").setMaxStackSize(1).setCreativeTab(ModItems.missileTab), "sat_chip"));
		created.add(ModItems.battery_pack = HbmRegistry.item(new ItemBatteryPack().setUnlocalizedName("battery_pack"), "battery_pack"));
		created.add(ModItems.battery_creative = HbmRegistry.item(new ItemBatteryCreative().setUnlocalizedName("battery_creative").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "battery_creative"));
		created.add(ModItems.battery_potatos = HbmRegistry.item(new ItemPotatos(500000, 0, 100).setUnlocalizedName("battery_potatos").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "battery_potatos"));

		created.add(ModItems.scraps = HbmRegistry.item(new ItemScraps().setUnlocalizedName("scraps").setCreativeTab(ModItems.partsTab), "scraps"));
		created.add(ModItems.pile_rod = HbmRegistry.item(new ItemPileRodMK2().setUnlocalizedName("pile_rod").setCreativeTab(ModItems.controlTab), "pile_rod"));
		created.add(ModItems.rod_zirnox = (ItemEnumMulti) HbmRegistry.item(new ItemZirnoxRod().setUnlocalizedName("rod_zirnox").setCreativeTab(ModItems.controlTab), "rod_zirnox"));
		created.add(ModItems.rod = HbmRegistry.item(new ItemBreedingRod().setUnlocalizedName("rod").setContainerItem(ModItems.rod_empty).setCreativeTab(ModItems.controlTab), "rod"));
		created.add(ModItems.rod_dual = HbmRegistry.item(new ItemBreedingRod().setUnlocalizedName("rod_dual").setContainerItem(ModItems.rod_dual_empty).setCreativeTab(ModItems.controlTab), "rod_dual"));
		created.add(ModItems.rod_quad = HbmRegistry.item(new ItemBreedingRod().setUnlocalizedName("rod_quad").setContainerItem(ModItems.rod_quad_empty).setCreativeTab(ModItems.controlTab), "rod_quad"));

		created.add(ModItems.blades_steel = HbmRegistry.item(new ItemBlades(400).setUnlocalizedName("blades_steel").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "blades_steel"));
		created.add(ModItems.blades_titanium = HbmRegistry.item(new ItemBlades(500).setUnlocalizedName("blades_titanium").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "blades_titanium"));
		created.add(ModItems.blades_desh = HbmRegistry.item(new ItemBlades(0).setUnlocalizedName("blades_desh").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "blades_desh"));

		created.add(ModItems.pile_rod_uranium = HbmRegistry.item(new ItemPileRod().setUnlocalizedName("pile_rod_uranium").setCreativeTab(null), "pile_rod_uranium"));
		created.add(ModItems.pile_rod_pu239 = HbmRegistry.item(new ItemPileRod().setUnlocalizedName("pile_rod_pu239").setCreativeTab(null), "pile_rod_pu239"));
		created.add(ModItems.pile_rod_plutonium = HbmRegistry.item(new ItemPileRod().setUnlocalizedName("pile_rod_plutonium").setCreativeTab(null), "pile_rod_plutonium"));
		created.add(ModItems.pile_rod_source = HbmRegistry.item(new ItemPileRod().setUnlocalizedName("pile_rod_source").setCreativeTab(null), "pile_rod_source"));
		created.add(ModItems.pile_rod_boron = HbmRegistry.item(new ItemPileRod().setUnlocalizedName("pile_rod_boron").setCreativeTab(null), "pile_rod_boron"));
		created.add(ModItems.pile_rod_lithium = HbmRegistry.item(new ItemPileRod().setUnlocalizedName("pile_rod_lithium").setCreativeTab(null), "pile_rod_lithium"));
		created.add(ModItems.pile_rod_detector = HbmRegistry.item(new ItemPileRod().setUnlocalizedName("pile_rod_detector").setCreativeTab(null), "pile_rod_detector"));

		created.add(ModItems.plate_fuel_u233 = HbmRegistry.item(new ItemPlateFuel(2200000).setFunction(FunctionEnum.SQUARE_ROOT, 50).setUnlocalizedName("plate_fuel_u233").setMaxStackSize(1).setCreativeTab(null), "plate_fuel_u233"));
		created.add(ModItems.plate_fuel_u235 = HbmRegistry.item(new ItemPlateFuel(2200000).setFunction(FunctionEnum.SQUARE_ROOT, 40).setUnlocalizedName("plate_fuel_u235").setMaxStackSize(1).setCreativeTab(null), "plate_fuel_u235"));
		created.add(ModItems.plate_fuel_mox = HbmRegistry.item(new ItemPlateFuel(2400000).setFunction(FunctionEnum.LOGARITHM, 50).setUnlocalizedName("plate_fuel_mox").setMaxStackSize(1).setCreativeTab(null), "plate_fuel_mox"));
		created.add(ModItems.plate_fuel_pu239 = HbmRegistry.item(new ItemPlateFuel(2000000).setFunction(FunctionEnum.NEGATIVE_QUADRATIC, 50).setUnlocalizedName("plate_fuel_pu239").setMaxStackSize(1).setCreativeTab(null), "plate_fuel_pu239"));
		created.add(ModItems.plate_fuel_sa326 = HbmRegistry.item(new ItemPlateFuel(2000000).setFunction(FunctionEnum.LINEAR, 80).setUnlocalizedName("plate_fuel_sa326").setMaxStackSize(1).setCreativeTab(null), "plate_fuel_sa326"));
		created.add(ModItems.plate_fuel_ra226be = HbmRegistry.item(new ItemPlateFuel(1300000).setFunction(FunctionEnum.PASSIVE, 30).setUnlocalizedName("plate_fuel_ra226be").setMaxStackSize(1).setCreativeTab(null), "plate_fuel_ra226be"));
		created.add(ModItems.plate_fuel_pu238be = HbmRegistry.item(new ItemPlateFuel(1000000).setFunction(FunctionEnum.PASSIVE, 50).setUnlocalizedName("plate_fuel_pu238be").setMaxStackSize(1).setCreativeTab(null), "plate_fuel_pu238be"));

		created.add(ModItems.ams_catalyst_aluminium = HbmRegistry.item(new ItemCatalyst(0xCCCCCC, 1000000, 1.15F, 0.85F, 1.15F).setUnlocalizedName("ams_catalyst_aluminium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_aluminium"));
		created.add(ModItems.ams_catalyst_beryllium = HbmRegistry.item(new ItemCatalyst(0x97978B, 0, 1.25F, 0.95F, 1.05F).setUnlocalizedName("ams_catalyst_beryllium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_beryllium"));
		created.add(ModItems.ams_catalyst_caesium = HbmRegistry.item(new ItemCatalyst(0x6400FF, 2500000, 1.00F, 0.85F, 1.15F).setUnlocalizedName("ams_catalyst_caesium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_caesium"));
		created.add(ModItems.ams_catalyst_cerium = HbmRegistry.item(new ItemCatalyst(0x1D3FFF, 1000000, 1.15F, 1.15F, 0.85F).setUnlocalizedName("ams_catalyst_cerium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_cerium"));
		created.add(ModItems.ams_catalyst_cobalt = HbmRegistry.item(new ItemCatalyst(0x789BBE, 0, 1.25F, 1.05F, 0.95F).setUnlocalizedName("ams_catalyst_cobalt").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_cobalt"));
		created.add(ModItems.ams_catalyst_copper = HbmRegistry.item(new ItemCatalyst(0xAADE29, 0, 1.25F, 1.00F, 1.00F).setUnlocalizedName("ams_catalyst_copper").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_copper"));
		created.add(ModItems.ams_catalyst_dineutronium = HbmRegistry.item(new ItemCatalyst(0x334077, 2500000, 1.00F, 1.15F, 0.85F).setUnlocalizedName("ams_catalyst_dineutronium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_dineutronium"));
		created.add(ModItems.ams_catalyst_euphemium = HbmRegistry.item(new ItemCatalyst(0xFF9CD2, 2500000, 1.00F, 1.00F, 1.00F).setUnlocalizedName("ams_catalyst_euphemium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_euphemium"));
		created.add(ModItems.ams_catalyst_iron = HbmRegistry.item(new ItemCatalyst(0xFF7E22, 1000000, 1.15F, 0.95F, 1.05F).setUnlocalizedName("ams_catalyst_iron").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_iron"));
		created.add(ModItems.ams_catalyst_lithium = HbmRegistry.item(new ItemCatalyst(0xFF2727, 0, 1.25F, 0.85F, 1.15F).setUnlocalizedName("ams_catalyst_lithium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_lithium"));
		created.add(ModItems.ams_catalyst_niobium = HbmRegistry.item(new ItemCatalyst(0x3BF1B6, 1000000, 1.15F, 1.05F, 0.95F).setUnlocalizedName("ams_catalyst_niobium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_niobium"));
		created.add(ModItems.ams_catalyst_schrabidium = HbmRegistry.item(new ItemCatalyst(0x32FFFF, 2500000, 1.00F, 1.05F, 0.95F).setUnlocalizedName("ams_catalyst_schrabidium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_schrabidium"));
		created.add(ModItems.ams_catalyst_strontium = HbmRegistry.item(new ItemCatalyst(0xDD0D35, 1000000, 1.15F, 1.00F, 1.00F).setUnlocalizedName("ams_catalyst_strontium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_strontium"));
		created.add(ModItems.ams_catalyst_thorium = HbmRegistry.item(new ItemCatalyst(0x653B22, 2500000, 1.00F, 0.95F, 1.05F).setUnlocalizedName("ams_catalyst_thorium").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_thorium"));
		created.add(ModItems.ams_catalyst_tungsten = HbmRegistry.item(new ItemCatalyst(0xF5FF48, 0, 1.25F, 1.15F, 0.85F).setUnlocalizedName("ams_catalyst_tungsten").setCreativeTab(ModItems.controlTab).setMaxStackSize(1), "ams_catalyst_tungsten"));

		// Pressurized water reactor fuel rods: the main piece gets one texture per subtype; hot and depleted rods share one, told apart by subtype name
		created.add(ModItems.pwr_fuel = HbmRegistry.item(new ItemPWRFuel().setUnlocalizedName("pwr_fuel").setCreativeTab(ModItems.controlTab), "pwr_fuel"));
		created.add(ModItems.pwr_fuel_hot = HbmRegistry.item(new ItemEnumMulti(EnumPWRFuel.class, true, false).setUnlocalizedName("pwr_fuel_hot").setCreativeTab(ModItems.controlTab), "pwr_fuel_hot"));
		created.add(ModItems.pwr_fuel_depleted = HbmRegistry.item(new ItemEnumMulti(EnumPWRFuel.class, true, false).setUnlocalizedName("pwr_fuel_depleted").setCreativeTab(ModItems.controlTab), "pwr_fuel_depleted"));

		// Watz fuel pellets: one colored, one depleted; both pieces' 21 subtypes are re-tinted from grayscale bases at load
		created.add(ModItems.watz_pellet = HbmRegistry.item(new ItemWatzPellet().setUnlocalizedName("watz_pellet"), "watz_pellet"));
		created.add(ModItems.watz_pellet_depleted = HbmRegistry.item(new ItemWatzPellet().setUnlocalizedName("watz_pellet"), "watz_pellet_depleted"));

		// The self-charging battery: charge is decided by meta, not NBT, so all four charge/discharge entries are no-ops
		created.add(ModItems.battery_sc = HbmRegistry.item(new ItemBatterySC().setUnlocalizedName("battery_sc"), "battery_sc"));

		// Sixteen spent fuels: two tiers each (fresh/cooled), the cooled tier a full-texture tint, so meta models must span to 1
		depletedFuels.add(ModItems.waste_natural_uranium = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_natural_uranium").setCreativeTab(ModItems.partsTab), "waste_natural_uranium"));
		depletedFuels.add(ModItems.waste_uranium = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_uranium").setCreativeTab(ModItems.partsTab), "waste_uranium"));
		depletedFuels.add(ModItems.waste_thorium = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_thorium").setCreativeTab(ModItems.partsTab), "waste_thorium"));
		depletedFuels.add(ModItems.waste_mox = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_mox").setCreativeTab(ModItems.partsTab), "waste_mox"));
		depletedFuels.add(ModItems.waste_plutonium = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plutonium").setCreativeTab(ModItems.partsTab), "waste_plutonium"));
		depletedFuels.add(ModItems.waste_u233 = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_u233").setCreativeTab(ModItems.partsTab), "waste_u233"));
		depletedFuels.add(ModItems.waste_u235 = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_u235").setCreativeTab(ModItems.partsTab), "waste_u235"));
		depletedFuels.add(ModItems.waste_schrabidium = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_schrabidium").setCreativeTab(ModItems.partsTab), "waste_schrabidium"));
		depletedFuels.add(ModItems.waste_zfb_mox = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_zfb_mox").setCreativeTab(ModItems.partsTab), "waste_zfb_mox"));
		depletedFuels.add(ModItems.waste_plate_u233 = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plate_u233").setCreativeTab(ModItems.partsTab), "waste_plate_u233"));
		depletedFuels.add(ModItems.waste_plate_u235 = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plate_u235").setCreativeTab(ModItems.partsTab), "waste_plate_u235"));
		depletedFuels.add(ModItems.waste_plate_mox = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plate_mox").setCreativeTab(ModItems.partsTab), "waste_plate_mox"));
		depletedFuels.add(ModItems.waste_plate_pu239 = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plate_pu239").setCreativeTab(ModItems.partsTab), "waste_plate_pu239"));
		depletedFuels.add(ModItems.waste_plate_sa326 = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plate_sa326").setCreativeTab(ModItems.partsTab), "waste_plate_sa326"));
		depletedFuels.add(ModItems.waste_plate_ra226be = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plate_ra226be").setCreativeTab(ModItems.partsTab), "waste_plate_ra226be"));
		depletedFuels.add(ModItems.waste_plate_pu238be = HbmRegistry.item(new ItemDepletedFuel().setUnlocalizedName("waste_plate_pu238be").setCreativeTab(ModItems.partsTab), "waste_plate_pu238be"));

		// The blueprint family: blueprints swap looks by the NBT pool prefix (see blueprints.json overrides),
		// the blueprint folder by meta (three textures, see ItemBlueprintFolder.subtypeModels).
		// The broken piece never entered the creative tab in the old version; same here — it's a machine's output, not something to display.
		created.add(ModItems.blueprints = HbmRegistry.item(new ItemBlueprints().setUnlocalizedName("blueprints").setCreativeTab(ModItems.templateTab), "blueprints"));
		created.add(ModItems.blueprint_folder = HbmRegistry.item(new ItemBlueprintFolder().setUnlocalizedName("blueprint_folder").setCreativeTab(ModItems.templateTab), "blueprint_folder"));
		created.add(ModItems.broken_item = HbmRegistry.item(new BrokenItem().setUnlocalizedName("broken_item"), "broken_item"));

		// The machine upgrade family. The six "same kind, three tiers" sets spread first in the old order, the special-effect singles follow;
		// the precision assembly table's upgrade_* recipes read exactly these fields — without the items they're all null.
		// Whoever inserts upgrades into machines (the upgrade slots) isn't wired yet; that half belongs to the machine side.
		created.add(ModItems.upgrade_speed_1 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.SPEED, 1).setUnlocalizedName("upgrade_speed_1").setCreativeTab(ModItems.controlTab), "upgrade_speed_1"));
		created.add(ModItems.upgrade_speed_2 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.SPEED, 2).setUnlocalizedName("upgrade_speed_2").setCreativeTab(ModItems.controlTab), "upgrade_speed_2"));
		created.add(ModItems.upgrade_speed_3 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.SPEED, 3).setUnlocalizedName("upgrade_speed_3").setCreativeTab(ModItems.controlTab), "upgrade_speed_3"));
		created.add(ModItems.upgrade_effect_1 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.EFFECT, 1).setUnlocalizedName("upgrade_effect_1").setCreativeTab(ModItems.controlTab), "upgrade_effect_1"));
		created.add(ModItems.upgrade_effect_2 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.EFFECT, 2).setUnlocalizedName("upgrade_effect_2").setCreativeTab(ModItems.controlTab), "upgrade_effect_2"));
		created.add(ModItems.upgrade_effect_3 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.EFFECT, 3).setUnlocalizedName("upgrade_effect_3").setCreativeTab(ModItems.controlTab), "upgrade_effect_3"));
		created.add(ModItems.upgrade_power_1 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.POWER, 1).setUnlocalizedName("upgrade_power_1").setCreativeTab(ModItems.controlTab), "upgrade_power_1"));
		created.add(ModItems.upgrade_power_2 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.POWER, 2).setUnlocalizedName("upgrade_power_2").setCreativeTab(ModItems.controlTab), "upgrade_power_2"));
		created.add(ModItems.upgrade_power_3 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.POWER, 3).setUnlocalizedName("upgrade_power_3").setCreativeTab(ModItems.controlTab), "upgrade_power_3"));
		created.add(ModItems.upgrade_fortune_1 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.FORTUNE, 1).setUnlocalizedName("upgrade_fortune_1").setCreativeTab(ModItems.controlTab), "upgrade_fortune_1"));
		created.add(ModItems.upgrade_fortune_2 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.FORTUNE, 2).setUnlocalizedName("upgrade_fortune_2").setCreativeTab(ModItems.controlTab), "upgrade_fortune_2"));
		created.add(ModItems.upgrade_fortune_3 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.FORTUNE, 3).setUnlocalizedName("upgrade_fortune_3").setCreativeTab(ModItems.controlTab), "upgrade_fortune_3"));
		created.add(ModItems.upgrade_afterburn_1 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.AFTERBURN, 1).setUnlocalizedName("upgrade_afterburn_1").setCreativeTab(ModItems.controlTab), "upgrade_afterburn_1"));
		created.add(ModItems.upgrade_afterburn_2 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.AFTERBURN, 2).setUnlocalizedName("upgrade_afterburn_2").setCreativeTab(ModItems.controlTab), "upgrade_afterburn_2"));
		created.add(ModItems.upgrade_afterburn_3 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.AFTERBURN, 3).setUnlocalizedName("upgrade_afterburn_3").setCreativeTab(ModItems.controlTab), "upgrade_afterburn_3"));
		created.add(ModItems.upgrade_overdrive_1 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 1).setUnlocalizedName("upgrade_overdrive_1").setCreativeTab(ModItems.controlTab), "upgrade_overdrive_1"));
		created.add(ModItems.upgrade_overdrive_2 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 2).setUnlocalizedName("upgrade_overdrive_2").setCreativeTab(ModItems.controlTab), "upgrade_overdrive_2"));
		created.add(ModItems.upgrade_overdrive_3 = HbmRegistry.item(new ItemMachineUpgrade(UpgradeType.OVERDRIVE, 3).setUnlocalizedName("upgrade_overdrive_3").setCreativeTab(ModItems.controlTab), "upgrade_overdrive_3"));
		created.add(ModItems.upgrade_radius = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_radius").setMaxStackSize(16).setCreativeTab(ModItems.controlTab), "upgrade_radius"));
		created.add(ModItems.upgrade_health = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_health").setMaxStackSize(16).setCreativeTab(ModItems.controlTab), "upgrade_health"));
		created.add(ModItems.upgrade_smelter = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_smelter").setCreativeTab(ModItems.controlTab), "upgrade_smelter"));
		created.add(ModItems.upgrade_shredder = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_shredder").setCreativeTab(ModItems.controlTab), "upgrade_shredder"));
		created.add(ModItems.upgrade_centrifuge = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_centrifuge").setCreativeTab(ModItems.controlTab), "upgrade_centrifuge"));
		created.add(ModItems.upgrade_crystallizer = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_crystallizer").setCreativeTab(ModItems.controlTab), "upgrade_crystallizer"));
		created.add(ModItems.upgrade_nullifier = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_nullifier").setCreativeTab(ModItems.controlTab), "upgrade_nullifier"));
		created.add(ModItems.upgrade_screm = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_screm").setCreativeTab(ModItems.controlTab), "upgrade_screm"));
		created.add(ModItems.upgrade_gc_speed = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_gc_speed").setCreativeTab(ModItems.controlTab), "upgrade_gc_speed"));
		created.add(ModItems.upgrade_5g = HbmRegistry.item(new ItemMachineUpgrade().setUnlocalizedName("upgrade_5g").setCreativeTab(ModItems.controlTab), "upgrade_5g"));

		// The two tiered pieces (one meta per tier, three tiers), names and textures carrying tier suffixes
		created.add(ModItems.upgrade_stack = HbmRegistry.item(new ItemMetaUpgrade(3).setUnlocalizedName("upgrade_stack").setCreativeTab(ModItems.controlTab), "upgrade_stack"));
		created.add(ModItems.upgrade_ejector = HbmRegistry.item(new ItemMetaUpgrade(3).setUnlocalizedName("upgrade_ejector").setCreativeTab(ModItems.controlTab), "upgrade_ejector"));

		// Inserts click onto machines; parts tab in the old version.
		// Note the same-named upgrade_template item is already registered by ItemsGeneric as an ItemCustomLore,
		// so it can't be registered again: a duplicate name makes FML decide at freezeData that "the override didn't match its object" and crash startup outright.
		created.add(ModItems.upgrade_muffler = HbmRegistry.item(new ItemMuffler().setUnlocalizedName("upgrade_muffler").setCreativeTab(ModItems.partsTab), "upgrade_muffler"));

		// The infinite fluid source family: the two fans ride the parts tab, the barrel and two water bags the control tab.
		// The latter two already consulted the body-fluid table via requiresTable in the old version; that half waits for dim; this batch registers items only.
		created.add(ModItems.chlorine_pinwheel = HbmRegistry.item(new ItemInfiniteFluid(Fluids.CHLORINE, 1, 2).setUnlocalizedName("chlorine_pinwheel").setCreativeTab(ModItems.partsTab), "chlorine_pinwheel"));
		created.add(ModItems.oxy_pinwheel = HbmRegistry.item(new ItemInfiniteFluid(Fluids.OXYGEN, 1).setUnlocalizedName("oxy_pinwheel").setCreativeTab(ModItems.partsTab), "oxy_pinwheel"));
		created.add(ModItems.fluid_barrel_infinite = HbmRegistry.item(new ItemInfiniteFluid(null, 1_000_000_000).setUnlocalizedName("fluid_barrel_infinite").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "fluid_barrel_infinite"));
		created.add(ModItems.inf_water = HbmRegistry.item(new ItemInfiniteFluid(Fluids.WATER, 50, true).setUnlocalizedName("inf_water").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "inf_water"));
		created.add(ModItems.inf_water_mk2 = HbmRegistry.item(new ItemInfiniteFluid(Fluids.WATER, 500, true).setUnlocalizedName("inf_water_mk2").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "inf_water_mk2"));
	}

	@Override
	public void registerModels() {
		for(Item item : created) ItemModels.registerAll(item);

		// The fluid cell's meta is the fluid id (sparse, non-zero-based) with one look, so the whole span gets mapped
		for(Item item : fluidTanks) ItemModels.registerRange(item, Fluids.getAll().length);

		// Spent fuel's cooled tier shares the fresh tier's texture, differing only by meta tint
		for(Item item : depletedFuels) ItemModels.registerRange(item, 2);

		registerScrapPredicates();
		registerBlueprintPredicates();
	}

	/**
	 * Scrap's liquid/additive variants don't swap textures per meta; the model JSON's `overrides` switch by predicate;
	 * the predicates hang here — client-only, hence inside registerModels.
	 */
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	private void registerScrapPredicates() {

		ModItems.scraps.addPropertyOverride(HbmRegistry.rl("liquid"), new net.minecraft.item.IItemPropertyGetter() {
			@Override
			public float apply(net.minecraft.item.ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity) {
				return ItemScraps.isLiquid(stack) ? 1F : 0F;
			}
		});

		ModItems.scraps.addPropertyOverride(HbmRegistry.rl("additive"), new net.minecraft.item.IItemPropertyGetter() {
			@Override
			public float apply(net.minecraft.item.ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity) {
				com.hbm.inventory.material.Mats.MaterialStack mats = ItemScraps.getMats(stack);
				boolean additive = mats != null && mats.material.smeltable == com.hbm.inventory.material.NTMMaterial.SmeltingBehavior.ADDITIVE;
				return additive ? 1F : 0F;
			}
		});
	}

	/**
	 * The blueprint's four looks are one item with meta always 0, distinguished only by the NBT pool prefix,
	 * so the overrides-predicate route is the only one (see blueprints.json).
	 */
	@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
	private void registerBlueprintPredicates() {

		ModItems.blueprints.addPropertyOverride(HbmRegistry.rl("discover"), new net.minecraft.item.IItemPropertyGetter() {
			@Override
			public float apply(net.minecraft.item.ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity) {
				return ItemBlueprints.isDiscover(stack) ? 1F : 0F;
			}
		});

		ModItems.blueprints.addPropertyOverride(HbmRegistry.rl("secret"), new net.minecraft.item.IItemPropertyGetter() {
			@Override
			public float apply(net.minecraft.item.ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity) {
				return ItemBlueprints.isSecret(stack) ? 1F : 0F;
			}
		});

		ModItems.blueprints.addPropertyOverride(HbmRegistry.rl("528"), new net.minecraft.item.IItemPropertyGetter() {
			@Override
			public float apply(net.minecraft.item.ItemStack stack, net.minecraft.world.World world, net.minecraft.entity.EntityLivingBase entity) {
				return ItemBlueprints.is528(stack) ? 1F : 0F;
			}
		});
	}
}
