package com.hbm.blocks;

import com.hbm.blocks.generic.BlockBobble;
import com.hbm.blocks.machine.WatzPump;
import com.hbm.blocks.machine.RailBooster;
import com.hbm.blocks.machine.RailGeneric;
import com.hbm.blocks.rail.RailNarrowCurve;
import com.hbm.blocks.rail.RailNarrowStraight;
import com.hbm.blocks.rail.RailStandardBuffer;
import com.hbm.blocks.rail.RailStandardCurveBase;
import com.hbm.blocks.rail.RailStandardCurveWide7;
import com.hbm.blocks.rail.RailStandardCurveWide9;
import com.hbm.blocks.rail.RailStandardRamp;
import com.hbm.blocks.rail.RailStandardSwitch;
import com.hbm.blocks.rail.RailStandardSwitchFlipped;
import com.hbm.blocks.rail.RailStandardStraight;
import com.hbm.blocks.rail.RailStandardStraightShort;
import com.hbm.blocks.generic.BlockNTMLadder;
import com.hbm.blocks.generic.BlockSpeedy;
import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.blocks.machine.BlockMassStorage;
import com.hbm.items.block.ItemBlockMassStorage;
import com.hbm.items.block.ItemBlockStorageCrate;
import com.hbm.blocks.generic.BlockNoDrop;
import com.hbm.blocks.generic.BlockGenericDrop;
import com.hbm.blocks.generic.BlockDragonProof;
import com.hbm.blocks.generic.BlockBeaconable;
import com.hbm.blocks.generic.BlockGeneric;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ItemBlockBase;
import com.hbm.items.block.ItemBlockMeta;
import com.hbm.config.GeneralConfig;
import com.hbm.creativetabs.BlockTab;
import com.hbm.creativetabs.MachineTab;
import com.hbm.creativetabs.MissileTab;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.registry.HbmRegistry;
import com.hbm.tileentity.machine.storage.TileEntityCrateDesh;
import com.hbm.tileentity.machine.storage.TileEntityCrateIron;
import com.hbm.tileentity.machine.storage.TileEntityCrateSteel;
import com.hbm.tileentity.machine.storage.TileEntityCrateTungsten;
import com.hbm.tileentity.machine.storage.TileEntitySafe;
import com.hbm.registry.IRegistrar;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;

/**
 * The block declaration layer.
 *
 * Only the family that doesn't lean on the multiblock base lives here for now (plain/single-texture blocks); each batch adds its family.
 * Registration queues in preInit; actual registration is the registry event's job.
 */
public class ModBlocks implements IRegistrar {

	/** The block tab and machine tab; the ammo/nuke and other tabs arrive with their own families */
	public static final CreativeTabs blockTab = new BlockTab("tabBlocks");
	public static final CreativeTabs machineTab = new MachineTab("tabMachine");
	public static final CreativeTabs missileTab = new MissileTab("tabMissile");

	public static Block structure_anchor;
	public static Block ore_nether_plutonium;
	public static Block ore_nether_tungsten;
	public static Block ore_nether_schrabidium;
	public static Block stone_gneiss;
	public static Block gneiss_brick;
	public static Block gneiss_tile;
	public static Block basalt_smooth;
	public static Block basalt_brick;
	public static Block basalt_polished;
	public static Block basalt_tiles;
	public static Block block_foam;
	public static Block deco_titanium;
	public static Block deco_rbmk;
	public static Block deco_rbmk_smooth;
	public static Block reinforced_brick;
	public static Block reinforced_light;
	public static Block reinforced_sand;
	public static Block reinforced_stone;
	public static Block brick_concrete_mossy;
	public static Block brick_concrete_cracked;
	public static Block brick_concrete_broken;
	public static Block brick_obsidian;
	public static Block brick_light;
	public static Block brick_compound;
	public static Block cmb_brick;
	public static Block cmb_brick_reinforced;
	public static Block brick_fire;
	public static Block meteor_polished;
	public static Block meteor_brick;
	public static Block meteor_brick_mossy;
	public static Block meteor_brick_cracked;
	public static Block meteor_brick_chiseled;
	public static Block moon_rock;
	public static Block duna_cobble;
	public static Block dry_ice;
	public static Block ferric_clay;
	public static Block eve_rock;
	public static Block ike_stone;
	public static Block dres_rock;
	public static Block moho_regolith;
	public static Block moho_stone;
	public static Block minmus_regolith;
	public static Block minmus_stone;
	public static Block minmus_smooth;
	public static Block brick_jungle;
	public static Block brick_jungle_cracked;
	public static Block brick_jungle_lava;
	public static Block tektite;
	public static Block ore_tektite_osmiridium;
	public static Block seal_frame;
	public static Block crystal_hardened;
	public static Block pink_planks;
	public static Block rubber_silt;
	public static Block vinyl_planks;
	public static Block pvc_planks;
	public static Block struct_launcher;
	public static Block struct_scaffold;

	/* The beaconable family of storage blocks (valid beacon bases) */
	public static Block block_titanium;
	public static Block block_sulfur;
	public static Block block_niter;
	public static Block block_copper;
	public static Block block_nickel;
	public static Block block_red_copper;
	public static Block block_osmiridium;
	public static Block block_tungsten;
	public static Block block_aluminium;
	public static Block block_fluorite;
	public static Block block_steel;
	public static Block block_tcalloy;
	public static Block block_cdalloy;
	public static Block block_lead;
	public static Block block_bismuth;
	public static Block block_cadmium;
	public static Block block_coltan;
	public static Block block_tantalium;
	public static Block block_niobium;
	public static Block block_beryllium;
	public static Block block_euphemium;
	public static Block block_dineutronium;
	public static Block block_magnetized_tungsten;
	public static Block block_combine_steel;
	public static Block block_desh;
	public static Block block_dura_steel;
	public static Block block_starmetal;
	public static Block block_polymer;
	public static Block block_bakelite;
	public static Block block_rubber;

	public static Block ore_tikite;
	public static Block asphalt;
	public static Block asphalt_light;
	public static Block duna_rock;
	public static Block ladder_sturdy;
	public static Block ladder_gold;
	public static Block ladder_copper;
	public static Block ladder_titanium;
	public static Block ladder_steel;
	public static Block oil_pipe;
	public static Block barricade;
	public static Block press_preheater;
	public static Block block_cobalt;
	public static Block block_zirconium;
	public static Block block_boron;
	public static Block block_lanthanium;
	public static Block block_australium;

	/* Fake multiblocks within two cells: beam segments and the Watz pump */
	public static Block dummy_beam;
	public static Block watz_pump;
	public static Block rail_narrow_straight;
	public static Block rail_large_straight;
	public static Block rail_large_straight_short;
	public static Block rail_large_ramp;
	public static Block rail_large_buffer;
	public static Block rail_narrow_curve;
	public static Block rail_large_curve;
	public static Block rail_large_curve_7;
	public static Block rail_large_curve_9;
	public static Block rail_large_switch;
	public static Block rail_large_switch_flipped;
	public static Block rail_wood;
	public static Block rail_narrow;
	public static Block rail_highspeed;
	public static Block rail_booster;
	public static Block bobblehead;

	public static Block crate_iron;
	public static Block crate_steel;
	public static Block crate_desh;
	public static Block crate_tungsten;
	public static Block safe;
	public static Block mass_storage;

	@Override
	public void preInit() {

		structure_anchor = reg("structure_anchor", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":structure_anchor").setBlockName("structure_anchor").setCreativeTab(null).setHardness(2.5F).setResistance(10.0F));

		ore_nether_plutonium = reg("ore_nether_plutonium", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_nether_plutonium").setBlockName("ore_nether_plutonium").setCreativeTab(blockTab).setHardness(0.4F).setResistance(10.0F));
		ore_nether_tungsten = reg("ore_nether_tungsten", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_nether_tungsten").setBlockName("ore_nether_tungsten").setCreativeTab(blockTab).setHardness(0.4F).setResistance(10.0F));
		ore_nether_schrabidium = reg("ore_nether_schrabidium", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_nether_schrabidium").setBlockName("ore_nether_schrabidium").setCreativeTab(blockTab).setHardness(15.0F).setResistance(600.0F));

		stone_gneiss = reg("stone_gneiss", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":stone_gneiss_var").setBlockName("stone_gneiss").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		gneiss_brick = reg("gneiss_brick", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":gneiss_brick").setBlockName("gneiss_brick").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		gneiss_tile = reg("gneiss_tile", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":gneiss_tile").setBlockName("gneiss_tile").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));

		basalt_smooth = reg("basalt_smooth", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":basalt_smooth").setBlockName("basalt_smooth").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		basalt_brick = reg("basalt_brick", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":basalt_brick").setBlockName("basalt_brick").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		basalt_polished = reg("basalt_polished", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":basalt_polished").setBlockName("basalt_polished").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		basalt_tiles = reg("basalt_tiles", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":basalt_tiles").setBlockName("basalt_tiles").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));

		block_foam = reg("block_foam", new BlockGeneric(Material.CRAFTED_SNOW).setBlockTextureName(RefStrings.MODID + ":foam").setStepSound(SoundType.SNOW).setBlockName("block_foam").setCreativeTab(blockTab).setHardness(0.5F).setResistance(0.0F));

		deco_titanium = reg("deco_titanium", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_titanium").setBlockName("deco_titanium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		deco_rbmk = reg("deco_rbmk", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":rbmk/rbmk_top").setBlockName("deco_rbmk").setCreativeTab(blockTab).setHardness(5.0F).setResistance(100.0F));
		deco_rbmk_smooth = reg("deco_rbmk_smooth", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":rbmk/rbmk_blank_top").setBlockName("deco_rbmk_smooth").setCreativeTab(blockTab).setHardness(5.0F).setResistance(100.0F));

		reinforced_brick = reg("reinforced_brick", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":reinforced_brick").setBlockName("reinforced_brick").setCreativeTab(blockTab).setHardness(15.0F).setResistance(300.0F));
		reinforced_light = reg("reinforced_light", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":reinforced_light").setBlockName("reinforced_light").setCreativeTab(blockTab).setLightLevel(1.0F).setHardness(15.0F).setResistance(80.0F));
		reinforced_sand = reg("reinforced_sand", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":reinforced_sand").setBlockName("reinforced_sand").setCreativeTab(blockTab).setHardness(15.0F).setResistance(40.0F));
		reinforced_stone = reg("reinforced_stone", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":reinforced_stone").setBlockName("reinforced_stone").setCreativeTab(blockTab).setHardness(15.0F).setResistance(100.0F));

		brick_concrete_mossy = reg("brick_concrete_mossy", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_concrete_mossy").setBlockName("brick_concrete_mossy").setCreativeTab(blockTab).setHardness(15.0F).setResistance(160.0F));
		brick_concrete_cracked = reg("brick_concrete_cracked", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_concrete_cracked").setBlockName("brick_concrete_cracked").setCreativeTab(blockTab).setHardness(15.0F).setResistance(60.0F));
		brick_concrete_broken = reg("brick_concrete_broken", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_concrete_broken").setBlockName("brick_concrete_broken").setCreativeTab(blockTab).setHardness(15.0F).setResistance(45.0F));

		brick_obsidian = reg("brick_obsidian", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_obsidian").setBlockName("brick_obsidian").setCreativeTab(blockTab).setLightOpacity(15).setHardness(15.0F).setResistance(120.0F));
		brick_light = reg("brick_light", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_light").setBlockName("brick_light").setCreativeTab(blockTab).setHardness(5.0F).setResistance(20.0F));
		brick_compound = reg("brick_compound", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_compound").setBlockName("brick_compound").setCreativeTab(blockTab).setHardness(15.0F).setResistance(400.0F));

		cmb_brick = reg("cmb_brick", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":cmb_brick").setBlockName("cmb_brick").setCreativeTab(blockTab).setHardness(25.0F).setResistance(5000.0F));
		cmb_brick_reinforced = reg("cmb_brick_reinforced", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":cmb_brick_reinforced").setBlockName("cmb_brick_reinforced").setCreativeTab(blockTab).setHardness(25.0F).setResistance(50000.0F));

		brick_fire = reg("brick_fire", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_fire").setBlockName("brick_fire").setCreativeTab(blockTab).setHardness(5.0F).setResistance(35.0F));

		meteor_polished = reg("meteor_polished", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_polished").setBlockName("meteor_polished").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F));
		meteor_brick = reg("meteor_brick", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_brick").setBlockName("meteor_brick").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F));
		meteor_brick_mossy = reg("meteor_brick_mossy", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_brick_mossy").setBlockName("meteor_brick_mossy").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F));
		meteor_brick_cracked = reg("meteor_brick_cracked", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_brick_cracked").setBlockName("meteor_brick_cracked").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F));
		meteor_brick_chiseled = reg("meteor_brick_chiseled", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_brick_chiseled").setBlockName("meteor_brick_chiseled").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F));

		moon_rock = reg("moon_rock", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":moon_rock").setStepSound(SoundType.STONE).setBlockName("moon_rock").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		duna_cobble = reg("duna_cobble", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":duna_cobble").setStepSound(SoundType.STONE).setBlockName("duna_cobble").setCreativeTab(blockTab).setHardness(2.0F).setResistance(10.0F));
		dry_ice = reg("dry_ice", new BlockGeneric(Material.ICE).setBlockTextureName(RefStrings.MODID + ":ice_dry").setStepSound(SoundType.STONE).setBlockName("dry_ice").setCreativeTab(blockTab).setHardness(0.5F));
		ferric_clay = reg("ferric_clay", new BlockGeneric(Material.CLAY).setBlockTextureName(RefStrings.MODID + ":ferric_clay").setStepSound(SoundType.GROUND).setBlockName("ferric_clay").setCreativeTab(blockTab).setHardness(0.6F));

		eve_rock = reg("eve_rock", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":eve_stone_2").setStepSound(SoundType.STONE).setBlockName("eve_rock").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		ike_stone = reg("ike_stone", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ike_stone").setStepSound(SoundType.STONE).setBlockName("ike_stone").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		dres_rock = reg("dres_rock", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":dresbase").setStepSound(SoundType.STONE).setBlockName("dres_rock").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		moho_regolith = reg("moho_regolith", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":moho_regolith").setStepSound(SoundType.STONE).setBlockName("moho_regolith").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		moho_stone = reg("moho_stone", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":moho_stone").setStepSound(SoundType.STONE).setBlockName("moho_stone").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		minmus_regolith = reg("minmus_regolith", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":minmus_regolith").setStepSound(SoundType.STONE).setBlockName("minmus_regolith").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		minmus_stone = reg("minmus_stone", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":minmus_stone").setStepSound(SoundType.STONE).setBlockName("minmus_stone").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		minmus_smooth = reg("minmus_smooth", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":minflat_regolith").setStepSound(SoundType.STONE).setBlockName("minmus_smooth").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));

		brick_jungle = reg("brick_jungle", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_jungle").setBlockName("brick_jungle").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F));
		brick_jungle_cracked = reg("brick_jungle_cracked", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_jungle_cracked").setBlockName("brick_jungle_cracked").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F));
		brick_jungle_lava = reg("brick_jungle_lava", new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_jungle_lava").setBlockName("brick_jungle_lava").setCreativeTab(blockTab).setHardness(15.0F).setResistance(360.0F).setLightLevel(5F/15F));

		tektite = reg("tektite", new BlockGeneric(Material.SAND).setBlockTextureName(RefStrings.MODID + ":tektite").setStepSound(SoundType.SAND).setBlockName("tektite").setCreativeTab(blockTab).setHardness(0.5F));
		ore_tektite_osmiridium = reg("ore_tektite_osmiridium", new BlockGeneric(Material.SAND).setBlockTextureName(RefStrings.MODID + ":ore_tektite_osmiridium").setStepSound(SoundType.SAND).setBlockName("ore_tektite_osmiridium").setCreativeTab(blockTab).setHardness(0.5F));

		seal_frame = reg("seal_frame", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":seal_frame").setBlockName("seal_frame").setHardness(10.0F).setResistance(100.0F).setCreativeTab(machineTab));
		crystal_hardened = reg("crystal_hardened", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":crystal_hardened").setBlockName("crystal_hardened").setHardness(15.0F).setResistance(Float.POSITIVE_INFINITY).setCreativeTab(null));
		pink_planks = reg("pink_planks", new BlockGeneric(Material.WOOD).setBlockTextureName(RefStrings.MODID + ":pink_planks").setStepSound(SoundType.WOOD).setBlockName("pink_planks").setCreativeTab(null));

		rubber_silt = reg("rubber_silt", new BlockGeneric(Material.SAND).setBlockTextureName(RefStrings.MODID + ":rubber_silt").setStepSound(SoundType.GROUND).setBlockName("rubber_silt").setCreativeTab(blockTab).setHardness(1.0F).setResistance(1.0F));
		vinyl_planks = reg("vinyl_planks", new BlockGeneric(Material.WOOD).setBlockTextureName(RefStrings.MODID + ":vinyl_planks").setStepSound(SoundType.WOOD).setBlockName("vinyl_planks").setCreativeTab(blockTab).setHardness(1.0F).setResistance(1.0F));
		pvc_planks = reg("pvc_planks", new BlockGeneric(Material.WOOD).setBlockTextureName(RefStrings.MODID + ":pvc_planks").setStepSound(SoundType.WOOD).setBlockName("pvc_planks").setCreativeTab(blockTab).setHardness(1.0F).setResistance(1.0F));

		struct_launcher = reg("struct_launcher", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":struct_launcher").setBlockName("struct_launcher").setHardness(5.0F).setResistance(10.0F).setCreativeTab(missileTab));
		struct_scaffold = reg("struct_scaffold", new BlockGeneric(Material.IRON).setBlockTextureName(RefStrings.MODID + ":struct_scaffold").setBlockName("struct_scaffold").setHardness(5.0F).setResistance(10.0F).setCreativeTab(missileTab));

		block_titanium = reg("block_titanium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_titanium").setStepSound(SoundType.METAL).setBlockName("block_titanium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(50.0F));
		block_sulfur = reg("block_sulfur", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_sulfur").setBlockName("block_sulfur").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		block_niter = reg("block_niter", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_niter").setBlockName("block_niter").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		block_copper = reg("block_copper", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_copper").setStepSound(SoundType.METAL).setBlockName("block_copper").setCreativeTab(blockTab).setHardness(5.0F).setResistance(20.0F));
		block_nickel = reg("block_nickel", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_nickel").setStepSound(SoundType.METAL).setBlockName("block_nickel").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		block_red_copper = reg("block_red_copper", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_red_copper").setStepSound(SoundType.METAL).setBlockName("block_red_copper").setCreativeTab(blockTab).setHardness(5.0F).setResistance(25.0F));
		block_osmiridium = reg("block_osmiridium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_osmiridium").setStepSound(SoundType.METAL).setBlockName("block_osmiridium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		block_tungsten = reg("block_tungsten", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_tungsten").setStepSound(SoundType.METAL).setBlockName("block_tungsten").setCreativeTab(blockTab).setHardness(5.0F).setResistance(20.0F));
		block_aluminium = reg("block_aluminium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_aluminium").setStepSound(SoundType.METAL).setBlockName("block_aluminium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(20.0F));
		block_fluorite = reg("block_fluorite", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_fluorite").setBlockName("block_fluorite").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		block_steel = reg("block_steel", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_steel").setStepSound(SoundType.METAL).setBlockName("block_steel").setCreativeTab(blockTab).setHardness(5.0F).setResistance(50.0F));
		block_tcalloy = reg("block_tcalloy", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_tcalloy").setStepSound(SoundType.METAL).setBlockName("block_tcalloy").setCreativeTab(blockTab).setHardness(5.0F).setResistance(70.0F));
		block_cdalloy = reg("block_cdalloy", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_cdalloy").setStepSound(SoundType.METAL).setBlockName("block_cdalloy").setCreativeTab(blockTab).setHardness(5.0F).setResistance(70.0F));
		block_lead = reg("block_lead", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_lead").setStepSound(SoundType.METAL).setBlockName("block_lead").setCreativeTab(blockTab).setHardness(5.0F).setResistance(50.0F));
		block_bismuth = reg("block_bismuth", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_bismuth").setStepSound(SoundType.METAL).setBlockName("block_bismuth").setCreativeTab(blockTab).setHardness(5.0F).setResistance(90.0F));
		block_cadmium = reg("block_cadmium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_cadmium").setStepSound(SoundType.METAL).setBlockName("block_cadmium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(90.0F));
		block_coltan = reg("block_coltan", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_coltan").setStepSound(SoundType.METAL).setBlockName("block_coltan").setCreativeTab(blockTab).setHardness(5.0F).setResistance(50.0F));
		block_tantalium = reg("block_tantalium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_tantalium").setStepSound(SoundType.METAL).setBlockName("block_tantalium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(50.0F));
		block_niobium = reg("block_niobium", new BlockBeaconable(Material.IRON).setStepSound(SoundType.METAL).setBlockName("block_niobium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(50.0F));
		block_beryllium = reg("block_beryllium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_beryllium").setStepSound(SoundType.METAL).setBlockName("block_beryllium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(20.0F));
		block_euphemium = reg("block_euphemium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_euphemium").setStepSound(SoundType.METAL).setBlockName("block_euphemium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(60000.0F));
		block_dineutronium = reg("block_dineutronium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_dineutronium").setStepSound(SoundType.METAL).setBlockName("block_dineutronium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(60000.0F));
		block_magnetized_tungsten = reg("block_magnetized_tungsten", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_magnetized_tungsten").setStepSound(SoundType.METAL).setBlockName("block_magnetized_tungsten").setCreativeTab(blockTab).setHardness(5.0F).setResistance(75.0F));
		block_combine_steel = reg("block_combine_steel", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_combine_steel").setStepSound(SoundType.METAL).setBlockName("block_combine_steel").setCreativeTab(blockTab).setHardness(5.0F).setResistance(600.0F));
		block_desh = reg("block_desh", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_desh").setStepSound(SoundType.METAL).setBlockName("block_desh").setCreativeTab(blockTab).setHardness(5.0F).setResistance(300.0F));
		block_dura_steel = reg("block_dura_steel", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_dura_steel").setStepSound(SoundType.METAL).setBlockName("block_dura_steel").setCreativeTab(blockTab).setHardness(5.0F).setResistance(200.0F));
		block_starmetal = reg("block_starmetal", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_starmetal").setStepSound(SoundType.METAL).setBlockName("block_starmetal").setCreativeTab(blockTab).setHardness(5.0F).setResistance(400.0F));
		block_polymer = reg("block_polymer", new BlockBeaconable(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":block_polymer").setStepSound(SoundType.STONE).setBlockName("block_polymer").setCreativeTab(blockTab).setHardness(3.0F).setResistance(10.0F));
		block_bakelite = reg("block_bakelite", new BlockBeaconable(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":block_bakelite").setStepSound(SoundType.STONE).setBlockName("block_bakelite").setCreativeTab(blockTab).setHardness(3.0F).setResistance(5.0F));
		block_rubber = reg("block_rubber", new BlockBeaconable(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":block_rubber").setStepSound(SoundType.STONE).setBlockName("block_rubber").setCreativeTab(blockTab).setHardness(3.0F).setResistance(15.0F));

		ore_tikite = reg("ore_tikite", new BlockDragonProof(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_tikite_alt").setBlockName("ore_tikite").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		asphalt = reg("asphalt", new BlockSpeedy(Material.ROCK, 1.5).setBlockTextureName(RefStrings.MODID + ":asphalt").setBlockName("asphalt").setCreativeTab(blockTab).setHardness(15.0F).setResistance(120.0F));
		asphalt_light = reg("asphalt_light", new BlockSpeedy(Material.ROCK, 1.5).setBlockTextureName(RefStrings.MODID + ":asphalt_light").setBlockName("asphalt_light").setCreativeTab(blockTab).setLightLevel(1F).setHardness(15.0F).setResistance(120.0F));
		duna_rock = reg("duna_rock", new BlockGenericDrop(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":duna_rock").setStepSound(SoundType.STONE).setBlockName("duna_rock").setCreativeTab(blockTab).setHardness(1.5F).setResistance(10.0F));
		ladder_sturdy = reg("ladder_sturdy", new BlockNTMLadder().setBlockTextureName(RefStrings.MODID + ":ladder_sturdy").setBlockName("ladder_sturdy").setCreativeTab(blockTab).setHardness(0.25F).setResistance(2.0F));
		ladder_gold = reg("ladder_gold", new BlockNTMLadder().setBlockTextureName(RefStrings.MODID + ":ladder_gold").setBlockName("ladder_gold").setCreativeTab(blockTab).setHardness(0.25F).setResistance(2.0F));
		ladder_copper = reg("ladder_copper", new BlockNTMLadder().setBlockTextureName(RefStrings.MODID + ":ladder_copper").setBlockName("ladder_copper").setCreativeTab(blockTab).setHardness(0.25F).setResistance(2.0F));
		ladder_titanium = reg("ladder_titanium", new BlockNTMLadder().setBlockTextureName(RefStrings.MODID + ":ladder_titanium").setBlockName("ladder_titanium").setCreativeTab(blockTab).setHardness(0.25F).setResistance(2.0F));
		ladder_steel = reg("ladder_steel", new BlockNTMLadder().setBlockTextureName(RefStrings.MODID + ":ladder_steel").setBlockName("ladder_steel").setCreativeTab(blockTab).setHardness(0.25F).setResistance(2.0F));
		oil_pipe = reg("oil_pipe", new BlockNoDrop(Material.IRON).setBlockTextureName(RefStrings.MODID + ":oil_pipe").setBlockName("oil_pipe").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		barricade = reg("barricade", new BlockNoDrop(Material.SAND).setBlockTextureName(RefStrings.MODID + ":barricade").setBlockName("barricade").setCreativeTab(null).setHardness(1.0F).setResistance(2.5F));
		press_preheater = reg("press_preheater", new BlockBase(Material.IRON).setBlockTextureName(RefStrings.MODID + ":press_preheater").setBlockName("press_preheater").setCreativeTab(machineTab).setHardness(5.0F).setResistance(10.0F));
		block_cobalt = reg("block_cobalt", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_cobalt").setBlockName("block_cobalt").setCreativeTab(blockTab).setHardness(5.0F).setResistance(50.0F));
		block_zirconium = reg("block_zirconium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_zirconium").setStepSound(SoundType.METAL).setBlockName("block_zirconium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(30.0F));
		block_boron = reg("block_boron", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_boron").setStepSound(SoundType.METAL).setBlockName("block_boron").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		block_lanthanium = reg("block_lanthanium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_lanthanium").setStepSound(SoundType.METAL).setBlockName("block_lanthanium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));
		block_australium = reg("block_australium", new BlockBeaconable(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_australium").setBlockName("block_australium").setCreativeTab(blockTab).setHardness(5.0F).setResistance(10.0F));

		dummy_beam = reg("dummy_beam", new BlockDummyableBeam(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_steel_machine").setBlockName("dummy_beam").setHardness(10.0F).setResistance(20.0F));
		watz_pump = reg("watz_pump", new WatzPump().setBlockTextureName(RefStrings.MODID + ":block_steel").setBlockName("watz_pump").setCreativeTab(machineTab).setHardness(5.0F).setResistance(10.0F));
		HbmRegistry.tile(WatzPump.TileEntityWatzPump.class, "watz_pump");

		// The bobblehead has 28 tiers so its item form must carry meta; hence no reg() here — an ItemBlockMeta is specified directly
		bobblehead = HbmRegistry.blockWithItem(new BlockBobble().setBlockTextureName(RefStrings.MODID + ":block_steel").setBlockName("bobblehead").setCreativeTab(blockTab).setHardness(0.0F).setResistance(0.0F), "bobblehead", ItemBlockMeta::new);
		HbmRegistry.tile(BlockBobble.TileEntityBobble.class, "bobblehead");

		rail_narrow_straight = reg("rail_narrow_straight", new RailNarrowStraight().setBlockTextureName(RefStrings.MODID + ":rail_narrow_neo").setBlockName("rail_narrow_straight").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		rail_large_straight = reg("rail_large_straight", new RailStandardStraight().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_straight").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		rail_large_straight_short = reg("rail_large_straight_short", new RailStandardStraightShort().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_straight_short").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
	
		rail_large_ramp = reg("rail_large_ramp", new RailStandardRamp().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_ramp").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		rail_large_buffer = reg("rail_large_buffer", new RailStandardBuffer().setBlockTextureName(RefStrings.MODID + ":rail_standard_buffer").setBlockName("rail_large_buffer").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));

		rail_narrow_curve = reg("rail_narrow_curve", new RailNarrowCurve().setBlockTextureName(RefStrings.MODID + ":rail_narrow_neo").setBlockName("rail_narrow_curve").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		rail_large_curve = reg("rail_large_curve", new RailStandardCurveBase().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_curve").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		rail_large_curve_7 = reg("rail_large_curve_7", new RailStandardCurveWide7().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_curve_7").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		rail_large_curve_9 = reg("rail_large_curve_9", new RailStandardCurveWide9().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_curve_9").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));

		rail_wood = reg("rail_wood", new RailGeneric("axe", 0).setMaxSpeed(0.2F).setBlockTextureName(RefStrings.MODID + ":rail_wood").setBlockName("rail_wood").setCreativeTab(CreativeTabs.TRANSPORTATION).setHardness(5.0F).setResistance(10.0F));
		rail_narrow = reg("rail_narrow", new RailGeneric("pickaxe", 0).setBlockTextureName(RefStrings.MODID + ":rail_narrow").setBlockName("rail_narrow").setCreativeTab(CreativeTabs.TRANSPORTATION).setHardness(5.0F).setResistance(10.0F));
		rail_highspeed = reg("rail_highspeed", new RailGeneric("pickaxe", 0).setMaxSpeed(1F).setFlexible(false).setBlockTextureName(RefStrings.MODID + ":rail_highspeed").setBlockName("rail_highspeed").setCreativeTab(CreativeTabs.TRANSPORTATION).setHardness(5.0F).setResistance(10.0F));
		rail_booster = reg("rail_booster", new RailBooster("pickaxe", 0).setBlockTextureName(RefStrings.MODID + ":rail_booster").setBlockName("rail_booster").setCreativeTab(CreativeTabs.TRANSPORTATION).setHardness(5.0F).setResistance(10.0F));
		rail_large_switch = reg("rail_large_switch", new RailStandardSwitch().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_switch").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		rail_large_switch_flipped = reg("rail_large_switch_flipped", new RailStandardSwitchFlipped().setBlockTextureName(RefStrings.MODID + ":rail_standard_straight").setBlockName("rail_large_switch_flipped").setCreativeTab(null).setHardness(5.0F).setResistance(10.0F));
		//The crate family's blocks only answer "which tile entity"; slot counts and GUIs live on that side; the item form uses the crate's own ItemBlock (hand-openable,
		crate_iron = HbmRegistry.blockWithItem(new BlockStorageCrate(Material.IRON, TileEntityCrateIron::new).setBlockName("crate_iron").setCreativeTab(machineTab).setHardness(5.0F).setResistance(10.0F), "crate_iron", ItemBlockStorageCrate::new);
		crate_steel = HbmRegistry.blockWithItem(new BlockStorageCrate(Material.IRON, TileEntityCrateSteel::new).setBlockName("crate_steel").setCreativeTab(machineTab).setHardness(5.0F).setResistance(10.0F), "crate_steel", ItemBlockStorageCrate::new);
		crate_desh = HbmRegistry.blockWithItem(new BlockStorageCrate(Material.IRON, TileEntityCrateDesh::new).setBlockName("crate_desh").setCreativeTab(machineTab).setHardness(5.0F).setResistance(10.0F), "crate_desh", ItemBlockStorageCrate::new);
		crate_tungsten = HbmRegistry.blockWithItem(new BlockStorageCrate(Material.IRON, TileEntityCrateTungsten::new).setBlockName("crate_tungsten").setCreativeTab(machineTab).setHardness(7.5F).setResistance(300.0F), "crate_tungsten", ItemBlockStorageCrate::new);
		safe = HbmRegistry.blockWithItem(new BlockStorageCrate(Material.IRON, TileEntitySafe::new).setBlockName("safe").setCreativeTab(machineTab).setHardness(7.5F).setResistance(10000.0F), "safe", ItemBlockStorageCrate::new);

		mass_storage = HbmRegistry.blockWithItem(new BlockMassStorage().setBlockName("mass_storage").setCreativeTab(machineTab).setHardness(5.0F).setResistance(10.0F), "mass_storage", ItemBlockMassStorage::new);

		HbmRegistry.tile(RailStandardSwitch.TileEntityRailSwitch.class, "rail_large_switch");
}

	/**
	 * Blocks and their item forms queue together.
	 *
	 * The name must be the **bare name**: 1.12.2's `Block.getUnlocalizedName()` prepends `tile.` itself
	 * (the bytecode literally does `ldc "tile."` then appends), so using it as the registry name yields `hbm:tile.deco_rbmk`,
	 * sending models hunting for `models/item/tile.deco_rbmk.json` and skewing the lang keys — changed only after measuring it.
	 * Item forms uniformly use ItemBlockBase; multi-subtype families lean on it to ask the block for names; the duplicate-registry-name affair
	 * was fixed at the source by the registration layer ("only set when the product has no name"), so no more detours here.
	 */
	private static Block reg(String name, Block block) {
		return HbmRegistry.blockWithItem(block, name, ItemBlockBase::new);
	}

	/** Debug-gated: a misplaced multiblock facing is green across compile, startup and static checks — only an active probe catches it */
	@Override
	public void init() {
		MinecraftForge.EVENT_BUS.register(new DummyPlacementProbe());	// The probe checks for its marker file itself; without one it stays silent
		if(GeneralConfig.enableDebugMode) {
			MainRegistry.logger.info("Multiblock facing self-test:{}", BlockDummyable.selfCheck());
			MainRegistry.logger.info("Material shape self-test:{}", Mats.selfCheck());
		}
	}
}
