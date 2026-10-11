package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockGeneric;
import com.hbm.items.ItemBlockBase;
import com.hbm.lib.RefStrings;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;
import com.hbm.tileentity.machine.TileEntityFoundryOutlet;
import com.hbm.tileentity.machine.TileEntityFoundryTank;
import com.hbm.tileentity.machine.TileEntityMachineSiren;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * The machine slice's block registration unit: machine-family blocks and their tile entities all queue here,
 * no more stuffing the `ModBlocks` machine (that file belongs to someone else and is long enough).
 */
public class ModBlocksMachine implements IRegistrar {

	public static Block foundry_channel;
	public static Block foundry_tank;
	public static Block foundry_outlet;
	public static Block machine_siren;
	public static Block fusion_heater;
	public static Block watz_element;
	public static Block watz_cooler;
	public static Block hadron_coil_alloy;
	public static Block hadron_coil_gold;
	public static Block hadron_coil_neodymium;
	public static Block hadron_coil_magtung;
	public static Block hadron_coil_schrabidium;
	public static Block hadron_coil_schrabidate;
	public static Block hadron_coil_starmetal;
	public static Block hadron_coil_chlorophyte;
	public static Block hadron_coil_mese;
	public static Block pwr_heatex;
	public static Block pwr_heatsink;
	public static Block pwr_neutron_source;
	public static Block pwr_reflector;
	public static Block pwr_casing;
	public static Block pwr_port;

	@Override
	public void preInit() {

		//The foundry family: material flows from crucible or tank into the channel, then the outlet pours it into the mold/tank below
		foundry_channel = reg("foundry_channel", new FoundryChannel().setBlockName("foundry_channel").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F));
		foundry_tank = reg("foundry_tank", new FoundryTank().setBlockName("foundry_tank").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F));
		foundry_outlet = reg("foundry_outlet", new FoundryOutlet().setBlockName("foundry_outlet").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F));

		HbmRegistry.tile(TileEntityFoundryChannel.class, "foundry_channel");
		HbmRegistry.tile(TileEntityFoundryTank.class, "foundry_tank");
		HbmRegistry.tile(TileEntityFoundryOutlet.class, "foundry_outlet");

		//The siren: tile entity, container and GUI have all been in the tree; only this block was missing to create them, hence block and registration together
		machine_siren = reg("machine_siren", new MachineSiren().setBlockName("machine_siren").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F));
		HbmRegistry.tile(TileEntityMachineSiren.class, "machine_siren");

		//The fusion heater's and Watz furnace's two structural pieces: plain pillar blocks in the old version too (top textures separate),
		//no tile entities; machine logic waits for their TEs, this batch only completes blocks and item forms.
		//fusion_heater never entered the creative tab in the old version (setCreativeTab(null)); copied.
		fusion_heater = reg("fusion_heater", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":fusion_heater_side").setBlockName("fusion_heater").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		watz_element = reg("watz_element", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":watz_element_side").setBlockName("watz_element").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F));
		watz_cooler = reg("watz_cooler", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":watz_cooler_side").setBlockName("watz_cooler").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F));

		//Nine hadron collider coil tiers (alloy/gold/neodymium/magnetic tungsten/bismuth/plutonic acid/starpower/chlorophyll/mithril): factor 10 to 10000,
		//block identity and factor read by the collider side; item tooltips ride the old info.coil with thousands separators.
		//All nine ride connected textures: the block-side three steps live in BlockHadronCoil, enabled by the nine blockstates'
		//model=hbm:ct/blocks/hadron_coil_* (batch 21 landed plain block models first, before the CT infrastructure arrived, and
		hadron_coil_alloy = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 10).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_alloy").setBlockName("hadron_coil_alloy").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_alloy", ItemBlockBase::new);
		hadron_coil_gold = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 25).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_gold").setBlockName("hadron_coil_gold").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_gold", ItemBlockBase::new);
		hadron_coil_neodymium = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 50).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_neodymium").setBlockName("hadron_coil_neodymium").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_neodymium", ItemBlockBase::new);
		hadron_coil_magtung = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 100).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_magtung").setBlockName("hadron_coil_magtung").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_magtung", ItemBlockBase::new);
		hadron_coil_schrabidium = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 250).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_schrabidium").setBlockName("hadron_coil_schrabidium").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_schrabidium", ItemBlockBase::new);
		hadron_coil_schrabidate = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 500).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_schrabidate").setBlockName("hadron_coil_schrabidate").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_schrabidate", ItemBlockBase::new);
		hadron_coil_starmetal = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 1000).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_starmetal").setBlockName("hadron_coil_starmetal").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_starmetal", ItemBlockBase::new);
		hadron_coil_chlorophyte = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 2500).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_chlorophyte").setBlockName("hadron_coil_chlorophyte").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_chlorophyte", ItemBlockBase::new);
		hadron_coil_mese = HbmRegistry.blockWithItem(new BlockHadronCoil(Material.IRON, 10000).setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":hadron_coil_mese").setBlockName("hadron_coil_mese").setCreativeTab(ModBlocks.machineTab).setHardness(5.0F).setResistance(10.0F), "hadron_coil_mese", ItemBlockBase::new);

		//the reactor's six structural pieces (heat exchanger / radiator / neutron source / reflector / shell / port): in the old version they were
		//the real core logic lives in the reactor-family TEs, which haven't landed, so this batch only completes blocks and item forms,
		//also giving the existing BlockGenericTooltip base its first callers (the shift-for-info line rides ITooltipProvider)
		pwr_heatex = reg("pwr_heatex", new BlockGenericTooltip(Material.IRON).setBlockTextureName(RefStrings.MODID + ":pwr_heatex").setBlockName("pwr_heatex").setHardness(5.0F).setResistance(10.0F).setCreativeTab(ModBlocks.machineTab));
		pwr_heatsink = reg("pwr_heatsink", new BlockGenericTooltip(Material.IRON).setBlockTextureName(RefStrings.MODID + ":pwr_heatsink").setBlockName("pwr_heatsink").setHardness(5.0F).setResistance(10.0F).setCreativeTab(ModBlocks.machineTab));
		pwr_neutron_source = reg("pwr_neutron_source", new BlockGenericTooltip(Material.IRON).setBlockTextureName(RefStrings.MODID + ":pwr_neutron_source").setBlockName("pwr_neutron_source").setHardness(5.0F).setResistance(10.0F).setCreativeTab(ModBlocks.machineTab));
		pwr_reflector = reg("pwr_reflector", new BlockGenericTooltip(Material.IRON).setBlockTextureName(RefStrings.MODID + ":pwr_reflector").setBlockName("pwr_reflector").setHardness(5.0F).setResistance(10.0F).setCreativeTab(ModBlocks.machineTab));
		pwr_casing = reg("pwr_casing", new BlockGenericTooltip(Material.IRON).setBlockTextureName(RefStrings.MODID + ":pwr_casing").setBlockName("pwr_casing").setHardness(5.0F).setResistance(10.0F).setCreativeTab(ModBlocks.machineTab));
		pwr_port = reg("pwr_port", new BlockGenericTooltip(Material.IRON).setBlockTextureName(RefStrings.MODID + ":pwr_port").setBlockName("pwr_port").setHardness(5.0F).setResistance(10.0F).setCreativeTab(ModBlocks.machineTab));
	}

	/**
	 * Blocks and item forms queue together; the name must be a **bare name** (`getUnlocalizedName()` carries the `tile.` prefix,
	 * and using it as the registry name skews both models and lang keys). Multi-subtype families get their own ItemBlock.
	 */
	private static Block reg(String name, Block block) {
		return HbmRegistry.blockWithItem(block, name, ItemBlockBase::new);
	}
}
