package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ItemBlockBase;
import com.hbm.items.block.ItemBlockMeta;
import com.hbm.items.block.ItemBlockNTMSand;
import com.hbm.lib.RefStrings;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockRenderLayer;

/**
 * The generic family's block registration unit: everything queues from here, no more stuffing the `ModBlocks` machine.
 */
public class ModBlocksGeneric implements IRegistrar {

	public static Block sand_mix;
	public static Block fallout;
	public static Block salted_fallout;
	public static Block gneiss_chiseled;
	public static Block basalt;
	public static Block block_smore;
	public static Block meteor_battery;
	public static Block concrete_smooth;
	public static Block concrete;
	public static Block concrete_asbestos;
	public static Block concrete_rebar;
	public static Block brick_concrete;
	public static Block ducrete_smooth;
	public static Block ducrete;
	public static Block brick_ducrete;
	public static Block reinforced_ducrete;
	public static Block stone_depth;
	public static Block depth_brick;
	public static Block depth_tiles;
	public static Block depth_nether_brick;
	public static Block depth_nether_tiles;
	public static Block depth_dnt;
	public static Block stone_depth_nether;
	public static Block ore_oil_sand;
	public static Block block_scrap;
	public static Block block_electrical_scrap;
	public static Block gravel_obsidian;
	public static Block gravel_diamond;
	public static Block concrete_super_broken;
	public static Block moon_turf;
	public static Block duna_sands;
	public static Block eve_silt;
	public static Block dirt_dead;
	public static Block dirt_oily;
	public static Block sand_dirty;
	public static Block sand_dirty_red;
	public static Block stone_cracked;
	public static Block vinyl_sand;
	public static Block block_schrabidium_cluster;
	public static Block block_euphemium_cluster;
	public static Block block_insulator;
	public static Block block_fiberglass;
	public static Block block_tritium;
	public static Block concrete_pillar;
	public static Block meteor_pillar;
	public static Block pink_log;
	public static Block vinyl_log;
	public static Block pvc_log;
	public static Block lattice_log;
	public static Block bf_log;
	public static Block primed_log;
	public static Block eu_log;
	public static Block ore_nether_sulfur;
	public static Block ore_nether_fire;
	public static Block ore_nether_cobalt;
	public static Block ore_gneiss_iron;
	public static Block ore_gneiss_gold;
	public static Block ore_gneiss_copper;
	public static Block ore_gneiss_lithium;
	public static Block ore_gneiss_schrabidium;
	public static Block ore_gneiss_rare;
	public static Block ore_gneiss_gas;
	public static Block block_meteor;
	public static Block block_meteor_cobble;
	public static Block block_meteor_broken;
	public static Block block_meteor_molten;
	public static Block brick_jungle_ooze;
	public static Block brick_jungle_mystic;
	public static Block waste_planks;
	public static Block frozen_dirt;
	public static Block frozen_planks;
	public static Block concrete_slab;
	public static Block concrete_double_slab;
	public static Block concrete_brick_slab;
	public static Block concrete_brick_double_slab;
	public static Block brick_slab;
	public static Block brick_double_slab;
	public static Block concrete_smooth_stairs;
	public static Block concrete_stairs;
	public static Block concrete_asbestos_stairs;
	public static Block ducrete_smooth_stairs;
	public static Block ducrete_stairs;
	public static Block brick_concrete_stairs;
	public static Block brick_concrete_mossy_stairs;
	public static Block brick_concrete_cracked_stairs;
	public static Block brick_concrete_broken_stairs;
	public static Block brick_ducrete_stairs;
	public static Block reinforced_stone_stairs;
	public static Block reinforced_brick_stairs;
	public static Block brick_obsidian_stairs;
	public static Block brick_light_stairs;
	public static Block brick_compound_stairs;
	public static Block brick_asbestos_stairs;
	public static Block brick_fire_stairs;
	public static Block pink_stairs;
	public static Block ore_uranium_scorched;
	public static Block ore_nether_uranium;
	public static Block ore_nether_uranium_scorched;
	public static Block ore_gneiss_uranium;
	public static Block ore_gneiss_uranium_scorched;
	public static Block ore_gneiss_asbestos;
	public static Block ancient_scrap;
	public static Block block_corium_cobble;
	public static Block block_asbestos;
	public static Block deco_asbestos;
	public static Block deco_red_copper;
	public static Block deco_tungsten;
	public static Block deco_aluminium;
	public static Block deco_steel;
	public static Block deco_rusty_steel;
	public static Block deco_lead;
	public static Block deco_beryllium;
	public static Block deco_stainless;
	public static Block laythe_root;
	public static Block platemetal;
	public static Block brick_asbestos;
	public static Block tile_lab;
	public static Block tile_lab_cracked;
	public static Block tile_lab_broken;
	public static Block ore_depth_cinnebar;
	public static Block ore_depth_zirconium;
	public static Block ore_depth_borax;
	public static Block cluster_depth_iron;
	public static Block cluster_depth_titanium;
	public static Block cluster_depth_tungsten;
	public static Block cluster_iron;
	public static Block cluster_titanium;
	public static Block cluster_aluminium;
	public static Block cluster_copper;
	public static Block ore_depth_nether_neodymium;
	public static Block block_uranium;
	public static Block block_u233;
	public static Block block_u235;
	public static Block block_u238;
	public static Block block_uranium_fuel;
	public static Block block_thorium;
	public static Block block_thorium_fuel;
	public static Block block_neptunium;
	public static Block block_mox_fuel;
	public static Block block_plutonium;
	public static Block block_pu239;
	public static Block block_pu240;
	public static Block block_pu_mix;
	public static Block block_plutonium_fuel;
	public static Block block_trinitite;
	public static Block block_corium;
	public static Block block_schraranium;
	public static Block block_schrabidium;
	public static Block block_schrabidate;
	public static Block block_solinium;
	public static Block block_schrabidium_fuel;
	public static Block block_yellowcake;
	public static Block block_white_phosphorus;
	public static Block block_red_phosphorus;
	public static Block block_fallout;
	public static Block block_ra226;
	public static Block block_actinium;

	//the nuclear waste trio (plain/painted/vitrified): hazardous-material base + drifting dense radon into neighbors, see BlockNuclearWaste
	public static Block block_waste;
	public static Block block_waste_painted;
	public static Block block_waste_vitrified;

	//the four coordinate-tinted pieces (tumor/sub-flake weathered rock/waste vitrified two-tone): the old version hand-hashed position to pick an IIcon in the block class,
	//this version hands that to weighted blockstate variants (seeded by block position at render), so all four use the plain base
	public static Block tumor;
	public static Block ike_regolith;
	public static Block waste_trinitite;
	public static Block waste_trinitite_red;

	//the two hot ones: beyond radiation they also vent steam clouds (see BlockHotHazard)
	public static Block block_polonium;
	public static Block block_pu238;

	//one steel trapdoor: rides the vanilla BlockTrapDoor line (see BlockNTMTrapdoor)
	public static Block trapdoor_steel;

	//two pink wood slabs: single material, two half tiers (see BlockPinkSlab)
	public static Block pink_slab;
	public static Block pink_double_slab;

	//two reinforced glass panes: vanilla pane connection and collision line, differing only in "drops itself or not" and one light tier (see BlockNTMGlassPane)
	public static Block reinforced_glass_pane;
	public static Block reinforced_laminate_pane;
	//ten connected glass pieces: looks ride the BlockBreakable line, this family manages "drops itself / which render layer / the three connect steps" (see BlockNTMGlassCT)
	public static Block reinforced_glass;
	public static Block reinforced_laminate;
	public static Block glass_boron;
	public static Block glass_lead;
	public static Block glass_uranium;
	public static Block glass_trinitite;
	public static Block glass_polonium;
	public static Block glass_ash;
	public static Block glass_quartz;
	public static Block glass_polarized;

	//two reinforced lamps: the off and on states point at each other; powered lights up, unpowered extinguishes after four ticks (see BlockNTMLamp)
	public static Block reinforced_lamp_off;
	public static Block reinforced_lamp_on;

	//four tritium lamps (green and blue, one off and one lit each): same base as the reinforced lamp, differing in material, step sound and tier
	public static Block lamp_tritium_green_off;
	public static Block lamp_tritium_green_on;
	public static Block lamp_tritium_blue_off;
	public static Block lamp_tritium_blue_on;

	//six barbed wire brothers: one block class, six effect tiers (see BlockBarbedWire.Effect)
	public static Block barbed_wire;
	public static Block barbed_wire_fire;
	public static Block barbed_wire_poison;
	public static Block barbed_wire_acid;
	public static Block barbed_wire_wither;
	public static Block barbed_wire_ultradeath;

	//the four wasteland grounds: one class (BlockWasteEarth), branched by instance identity — trample effects, spread and decay all in the class
	public static Block waste_earth;
	public static Block waste_mycelium;
	public static Block frozen_grass;
	public static Block burning_earth;

	//meteorite scorched earth (the only BlockDirt-family piece the old version registered): bright enough and it scorches the regular dirt beneath — where burning_earth ends up after burning
	public static Block impact_dirt;

	//the mushroom that grows on the wasteland by itself (the only BlockMush-family piece): it survives standing on the seven wasteland grounds, and is where wasteland-grown mushrooms end up
	public static Block mush;

	//stalactite and stalagmite pair (two instances of the old BlockStalagmite family): one shared eight-tier material table; facing is instance identity, not a property
	public static Block stalactite;
	public static Block stalagmite;

	//Four surface layers (four instances of the old BlockLayering family): one shared eight-step thickness (a thickness property), differing only in material, sound, texture and whether they can be covered over
	public static Block foam_layer;
	public static Block sand_boron_layer;
	public static Block leaves_layer;
	public static Block oil_spill;

	@Override
	public void preInit() {

		//mixed sand: five subtypes, needs an ItemBlock so each meta hangs its own model
		sand_mix = HbmRegistry.blockWithItem(new BlockNTMSand(Material.SAND).setBlockName("sand_mix").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "sand_mix", ItemBlockNTMSand::new);

		// fallout's two tiers: block registry only, no ItemBlock. The hbm:fallout **item** name is already taken by the grey product
		// (that parts piece in ItemsGeneric); adding an ItemBlock would put a second same-named entry into the item registry ⇒ Forge throws Duplicate registration outright.
		// The old version had separate id spaces for blocks and items so it never collided; 1.12.2 registers by name, hence the crash. Drops come from the hbm:blocks/fallout table.
		fallout = HbmRegistry.block(new BlockFallout(Material.SAND, false).setStepSound(SoundType.GROUND).setBlockTextureName(RefStrings.MODID + ":ash").setBlockName("fallout").setCreativeTab(ModBlocks.blockTab).setHardness(0.1F).setLightOpacity(0), "fallout");
		salted_fallout = HbmRegistry.block(new BlockFallout(Material.SNOW, true).setStepSound(SoundType.GROUND).setBlockTextureName(RefStrings.MODID + ":ash").setBlockName("salted_fallout").setCreativeTab(ModBlocks.blockTab).setHardness(0.1F).setLightOpacity(0), "salted_fallout");

		//four pillar decos (the old BlockPillar split top/side textures in code; here those two faces belong to models/block/*.json,
		//so plain BlockGeneric suffices; the models were reworked to cube_column in the same batch, each top getting its own *_top)
		gneiss_chiseled = HbmRegistry.blockWithItem(new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":gneiss_chiseled").setBlockName("gneiss_chiseled").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "gneiss_chiseled", ItemBlockBase::new);
		basalt = HbmRegistry.blockWithItem(new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":basalt").setBlockName("basalt").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "basalt", ItemBlockBase::new);
		block_smore = HbmRegistry.blockWithItem(new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":block_smore_side").setBlockName("block_smore").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(600.0F), "block_smore", ItemBlockBase::new);
		meteor_battery = HbmRegistry.blockWithItem(new BlockGeneric(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_spawner_side").setBlockName("meteor_battery").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F), "meteor_battery", ItemBlockBase::new);

		//nine concrete and ducrete building blocks: the old batch already used BlockNoSpawn, differing only in no-spawning plus a red-text tooltip,
		//textures from their own models/block/*.json (all cube_all), no block logic, so only block and item forms land.
		//concrete_smooth uses the concrete texture and concrete uses concrete_tile, ducrete likewise — don't match names to textures by intuition.
		concrete_smooth = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":concrete").setBlockName("concrete_smooth").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(140.0F), "concrete_smooth", ItemBlockBase::new);
		concrete = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":concrete_tile").setBlockName("concrete").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(140.0F), "concrete", ItemBlockBase::new);
		concrete_asbestos = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":concrete_asbestos").setBlockName("concrete_asbestos").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(150.0F), "concrete_asbestos", ItemBlockBase::new);
		concrete_rebar = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":concrete_rebar").setBlockName("concrete_rebar").setCreativeTab(ModBlocks.blockTab).setHardness(50.0F).setResistance(240.0F), "concrete_rebar", ItemBlockBase::new);
		brick_concrete = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_concrete").setBlockName("brick_concrete").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(160.0F), "brick_concrete", ItemBlockBase::new);
		ducrete_smooth = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ducrete").setBlockName("ducrete_smooth").setCreativeTab(ModBlocks.blockTab).setHardness(20.0F).setResistance(500.0F), "ducrete_smooth", ItemBlockBase::new);
		ducrete = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ducrete_tile").setBlockName("ducrete").setCreativeTab(ModBlocks.blockTab).setHardness(20.0F).setResistance(500.0F), "ducrete", ItemBlockBase::new);
		brick_ducrete = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_ducrete").setBlockName("brick_ducrete").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(750.0F), "brick_ducrete", ItemBlockBase::new);
		reinforced_ducrete = HbmRegistry.blockWithItem(new BlockNoSpawn(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":reinforced_ducrete").setBlockName("reinforced_ducrete").setCreativeTab(ModBlocks.blockTab).setHardness(20.0F).setResistance(1000.0F), "reinforced_ducrete", ItemBlockBase::new);

		//seven depth-rock family pieces: in the old version all seven were bare BlockDepth (unmineable, explosives only), with only depth_dnt pushing blast resistance to 60000.
		//No hardness given: setBlockUnbreakable() already ran in the constructor; writing a number would mislead.
		//The ore_depth_* pieces on the same band (BlockDepthOre) drop ore items, which wants loot tables — landing with the ore family batch.
		stone_depth = HbmRegistry.blockWithItem(new BlockDepth().setBlockTextureName(RefStrings.MODID + ":stone_depth").setBlockName("stone_depth").setCreativeTab(ModBlocks.blockTab), "stone_depth", ItemBlockBase::new);
		depth_brick = HbmRegistry.blockWithItem(new BlockDepth().setBlockTextureName(RefStrings.MODID + ":depth_brick").setBlockName("depth_brick").setCreativeTab(ModBlocks.blockTab), "depth_brick", ItemBlockBase::new);
		depth_tiles = HbmRegistry.blockWithItem(new BlockDepth().setBlockTextureName(RefStrings.MODID + ":depth_tiles").setBlockName("depth_tiles").setCreativeTab(ModBlocks.blockTab), "depth_tiles", ItemBlockBase::new);
		depth_nether_brick = HbmRegistry.blockWithItem(new BlockDepth().setBlockTextureName(RefStrings.MODID + ":depth_nether_brick").setBlockName("depth_nether_brick").setCreativeTab(ModBlocks.blockTab), "depth_nether_brick", ItemBlockBase::new);
		depth_nether_tiles = HbmRegistry.blockWithItem(new BlockDepth().setBlockTextureName(RefStrings.MODID + ":depth_nether_tiles").setBlockName("depth_nether_tiles").setCreativeTab(ModBlocks.blockTab), "depth_nether_tiles", ItemBlockBase::new);
		depth_dnt = HbmRegistry.blockWithItem(new BlockDepth().setBlockTextureName(RefStrings.MODID + ":depth_dnt").setBlockName("depth_dnt").setCreativeTab(ModBlocks.blockTab).setResistance(60000.0F), "depth_dnt", ItemBlockBase::new);
		stone_depth_nether = HbmRegistry.blockWithItem(new BlockDepth().setBlockTextureName(RefStrings.MODID + ":stone_depth_nether").setBlockName("stone_depth_nether").setCreativeTab(ModBlocks.blockTab), "stone_depth_nether", ItemBlockBase::new);

		//fifteen gravity blocks (sand, gravel, scrap, moon soil and friends): the old version newed vanilla's BlockFalling directly; this version gave it a thin layer,
		//BlockFallingMod — 1.12.2's setSoundType is protected, so sounds can't be set from outside the package, and naming has to go through IBlockNaming.
		//The falling itself is vanilla: unsupported below turns into EntityFallingBlock, landing restores the block, so no tile entities in this family.
		//laythe_silt used BlockFallingTint in the old version (biome tinting, a render-side concern); skipped this batch until the tinting line is through.
		//block_electrical_scrap and vinyl_sand use textures named differently (electrical_scrap, sand_vinyl); don't search by block name.
		ore_oil_sand = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":ore_oil_sand_alt").setStepSound(SoundType.SAND).setBlockName("ore_oil_sand").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(1.0F), "ore_oil_sand", ItemBlockBase::new);
		block_scrap = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":block_scrap").setStepSound(SoundType.GROUND).setBlockName("block_scrap").setCreativeTab(ModBlocks.blockTab).setHardness(2.5F).setResistance(5.0F), "block_scrap", ItemBlockBase::new);
		block_electrical_scrap = HbmRegistry.blockWithItem(new BlockFallingMod(Material.IRON).setBlockTextureName(RefStrings.MODID + ":electrical_scrap").setStepSound(SoundType.METAL).setBlockName("block_electrical_scrap").setCreativeTab(ModBlocks.blockTab).setHardness(2.5F).setResistance(5.0F), "block_electrical_scrap", ItemBlockBase::new);
		gravel_obsidian = HbmRegistry.blockWithItem(new BlockFallingMod(Material.IRON).setBlockTextureName(RefStrings.MODID + ":gravel_obsidian").setStepSound(SoundType.GROUND).setBlockName("gravel_obsidian").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(240.0F), "gravel_obsidian", ItemBlockBase::new);
		gravel_diamond = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":gravel_diamond").setStepSound(SoundType.GROUND).setBlockName("gravel_diamond").setCreativeTab(ModBlocks.blockTab).setHardness(0.6F), "gravel_diamond", ItemBlockBase::new);
		concrete_super_broken = HbmRegistry.blockWithItem(new BlockFallingMod(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":concrete_super_broken").setBlockName("concrete_super_broken").setCreativeTab(ModBlocks.blockTab).setHardness(10.0F).setResistance(20.0F), "concrete_super_broken", ItemBlockBase::new);
		moon_turf = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":moon_turf").setStepSound(SoundType.SAND).setBlockName("moon_turf").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "moon_turf", ItemBlockBase::new);
		duna_sands = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":duna_sands").setStepSound(SoundType.SAND).setBlockName("duna_sands").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "duna_sands", ItemBlockBase::new);
		eve_silt = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":eve_silt").setStepSound(SoundType.SAND).setBlockName("eve_silt").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "eve_silt", ItemBlockBase::new);
		dirt_dead = HbmRegistry.blockWithItem(new BlockFallingMod(Material.GROUND).setBlockTextureName(RefStrings.MODID + ":dirt_dead").setStepSound(SoundType.GROUND).setBlockName("dirt_dead").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "dirt_dead", ItemBlockBase::new);
		dirt_oily = HbmRegistry.blockWithItem(new BlockFallingMod(Material.GROUND).setBlockTextureName(RefStrings.MODID + ":dirt_oily").setStepSound(SoundType.GROUND).setBlockName("dirt_oily").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "dirt_oily", ItemBlockBase::new);
		sand_dirty = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":sand_dirty").setStepSound(SoundType.SAND).setBlockName("sand_dirty").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "sand_dirty", ItemBlockBase::new);
		sand_dirty_red = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":sand_dirty_red").setStepSound(SoundType.SAND).setBlockName("sand_dirty_red").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "sand_dirty_red", ItemBlockBase::new);
		stone_cracked = HbmRegistry.blockWithItem(new BlockFallingMod(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":stone_cracked").setStepSound(SoundType.STONE).setBlockName("stone_cracked").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F), "stone_cracked", ItemBlockBase::new);
		vinyl_sand = HbmRegistry.blockWithItem(new BlockFallingMod(Material.SAND).setBlockTextureName(RefStrings.MODID + ":sand_vinyl").setStepSound(SoundType.SAND).setBlockName("vinyl_sand").setCreativeTab(ModBlocks.blockTab).setHardness(1.0F).setResistance(1.0F), "vinyl_sand", ItemBlockBase::new);

		//seven rotatable pillars (bismuth cluster, joyeuse cluster, insulation, fiberglass, tritium block, concrete pillar, meteorite pillar): axis rides vanilla's LOG_AXIS block state,
		//top and side textures given in models/block/*.json's cube_column, and the blockstates were completed to axis=y/x/z three ways
		//(the generator had flattened them to single-texture cube_all, leaving the top textures unused — fixed in this batch).
		//The old block_schrabidium_cluster also had "item rarity=rare (blue name)", visible only through an ItemBlockBase forwarder;
		block_schrabidium_cluster = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":block_schrabidium_cluster_side").setBlockName("block_schrabidium_cluster").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(60000.0F), "block_schrabidium_cluster", ItemBlockBase::new);
		block_euphemium_cluster = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":block_euphemium_cluster_side").setBlockName("block_euphemium_cluster").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(60000.0F), "block_euphemium_cluster", ItemBlockBase::new);
		block_insulator = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.CLOTH).setBlockTextureName(RefStrings.MODID + ":block_insulator_side").setStepSound(SoundType.CLOTH).setBlockName("block_insulator").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_insulator", ItemBlockBase::new);
		block_fiberglass = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.CLOTH).setBlockTextureName(RefStrings.MODID + ":block_fiberglass_side").setStepSound(SoundType.CLOTH).setBlockName("block_fiberglass").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(15.0F), "block_fiberglass", ItemBlockBase::new);
		block_tritium = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.GLASS).setBlockTextureName(RefStrings.MODID + ":block_tritium_side").setStepSound(SoundType.GLASS).setBlockName("block_tritium").setCreativeTab(ModBlocks.blockTab).setHardness(3.0F).setResistance(2.0F), "block_tritium", ItemBlockBase::new);
		concrete_pillar = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":concrete_pillar_side").setBlockName("concrete_pillar").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(180.0F), "concrete_pillar", ItemBlockBase::new);
		//meteor_pillar's side texture in the old version was just "meteor_pillar" (no _side suffix); only the top carries _top
		meteor_pillar = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_pillar").setBlockName("meteor_pillar").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F), "meteor_pillar", ItemBlockBase::new);

		//seven logs (pink log, hardened vinyl log, PVC log, lattice log, BF log, detonating log, EU log):
		//the old BlockLogNT just swapped two textures onto vanilla's BlockLog; here textures belong to models/block and axis/shape live in the previous batch's
		//BlockRotatablePillar, so this batch adds zero classes, only registration and asset fixes (six families' blockstates previously had only a "normal" variant,
		//and lattice_log was missing all three assets). pink_log was excluded from the creative tab in the old version (setCreativeTab(null)),
		//its side borrows vanilla's oak log (minecraft:blocks/log_oak) with only the top being hbm:pink_log.
		//Also added to BlockRotatablePillar are two behaviors the old version had through different hooks: the default state pins back to vertical (setblock used to come out sideways),
		//and rotating by clicked face; the previous batch's seven pillars share this base and benefit too — details in that class's comments.
		pink_log = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.WOOD).setStepSound(SoundType.WOOD).setBlockName("pink_log").setCreativeTab(null).setHardness(0.5F), "pink_log", ItemBlockBase::new);
		vinyl_log = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.WOOD).setStepSound(SoundType.WOOD).setBlockName("vinyl_log").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "vinyl_log", ItemBlockBase::new);
		pvc_log = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.WOOD).setStepSound(SoundType.WOOD).setBlockName("pvc_log").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "pvc_log", ItemBlockBase::new);
		lattice_log = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.WOOD).setStepSound(SoundType.WOOD).setBlockName("lattice_log").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "lattice_log", ItemBlockBase::new);
		bf_log = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.WOOD).setStepSound(SoundType.WOOD).setBlockName("bf_log").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "bf_log", ItemBlockBase::new);
		primed_log = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.WOOD).setStepSound(SoundType.WOOD).setBlockName("primed_log").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "primed_log", ItemBlockBase::new);
		eu_log = HbmRegistry.blockWithItem(new BlockRotatablePillar(Material.WOOD).setStepSound(SoundType.WOOD).setBlockName("eu_log").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "eu_log", ItemBlockBase::new);
		//twenty-seven hazardous material blocks (uranium/thorium/plutonium/neptunium series plus bismuth/joyeuse/plutonic acid, plus yellowcake/red phosphorus/fallout/white phosphorus/actinium/tritium sulfide):
		//BlockHazard and BlockHazardFalling's behavior ported wholesale — on world entry it reads this block item's radiation from the hazard table (a tenth of it),
		//then adds to its chunk every 20 ticks and reschedules; beacon base as before. Three spots rewritten for 1.12.2 on landing:
		//coordinates and scheduled updates moved to BlockPos/IBlockState; client particles townaura / flame+smoke / lava map to the same-named EnumParticleTypes tiers,
		//but the old SCHRAB tier sent a custom schrabfog packet and this version's effectNT dispatch table has no such type ⇒ registration skipped (radiation itself unaffected).
		//The old getRarity (blue names on the bismuth/joyeuse/plutonic acid items) didn't come over: this version needs an ItemBlock forwarder to show it, and the door isn't on this block's face — reported to the item line.
		block_uranium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_uranium").setBlockName("block_uranium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_uranium", ItemBlockBase::new);
		block_u233 = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_u233").setBlockName("block_u233").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_u233", ItemBlockBase::new);
		block_u235 = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_u235").setBlockName("block_u235").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_u235", ItemBlockBase::new);
		block_u238 = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_u238").setBlockName("block_u238").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_u238", ItemBlockBase::new);
		block_uranium_fuel = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_uranium_fuel").setBlockName("block_uranium_fuel").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_uranium_fuel", ItemBlockBase::new);
		block_thorium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_thorium").setBlockName("block_thorium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_thorium", ItemBlockBase::new);
		block_thorium_fuel = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_thorium_fuel").setBlockName("block_thorium_fuel").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_thorium_fuel", ItemBlockBase::new);
		block_neptunium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_neptunium").setBlockName("block_neptunium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(60.0F), "block_neptunium", ItemBlockBase::new);
		block_mox_fuel = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_mox_fuel").setBlockName("block_mox_fuel").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_mox_fuel", ItemBlockBase::new);
		block_plutonium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_plutonium").setBlockName("block_plutonium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_plutonium", ItemBlockBase::new);
		block_pu239 = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_pu239").setBlockName("block_pu239").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_pu239", ItemBlockBase::new);
		block_pu240 = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_pu240").setBlockName("block_pu240").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_pu240", ItemBlockBase::new);
		block_pu_mix = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_pu_mix").setBlockName("block_pu_mix").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_pu_mix", ItemBlockBase::new);
		block_plutonium_fuel = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_plutonium_fuel").setBlockName("block_plutonium_fuel").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_plutonium_fuel", ItemBlockBase::new);
		block_trinitite = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setBlockTextureName(RefStrings.MODID + ":block_trinitite").setBlockName("block_trinitite").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_trinitite", ItemBlockBase::new);
		block_corium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBlockTextureName(RefStrings.MODID + ":block_corium").setBlockName("block_corium").setCreativeTab(ModBlocks.blockTab).setHardness(100.0F).setResistance(6000.0F), "block_corium", ItemBlockBase::new);
		block_schraranium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.SCHRAB).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_schraranium").setBlockName("block_schraranium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(250.0F), "block_schraranium", ItemBlockBase::new);
		block_schrabidium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.SCHRAB).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_schrabidium").setBlockName("block_schrabidium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(600.0F), "block_schrabidium", ItemBlockBase::new);
		block_schrabidate = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.SCHRAB).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_schrabidate").setBlockName("block_schrabidate").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(600.0F), "block_schrabidate", ItemBlockBase::new);
		block_solinium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.SCHRAB).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_solinium").setBlockName("block_solinium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(600.0F), "block_solinium", ItemBlockBase::new);
		block_schrabidium_fuel = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setDisplayEffect(BlockHazard.ExtDisplayEffect.SCHRAB).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_schrabidium_fuel").setBlockName("block_schrabidium_fuel").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(600.0F), "block_schrabidium_fuel", ItemBlockBase::new);
		block_yellowcake = HbmRegistry.blockWithItem(new BlockHazardFalling(Material.SAND).setBeaconable().setStepSound(SoundType.SAND).setBlockTextureName(RefStrings.MODID + ":block_yellowcake").setBlockName("block_yellowcake").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_yellowcake", ItemBlockBase::new);
		block_white_phosphorus = HbmRegistry.blockWithItem(new BlockHazard(Material.ROCK).setBeaconable().setBlockTextureName(RefStrings.MODID + ":block_white_phosphorus").setBlockName("block_white_phosphorus").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_white_phosphorus", ItemBlockBase::new);
		block_red_phosphorus = HbmRegistry.blockWithItem(new BlockHazardFalling(Material.SAND).setBeaconable().setStepSound(SoundType.SAND).setBlockTextureName(RefStrings.MODID + ":block_red_phosphorus").setBlockName("block_red_phosphorus").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_red_phosphorus", ItemBlockBase::new);
		block_fallout = HbmRegistry.blockWithItem(new BlockHazardFalling(Material.SAND).setStepSound(SoundType.GROUND).setBlockTextureName(RefStrings.MODID + ":ash").setBlockName("block_fallout").setCreativeTab(ModBlocks.blockTab).setHardness(0.2F), "block_fallout", ItemBlockBase::new);
		block_ra226 = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_ra226").setBlockName("block_ra226").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_ra226", ItemBlockBase::new);
		block_actinium = HbmRegistry.blockWithItem(new BlockHazard(Material.IRON).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_actinium").setBlockName("block_actinium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_actinium", ItemBlockBase::new);
		//the three nuclear waste blocks: same base as above, except each tick has a half chance to place dense radon in a neighbor (the only behavioral branch among the old four fields)
		block_waste = HbmRegistry.blockWithItem(new BlockNuclearWaste().setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setBlockTextureName(RefStrings.MODID + ":block_waste").setBlockName("block_waste").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_waste", ItemBlockBase::new);
		block_waste_painted = HbmRegistry.blockWithItem(new BlockNuclearWaste().setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setBlockTextureName(RefStrings.MODID + ":block_waste_painted").setBlockName("block_waste_painted").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_waste_painted", ItemBlockBase::new);
		block_waste_vitrified = HbmRegistry.blockWithItem(new BlockNuclearWaste().setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setBlockTextureName(RefStrings.MODID + ":block_waste_vitrified").setBlockName("block_waste_vitrified").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "block_waste_vitrified", ItemBlockBase::new);
		//the four coordinate-tinted pieces: the old version hashed x/y/z in the block class into an index (one getIcon each on BlockTint / BlockTrinitite);
		//this version needs no code — the blockstate lists several weighted models for the same tier, and rendering seeds by MathHelper.getPositionRandom
		//to pick one, landing visually identical to the old version. The variant lists match the old texture order one for one, with their tables shipped in the same batch.
		tumor = HbmRegistry.blockWithItem(new BlockBase(Material.CLAY).setStepSound(SoundType.SNOW).setBlockTextureName(RefStrings.MODID + ":tumor_base").setBlockName("tumor").setCreativeTab(ModBlocks.blockTab).setHardness(1.0F), "tumor", ItemBlockBase::new);
		ike_regolith = HbmRegistry.blockWithItem(new BlockBase(Material.ROCK).setStepSound(SoundType.STONE).setBlockTextureName(RefStrings.MODID + ":ike_regolith").setBlockName("ike_regolith").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ike_regolith", ItemBlockBase::new);
		waste_trinitite = HbmRegistry.blockWithItem(new BlockStepEffect(Material.SAND).setStepSound(SoundType.SAND).setBlockTextureName(RefStrings.MODID + ":waste_trinitite").setBlockName("waste_trinitite").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "waste_trinitite", ItemBlockBase::new);
		waste_trinitite_red = HbmRegistry.blockWithItem(new BlockStepEffect(Material.SAND).setStepSound(SoundType.SAND).setBlockTextureName(RefStrings.MODID + ":waste_trinitite_red").setBlockName("waste_trinitite_red").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "waste_trinitite_red", ItemBlockBase::new);
		//the two hot ones: same hazard base as above, plus the "vent steam clouds" half; the old two differed only in pu238 carrying one glow tier (5/15)
		block_polonium = HbmRegistry.blockWithItem(new BlockHotHazard().setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_polonium").setBlockName("block_polonium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F), "block_polonium", ItemBlockBase::new);
		block_pu238 = HbmRegistry.blockWithItem(new BlockHotHazard().setDisplayEffect(BlockHazard.ExtDisplayEffect.RADFOG).setBeaconable().setStepSound(SoundType.METAL).setBlockTextureName(RefStrings.MODID + ":block_pu238").setBlockName("block_pu238").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(50.0F).setLightLevel(5F / 15F), "block_pu238", ItemBlockBase::new);
		//the steel trapdoor: stats follow the old line (hardness 3 / blast 8 / metal steps), behavior in BlockNTMTrapdoor — right-click toggles too, and an open door works as a ladder
		trapdoor_steel = HbmRegistry.blockWithItem(new BlockNTMTrapdoor(Material.IRON).setBlockTextureName(RefStrings.MODID + ":trapdoor_steel").setStepSound(SoundType.METAL).setBlockName("trapdoor_steel").setCreativeTab(ModBlocks.blockTab).setHardness(3.0F).setResistance(8.0F), "trapdoor_steel", ItemBlockBase::new);
		//two pink wood slabs: neither enters the creative tab (old setCreativeTab(null)); the full-block version has no item form and drops the two single slabs
		pink_slab = HbmRegistry.blockWithItem(new BlockPinkSlab(false, null, Material.WOOD).setBlockTextureName(RefStrings.MODID + ":pink_planks").setStepSound(SoundType.WOOD).setBlockName("pink_slab").setCreativeTab(null), "pink_slab", ItemBlockBase::new);
		pink_double_slab = HbmRegistry.block(new BlockPinkSlab(true, pink_slab, Material.WOOD).setBlockTextureName(RefStrings.MODID + ":pink_planks").setStepSound(SoundType.WOOD).setBlockName("pink_double_slab").setCreativeTab(null), "pink_double_slab");
		//two reinforced glass panes: connection, four-way state and collision all ride vanilla's pane set; this mod differs only in "drops itself when broken" and the hardness/blast pair;
		//the old render pass count is carried here by the JSON model's alpha, reasoning in BlockNTMGlassPane's class comment.
		reinforced_glass_pane = HbmRegistry.blockWithItem(new BlockNTMGlassPane(Material.ROCK, false).setBlockTextureName(RefStrings.MODID + ":reinforced_glass_pane").setBlockName("reinforced_glass_pane").setCreativeTab(ModBlocks.blockTab).setHardness(2.0F).setResistance(25.0F), "reinforced_glass_pane", ItemBlockBase::new);
		reinforced_laminate_pane = HbmRegistry.blockWithItem(new BlockNTMGlassPane(Material.ROCK, true).setBlockTextureName(RefStrings.MODID + ":reinforced_laminate_pane").setBlockName("reinforced_laminate_pane").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(300.0F), "reinforced_laminate_pane", ItemBlockBase::new);
		//ten connected glass pieces: the old int render pass count became a genuinely effective BlockRenderLayer (0→CUTOUT, 1→TRANSLUCENT),
		//with the reasoning and the two places this version has no face for written in BlockNTMGlassCT's class comment. The looks switch on via the ten blockstates'
		//model=hbm:ct/blocks/<base name>. Of these ten only reinforced_laminate and glass_quartz drop themselves.
		reinforced_glass = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.ROCK, false, BlockRenderLayer.CUTOUT).setBlockTextureName(RefStrings.MODID + ":reinforced_glass").setBlockName("reinforced_glass").setCreativeTab(ModBlocks.blockTab).setLightOpacity(0).setHardness(2.0F).setResistance(25.0F), "reinforced_glass", ItemBlockBase::new);
		reinforced_laminate = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.ROCK, true, BlockRenderLayer.TRANSLUCENT).setBlockTextureName(RefStrings.MODID + ":reinforced_laminate").setBlockName("reinforced_laminate").setCreativeTab(ModBlocks.blockTab).setLightOpacity(0).setHardness(15.0F).setResistance(300.0F), "reinforced_laminate", ItemBlockBase::new);
		glass_boron = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.GLASS, false, BlockRenderLayer.CUTOUT).setBlockTextureName(RefStrings.MODID + ":glass_boron").setStepSound(SoundType.GLASS).setBlockName("glass_boron").setCreativeTab(ModBlocks.machineTab).setHardness(0.3F), "glass_boron", ItemBlockBase::new);
		glass_lead = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.GLASS, false, BlockRenderLayer.CUTOUT).setBlockTextureName(RefStrings.MODID + ":glass_lead").setStepSound(SoundType.GLASS).setBlockName("glass_lead").setCreativeTab(ModBlocks.machineTab).setHardness(0.3F), "glass_lead", ItemBlockBase::new);
		glass_uranium = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.GLASS, false, BlockRenderLayer.TRANSLUCENT).setBlockTextureName(RefStrings.MODID + ":glass_uranium").setStepSound(SoundType.GLASS).setBlockName("glass_uranium").setCreativeTab(ModBlocks.machineTab).setLightLevel(5F / 15F).setHardness(0.3F), "glass_uranium", ItemBlockBase::new);
		glass_trinitite = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.GLASS, false, BlockRenderLayer.TRANSLUCENT).setBlockTextureName(RefStrings.MODID + ":glass_trinitite").setStepSound(SoundType.GLASS).setBlockName("glass_trinitite").setCreativeTab(ModBlocks.machineTab).setLightLevel(5F / 15F).setHardness(0.3F), "glass_trinitite", ItemBlockBase::new);
		glass_polonium = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.GLASS, false, BlockRenderLayer.TRANSLUCENT).setBlockTextureName(RefStrings.MODID + ":glass_polonium").setStepSound(SoundType.GLASS).setBlockName("glass_polonium").setCreativeTab(ModBlocks.machineTab).setLightLevel(5F / 15F).setHardness(0.3F), "glass_polonium", ItemBlockBase::new);
		glass_ash = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.GLASS, false, BlockRenderLayer.TRANSLUCENT).setBlockTextureName(RefStrings.MODID + ":glass_ash").setStepSound(SoundType.GLASS).setBlockName("glass_ash").setCreativeTab(ModBlocks.machineTab).setHardness(3.0F), "glass_ash", ItemBlockBase::new);
		//⚠ the texture string below is kept verbatim from the old version (missing its colon); the old version really did let it overwrite the constructor's correct name. Textures belong to JSON here, so the string does nothing,
		//kept to avoid re-editing old history; reasoning in BlockNTMGlassCT's class comment.
		glass_quartz = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.PACKED_ICE, true, BlockRenderLayer.CUTOUT).setBlockTextureName(RefStrings.MODID + "glass_quartz").setStepSound(SoundType.GLASS).setBlockName("glass_quartz").setCreativeTab(ModBlocks.blockTab).setHardness(1.0F).setResistance(40.0F), "glass_quartz", ItemBlockBase::new);
		glass_polarized = HbmRegistry.blockWithItem(new BlockNTMGlassCT(Material.GLASS, false, BlockRenderLayer.CUTOUT).setBlockTextureName(RefStrings.MODID + ":glass_polarized").setStepSound(SoundType.GLASS).setBlockName("glass_polarized").setCreativeTab(ModBlocks.machineTab).setHardness(0.3F), "glass_polarized", ItemBlockBase::new);
		//two reinforced lamps (off state in the block creative tab; the lit one has no item page — the old version never set one): this version's vanilla redstone lamp is exactly this shape,
		//but vanilla's "paired lamp" is hard-wired onto the two Blocks pieces with no override ⇒ this little piece is self-built; toggle and drop reasoning all in BlockNTMLamp.
		//The two point at each other by calling pair once after construction, no static fields (a registration order change would make statics read null).
		BlockNTMLamp lampOff = new BlockNTMLamp(Material.ROCK, false);
		BlockNTMLamp lampOn = new BlockNTMLamp(Material.ROCK, true);
		lampOff.pair(lampOn);
		lampOn.pair(lampOff);
		reinforced_lamp_off = HbmRegistry.blockWithItem(lampOff.setBlockTextureName(RefStrings.MODID + ":reinforced_lamp_off").setBlockName("reinforced_lamp_off").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(80.0F), "reinforced_lamp_off", ItemBlockBase::new);
		reinforced_lamp_on = HbmRegistry.blockWithItem(lampOn.setBlockTextureName(RefStrings.MODID + ":reinforced_lamp_on").setBlockName("reinforced_lamp_on").setCreativeTab(null).setHardness(15.0F).setResistance(80.0F), "reinforced_lamp_on", ItemBlockBase::new);
		//four tritium lamps (green / blue, one off and one lit each — four lamps in the old version too): toggle and drops follow the reinforced lamp's rules, sharing the BlockNTMLamp base.
		//The old TritiumLamp also had a "notify nearby spotlights to update their beams" bit; this version's port is blocks/ISpotlight (in tree, zero implementations, zero callers),
		//and the spotlight machine piece hasn't landed ⇒ writing it now gives it no call surface; wire it together with this base when it lands.
		BlockNTMLamp greenOff = new BlockNTMLamp(Material.REDSTONE_LIGHT, false);
		BlockNTMLamp greenOn = new BlockNTMLamp(Material.REDSTONE_LIGHT, true);
		greenOff.pair(greenOn);
		greenOn.pair(greenOff);
		lamp_tritium_green_off = HbmRegistry.blockWithItem(greenOff.setBlockTextureName(RefStrings.MODID + ":lamp_tritium_green_off").setStepSound(SoundType.GLASS).setBlockName("lamp_tritium_green_off").setCreativeTab(ModBlocks.blockTab).setHardness(3.0F), "lamp_tritium_green_off", ItemBlockBase::new);
		lamp_tritium_green_on = HbmRegistry.blockWithItem(greenOn.setBlockTextureName(RefStrings.MODID + ":lamp_tritium_green_on").setStepSound(SoundType.GLASS).setBlockName("lamp_tritium_green_on").setCreativeTab(null).setHardness(3.0F), "lamp_tritium_green_on", ItemBlockBase::new);
		BlockNTMLamp blueOff = new BlockNTMLamp(Material.REDSTONE_LIGHT, false);
		BlockNTMLamp blueOn = new BlockNTMLamp(Material.REDSTONE_LIGHT, true);
		blueOff.pair(blueOn);
		blueOn.pair(blueOff);
		lamp_tritium_blue_off = HbmRegistry.blockWithItem(blueOff.setBlockTextureName(RefStrings.MODID + ":lamp_tritium_blue_off").setStepSound(SoundType.GLASS).setBlockName("lamp_tritium_blue_off").setCreativeTab(ModBlocks.blockTab).setHardness(3.0F), "lamp_tritium_blue_off", ItemBlockBase::new);
		lamp_tritium_blue_on = HbmRegistry.blockWithItem(blueOn.setBlockTextureName(RefStrings.MODID + ":lamp_tritium_blue_on").setStepSound(SoundType.GLASS).setBlockName("lamp_tritium_blue_on").setCreativeTab(null).setHardness(3.0F), "lamp_tritium_blue_on", ItemBlockBase::new);
		//nineteen ores and decos (nether sulfur/phosphorus/cobalt ores, ten gneiss pieces, four meteorite pieces, two jungle bricks, waste planks, permafrost and frozen planks):
		//BlockStepEffect's two behaviors ported wholesale — trample applies effects (slow on permafrost, radiation/pollution on jungle bricks, ignition on molten meteorite),
		//and molten meteorite cools into meteorite cobble on random tick (with a quench hiss) and leaves lava in place when mined.
		//⚠ drops moved to tables in this version: the old version hard-coded drops in getItemDropped/quantityDroppedWithBonus, and 1.12.2's block side has no table mechanism there
		//(Block has no getLootTable, and the context carries neither coordinates nor tool), so all nineteen override harvestBlock through BlockLoot,
		//each with an hbm:blocks/<registry name> table (shipped in this batch): sulfur 2~4, phosphorus/gunpowder 1:9, cobalt fragments 5~12, rare earth to chunk_ore,
		//meteorite cobble and fragments to meteorite shards, waste planks to charcoal (coal data 1), molten meteorite an empty table (drops nothing).
		//The cost: fortune can't reach the table (no tool in context), so the old multiplier is lost — a registered §15 mechanism exception; silk touch still handled by BlockLoot as vanilla does.
		ore_nether_sulfur = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_nether_sulfur").setBlockName("ore_nether_sulfur").setCreativeTab(ModBlocks.blockTab).setHardness(0.4F).setResistance(10.0F), "ore_nether_sulfur", ItemBlockBase::new);
		ore_nether_fire = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_nether_fire").setBlockName("ore_nether_fire").setCreativeTab(ModBlocks.blockTab).setHardness(0.4F).setResistance(10.0F), "ore_nether_fire", ItemBlockBase::new);
		ore_nether_cobalt = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_nether_cobalt").setBlockName("ore_nether_cobalt").setCreativeTab(ModBlocks.blockTab).setHardness(0.4F).setResistance(10.0F), "ore_nether_cobalt", ItemBlockBase::new);
		ore_gneiss_iron = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_iron").setBlockName("ore_gneiss_iron").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_iron", ItemBlockBase::new);
		ore_gneiss_gold = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_gold").setBlockName("ore_gneiss_gold").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_gold", ItemBlockBase::new);
		ore_gneiss_copper = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_copper").setBlockName("ore_gneiss_copper").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_copper", ItemBlockBase::new);
		ore_gneiss_lithium = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_lithium").setBlockName("ore_gneiss_lithium").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_lithium", ItemBlockBase::new);
		ore_gneiss_schrabidium = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_schrabidium").setBlockName("ore_gneiss_schrabidium").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_schrabidium", ItemBlockBase::new);
		ore_gneiss_rare = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_rare").setBlockName("ore_gneiss_rare").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_rare", ItemBlockBase::new);
		ore_gneiss_gas = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_gas").setBlockName("ore_gneiss_gas").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_gas", ItemBlockBase::new);
		block_meteor = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor").setBlockName("block_meteor").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F), "block_meteor", ItemBlockBase::new);
		block_meteor_cobble = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_cobble").setBlockName("block_meteor_cobble").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F), "block_meteor_cobble", ItemBlockBase::new);
		block_meteor_broken = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":meteor_crushed").setBlockName("block_meteor_broken").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F), "block_meteor_broken", ItemBlockBase::new);
		block_meteor_molten = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK, true).setBlockTextureName(RefStrings.MODID + ":meteor_cobble_molten").setBlockName("block_meteor_molten").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F).setLightLevel(0.75F), "block_meteor_molten", ItemBlockBase::new);
		brick_jungle_ooze = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_jungle_ooze").setBlockName("brick_jungle_ooze").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F), "brick_jungle_ooze", ItemBlockBase::new);
		brick_jungle_mystic = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":brick_jungle_mystic").setBlockName("brick_jungle_mystic").setCreativeTab(ModBlocks.blockTab).setHardness(15.0F).setResistance(360.0F), "brick_jungle_mystic", ItemBlockBase::new);
		waste_planks = HbmRegistry.blockWithItem(new BlockStepEffect(Material.WOOD).setStepSound(SoundType.WOOD).setBlockTextureName(RefStrings.MODID + ":waste_planks").setBlockName("waste_planks").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "waste_planks", ItemBlockBase::new);
		frozen_dirt = HbmRegistry.blockWithItem(new BlockStepEffect(Material.GROUND).setStepSound(SoundType.GLASS).setBlockTextureName(RefStrings.MODID + ":frozen_dirt").setBlockName("frozen_dirt").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "frozen_dirt", ItemBlockBase::new);
		frozen_planks = HbmRegistry.blockWithItem(new BlockStepEffect(Material.WOOD).setStepSound(SoundType.GLASS).setBlockTextureName(RefStrings.MODID + ":frozen_planks").setBlockName("frozen_planks").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "frozen_planks", ItemBlockBase::new);
		//seven depth-rock ores (gneiss-cluster iron/titanium/tungsten, deep cinnabar/zircon/borax/nether neodymium): the minability half inherits the previous batch's BlockDepth
		//(only tools honoring IDepthRockTool can mine it), and the drops half moves to tables per §15 — all seven override harvestBlock through BlockLoot,
		//each with an hbm:blocks/<registry name> table (cinnabar 2~4, zircon grains 2~3, neodymium fragments 2~3, clusters to their crystal, borax to powder).
		//⚠ one skipped and logged: ore_alexandrite's drop gem_alexandrite isn't in the item registry yet (the old ItemAlexandrite class,
		//whose stats belong to the item line), so its table waits — "block + table" lands together once that item registers.
		ore_depth_cinnebar = HbmRegistry.blockWithItem(new BlockDepthOre().setBlockTextureName(RefStrings.MODID + ":ore_depth_cinnebar").setBlockName("ore_depth_cinnebar").setCreativeTab(ModBlocks.blockTab), "ore_depth_cinnebar", ItemBlockBase::new);
		ore_depth_zirconium = HbmRegistry.blockWithItem(new BlockDepthOre().setBlockTextureName(RefStrings.MODID + ":ore_depth_zirconium").setBlockName("ore_depth_zirconium").setCreativeTab(ModBlocks.blockTab), "ore_depth_zirconium", ItemBlockBase::new);
		ore_depth_borax = HbmRegistry.blockWithItem(new BlockDepthOre().setBlockTextureName(RefStrings.MODID + ":ore_depth_borax").setBlockName("ore_depth_borax").setCreativeTab(ModBlocks.blockTab), "ore_depth_borax", ItemBlockBase::new);
		cluster_depth_iron = HbmRegistry.blockWithItem(new BlockDepthOre().setBlockTextureName(RefStrings.MODID + ":cluster_depth_iron").setBlockName("cluster_depth_iron").setCreativeTab(ModBlocks.blockTab), "cluster_depth_iron", ItemBlockBase::new);
		cluster_depth_titanium = HbmRegistry.blockWithItem(new BlockDepthOre().setBlockTextureName(RefStrings.MODID + ":cluster_depth_titanium").setBlockName("cluster_depth_titanium").setCreativeTab(ModBlocks.blockTab), "cluster_depth_titanium", ItemBlockBase::new);
		cluster_depth_tungsten = HbmRegistry.blockWithItem(new BlockDepthOre().setBlockTextureName(RefStrings.MODID + ":cluster_depth_tungsten").setBlockName("cluster_depth_tungsten").setCreativeTab(ModBlocks.blockTab), "cluster_depth_tungsten", ItemBlockBase::new);
		//four surface crystal clusters (same shape as the three depth-rock clusters above): drops follow the old "drop the matching crystal", implemented here with tables per §15
		cluster_iron = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":cluster_iron").setBlockName("cluster_iron").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(15.0F), "cluster_iron", ItemBlockBase::new);
		cluster_titanium = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":cluster_titanium").setBlockName("cluster_titanium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(15.0F), "cluster_titanium", ItemBlockBase::new);
		cluster_aluminium = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":cluster_aluminium").setBlockName("cluster_aluminium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(15.0F), "cluster_aluminium", ItemBlockBase::new);
		cluster_copper = HbmRegistry.blockWithItem(new BlockStepEffect(Material.ROCK).setBlockTextureName(RefStrings.MODID + ":cluster_copper").setBlockName("cluster_copper").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(15.0F), "cluster_copper", ItemBlockBase::new);
		ore_depth_nether_neodymium = HbmRegistry.blockWithItem(new BlockDepthOre().setBlockTextureName(RefStrings.MODID + ":ore_depth_nether_neodymium").setBlockName("ore_depth_nether_neodymium").setCreativeTab(ModBlocks.blockTab), "ore_depth_nether_neodymium", ItemBlockBase::new);
		//fourteen gas-leaking family pieces (charred uranium ore ×5, asbestos and lab bricks ×6, ancient waste, core fragment): the four switches ported as-is
		//(randomTick/rate/onBreak/onNeighbour); what gas leaks is decided by block identity, the four gases living in the previous batch's ModBlocksGas.
		//The "leave a puff of gas when mined" half lands in breakBlock (vanilla calls it after the block is already air — timing matches the old dropBlockAsItemWithChance);
		//the "trampling leaks asbestos" half splits into server-side gas release + client-side particles. Drops are the block itself (vanilla fallback) ⇒ no §15 table needed for this family.
		//⚠ the old getGas() also had an ore_nether_coal → gas_monoxide branch; that block isn't in the block registry yet — add it when it lands.
		ore_uranium_scorched = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setBlockTextureName(RefStrings.MODID + ":ore_uranium_scorched").setBlockName("ore_uranium_scorched").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "ore_uranium_scorched", ItemBlockBase::new);
		ore_nether_uranium = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setBlockTextureName(RefStrings.MODID + ":ore_nether_uranium").setBlockName("ore_nether_uranium").setCreativeTab(ModBlocks.blockTab).setHardness(0.4F).setResistance(10.0F), "ore_nether_uranium", ItemBlockBase::new);
		ore_nether_uranium_scorched = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setBlockTextureName(RefStrings.MODID + ":ore_nether_uranium_scorched").setBlockName("ore_nether_uranium_scorched").setCreativeTab(ModBlocks.blockTab).setHardness(0.4F).setResistance(10.0F), "ore_nether_uranium_scorched", ItemBlockBase::new);
		ore_gneiss_uranium = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_uranium").setBlockName("ore_gneiss_uranium").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_uranium", ItemBlockBase::new);
		ore_gneiss_uranium_scorched = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_uranium_scorched").setBlockName("ore_gneiss_uranium_scorched").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_uranium_scorched", ItemBlockBase::new);
		ore_gneiss_asbestos = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setBlockTextureName(RefStrings.MODID + ":ore_gneiss_asbestos").setBlockName("ore_gneiss_asbestos").setCreativeTab(ModBlocks.blockTab).setHardness(1.5F).setResistance(10.0F), "ore_gneiss_asbestos", ItemBlockBase::new);
		ancient_scrap = HbmRegistry.blockWithItem(new BlockOutgas(Material.IRON, true, 1, true, true).setBlockTextureName(RefStrings.MODID + ":ancient_scrap").setBlockName("ancient_scrap").setCreativeTab(ModBlocks.blockTab).setHardness(100.0F).setResistance(6000.0F), "ancient_scrap", ItemBlockBase::new);
		block_corium_cobble = HbmRegistry.blockWithItem(new BlockOutgas(Material.IRON, true, 1, true, true).setBlockTextureName(RefStrings.MODID + ":block_corium_cobble").setBlockName("block_corium_cobble").setCreativeTab(ModBlocks.blockTab).setHardness(100.0F).setResistance(6000.0F), "block_corium_cobble", ItemBlockBase::new);
		block_asbestos = HbmRegistry.blockWithItem(new BlockOutgas(Material.CLOTH, true, 5, true).setStepSound(SoundType.CLOTH).setBlockTextureName(RefStrings.MODID + ":block_asbestos").setBlockName("block_asbestos").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(15.0F), "block_asbestos", ItemBlockBase::new);
		deco_asbestos = HbmRegistry.blockWithItem(new BlockOutgas(Material.CLOTH, true, 5, true).setBlockTextureName(RefStrings.MODID + ":deco_asbestos").setBlockName("deco_asbestos").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_asbestos", ItemBlockBase::new);
		//nine connected decos (BlockDecoCT): looks via connected textures, enabled by writing model=hbm:ct/blocks/<base name> in the blockstate,
		//the three block-side steps live in the class; the old noFortune/quantityDroppedWithBonus were always no-ops for this family — see that side's class comment.
		deco_red_copper = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_red_copper").setBlockName("deco_red_copper").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_red_copper", ItemBlockBase::new);
		deco_tungsten = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_tungsten").setBlockName("deco_tungsten").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_tungsten", ItemBlockBase::new);
		deco_aluminium = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_aluminium").setBlockName("deco_aluminium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_aluminium", ItemBlockBase::new);
		deco_steel = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_steel").setBlockName("deco_steel").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_steel", ItemBlockBase::new);
		deco_rusty_steel = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_rusty_steel").setBlockName("deco_rusty_steel").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_rusty_steel", ItemBlockBase::new);
		deco_lead = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_lead").setBlockName("deco_lead").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_lead", ItemBlockBase::new);
		deco_beryllium = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_beryllium").setBlockName("deco_beryllium").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_beryllium", ItemBlockBase::new);
		deco_stainless = HbmRegistry.blockWithItem(new BlockDecoCT(Material.IRON).setBlockTextureName(RefStrings.MODID + ":deco_stainless").setBlockName("deco_stainless").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "deco_stainless", ItemBlockBase::new);
		laythe_root = HbmRegistry.blockWithItem(new BlockDecoCT(Material.WOOD).setBlockTextureName(RefStrings.MODID + ":laythe_root").setBlockName("laythe_root").setCreativeTab(ModBlocks.blockTab).setHardness(2.0F), "laythe_root", ItemBlockBase::new);
		//one platemetal in fifteen tiers (BlockPlatemetalCT): the fourth family on the connected-texture line, subtypes via the variant property,
		//tier table BlockEnums.PlatemetalType; all fifteen blockstate tiers point at hbm:ct/blocks/platemetal.<tier>,
		//with the block-side three steps and the two naming conventions explained in the class comment. The old ModSoundTypes.platemetal doesn't exist here; vanilla METAL substitutes for now (logged in the same comment).
		platemetal = HbmRegistry.blockWithItem(new BlockPlatemetalCT(Material.IRON, true, true).setBlockTextureName(RefStrings.MODID + ":platemetal").setStepSound(SoundType.METAL).setBlockName("platemetal").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "platemetal", ItemBlockMeta::new);
		brick_asbestos = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setBlockTextureName(RefStrings.MODID + ":brick_asbestos").setBlockName("brick_asbestos").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(1000.0F), "brick_asbestos", ItemBlockBase::new);
		tile_lab = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, false, 5, true).setStepSound(SoundType.GLASS).setBlockTextureName(RefStrings.MODID + ":tile_lab").setBlockName("tile_lab").setCreativeTab(ModBlocks.blockTab).setHardness(1.0F).setResistance(20.0F), "tile_lab", ItemBlockBase::new);
		tile_lab_cracked = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, false, 5, true).setStepSound(SoundType.GLASS).setBlockTextureName(RefStrings.MODID + ":tile_lab_cracked").setBlockName("tile_lab_cracked").setCreativeTab(ModBlocks.blockTab).setHardness(1.0F).setResistance(20.0F), "tile_lab_cracked", ItemBlockBase::new);
		tile_lab_broken = HbmRegistry.blockWithItem(new BlockOutgas(Material.ROCK, true, 5, true).setStepSound(SoundType.GLASS).setBlockTextureName(RefStrings.MODID + ":tile_lab_broken").setBlockName("tile_lab_broken").setCreativeTab(ModBlocks.blockTab).setHardness(1.0F).setResistance(20.0F), "tile_lab_broken", ItemBlockBase::new);
		//eighteen generic stairs (the concrete/ducrete/brick families' staircases): shape, facing, half height, inner/outer corner shapes all vanilla BlockStairs,
		//with bottom/top/side textures from models/block/<name>{,_inner,_outer}.json (parents are vanilla block/stairs,
		//inner/outer likewise), and blockstates listing all forty facing×half×shape tiers per the vanilla stairs template (copied straight from the 1.12.2 client jar's
		//oak_stairs.json with model names swapped); the base blocks registered in earlier batches, so only the stairs' shape base gets pointed over.
		//⚠ two skipped and logged: lightstone_tile_stairs / lightstone_bricks_stairs base lightstone isn't in the block registry yet — add when it lands.
		//⚠ the old constructor's recipeGen (the auto stairs-recipe list) didn't come over; the recipe tier isn't this batch's job — don't book them as "recipes ready".
		concrete_smooth_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(concrete_smooth, 0).setBlockTextureName(RefStrings.MODID + ":concrete").setBlockName("concrete_smooth_stairs").setCreativeTab(ModBlocks.blockTab), "concrete_smooth_stairs", ItemBlockBase::new);
		concrete_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(concrete, 0).setBlockTextureName(RefStrings.MODID + ":concrete_tile").setBlockName("concrete_stairs").setCreativeTab(ModBlocks.blockTab), "concrete_stairs", ItemBlockBase::new);
		concrete_asbestos_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(concrete_asbestos, 0).setBlockTextureName(RefStrings.MODID + ":concrete_asbestos").setBlockName("concrete_asbestos_stairs").setCreativeTab(ModBlocks.blockTab), "concrete_asbestos_stairs", ItemBlockBase::new);
		ducrete_smooth_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ducrete_smooth, 0).setBlockTextureName(RefStrings.MODID + ":ducrete").setBlockName("ducrete_smooth_stairs").setCreativeTab(ModBlocks.blockTab), "ducrete_smooth_stairs", ItemBlockBase::new);
		ducrete_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ducrete, 0).setBlockTextureName(RefStrings.MODID + ":ducrete_tile").setBlockName("ducrete_stairs").setCreativeTab(ModBlocks.blockTab), "ducrete_stairs", ItemBlockBase::new);
		brick_concrete_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(brick_concrete, 0).setBlockTextureName(RefStrings.MODID + ":brick_concrete").setBlockName("brick_concrete_stairs").setCreativeTab(ModBlocks.blockTab), "brick_concrete_stairs", ItemBlockBase::new);
		brick_concrete_mossy_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.brick_concrete_mossy, 0).setBlockTextureName(RefStrings.MODID + ":brick_concrete_mossy").setBlockName("brick_concrete_mossy_stairs").setCreativeTab(ModBlocks.blockTab), "brick_concrete_mossy_stairs", ItemBlockBase::new);
		brick_concrete_cracked_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.brick_concrete_cracked, 0).setBlockTextureName(RefStrings.MODID + ":brick_concrete_cracked").setBlockName("brick_concrete_cracked_stairs").setCreativeTab(ModBlocks.blockTab), "brick_concrete_cracked_stairs", ItemBlockBase::new);
		brick_concrete_broken_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.brick_concrete_broken, 0).setBlockTextureName(RefStrings.MODID + ":brick_concrete_broken").setBlockName("brick_concrete_broken_stairs").setCreativeTab(ModBlocks.blockTab), "brick_concrete_broken_stairs", ItemBlockBase::new);
		brick_ducrete_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(brick_ducrete, 0).setBlockTextureName(RefStrings.MODID + ":brick_ducrete").setBlockName("brick_ducrete_stairs").setCreativeTab(ModBlocks.blockTab), "brick_ducrete_stairs", ItemBlockBase::new);
		reinforced_stone_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.reinforced_stone, 0).setBlockTextureName(RefStrings.MODID + ":reinforced_stone").setBlockName("reinforced_stone_stairs").setCreativeTab(ModBlocks.blockTab), "reinforced_stone_stairs", ItemBlockBase::new);
		reinforced_brick_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.reinforced_brick, 0).setBlockTextureName(RefStrings.MODID + ":reinforced_brick").setBlockName("reinforced_brick_stairs").setCreativeTab(ModBlocks.blockTab), "reinforced_brick_stairs", ItemBlockBase::new);
		brick_obsidian_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.brick_obsidian, 0).setBlockTextureName(RefStrings.MODID + ":brick_obsidian").setBlockName("brick_obsidian_stairs").setCreativeTab(ModBlocks.blockTab), "brick_obsidian_stairs", ItemBlockBase::new);
		brick_light_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.brick_light, 0).setBlockTextureName(RefStrings.MODID + ":brick_light").setBlockName("brick_light_stairs").setCreativeTab(ModBlocks.blockTab), "brick_light_stairs", ItemBlockBase::new);
		brick_compound_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.brick_compound, 0).setBlockTextureName(RefStrings.MODID + ":brick_compound").setBlockName("brick_compound_stairs").setCreativeTab(ModBlocks.blockTab), "brick_compound_stairs", ItemBlockBase::new);
		brick_asbestos_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(brick_asbestos, 0).setBlockTextureName(RefStrings.MODID + ":brick_asbestos").setBlockName("brick_asbestos_stairs").setCreativeTab(ModBlocks.blockTab), "brick_asbestos_stairs", ItemBlockBase::new);
		brick_fire_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.brick_fire, 0).setBlockTextureName(RefStrings.MODID + ":brick_fire").setBlockName("brick_fire_stairs").setCreativeTab(ModBlocks.blockTab), "brick_fire_stairs", ItemBlockBase::new);
		pink_stairs = HbmRegistry.blockWithItem(new BlockGenericStairs(ModBlocks.pink_planks, 0).setBlockTextureName(RefStrings.MODID + ":pink_planks").setStepSound(SoundType.WOOD).setBlockName("pink_stairs").setCreativeTab(ModBlocks.blockTab), "pink_stairs", ItemBlockBase::new);
		//six multi-material slabs (six concrete slab materials, five concrete tile, seven brick family, each plus a double-slab):
		//subtypes use a proper variant property (values are the base block names; the old tile.<slab>.<base>.name lang entries were written long ago),
		//top/bottom halves and shape via vanilla BlockSlab; textures in models/block/<slab>_<base>.json (parent vanilla block/half_slab),
		//double slabs reuse the base block's solid model, with blockstates listing half×variant 2N tiers for single and double both (the constructor trap in BlockMultiSlab's comment).
		//⚠ double slabs get no item form (the old one was an empty shell anyway); drops forward to the single slab via getItemDropped with vanilla's count (double = 2).
		//⚠ item icons for subtypes need the item side to provide an IMultiModelItem forwarding the block's variant names to hang at all;
		concrete_slab = HbmRegistry.blockWithItem(new BlockSlabConcrete(false, null, concrete_smooth, concrete, concrete_asbestos, ducrete_smooth, ducrete, ModBlocks.asphalt), "concrete_slab", ItemBlockMeta::new);
		concrete_double_slab = HbmRegistry.block(new BlockSlabConcrete(true, concrete_slab, concrete_smooth, concrete, concrete_asbestos, ducrete_smooth, ducrete, ModBlocks.asphalt), "concrete_double_slab");
		concrete_brick_slab = HbmRegistry.blockWithItem(new BlockSlabConcreteBrick(false, null, brick_concrete, ModBlocks.brick_concrete_mossy, ModBlocks.brick_concrete_cracked, ModBlocks.brick_concrete_broken, brick_ducrete), "concrete_brick_slab", ItemBlockMeta::new);
		concrete_brick_double_slab = HbmRegistry.block(new BlockSlabConcreteBrick(true, concrete_brick_slab, brick_concrete, ModBlocks.brick_concrete_mossy, ModBlocks.brick_concrete_cracked, ModBlocks.brick_concrete_broken, brick_ducrete), "concrete_brick_double_slab");
		brick_slab = HbmRegistry.blockWithItem(new BlockSlabBrick(false, null, ModBlocks.reinforced_stone, ModBlocks.reinforced_brick, ModBlocks.brick_obsidian, ModBlocks.brick_light, ModBlocks.brick_compound, brick_asbestos, ModBlocks.brick_fire), "brick_slab", ItemBlockMeta::new);
		brick_double_slab = HbmRegistry.block(new BlockSlabBrick(true, brick_slab, ModBlocks.reinforced_stone, ModBlocks.reinforced_brick, ModBlocks.brick_obsidian, ModBlocks.brick_light, ModBlocks.brick_compound, brick_asbestos, ModBlocks.brick_fire), "brick_double_slab");
		//six barbed wires (plain/burning/poisoned/acid/withering/super-death): one collision routine, extra effects by Effect tier,
		//entangle and slow run on both sides (client keeps it responsive), damage and potions only on the server — reasoning in BlockBarbedWire.
		//⚠ the old geometry was an OBJ rotated 90°/180° by meta; block-level render dispatch is gone in this version ⇒ facing and geometry move to the render side's
		//  custom baked-geometry batch (option ② approved at A1 03:25); this batch's block states keep the existing placeholder tier, no new properties.
		barbed_wire = HbmRegistry.blockWithItem(new BlockBarbedWire(BlockBarbedWire.Effect.PLAIN).setBlockTextureName(RefStrings.MODID + ":barbed_wire_model").setBlockName("barbed_wire").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "barbed_wire", ItemBlockBase::new);
		barbed_wire_fire = HbmRegistry.blockWithItem(new BlockBarbedWire(BlockBarbedWire.Effect.FIRE).setBlockTextureName(RefStrings.MODID + ":barbed_wire_fire_model").setBlockName("barbed_wire_fire").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "barbed_wire_fire", ItemBlockBase::new);
		barbed_wire_poison = HbmRegistry.blockWithItem(new BlockBarbedWire(BlockBarbedWire.Effect.POISON).setBlockTextureName(RefStrings.MODID + ":barbed_wire_poison_model").setBlockName("barbed_wire_poison").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "barbed_wire_poison", ItemBlockBase::new);
		barbed_wire_acid = HbmRegistry.blockWithItem(new BlockBarbedWire(BlockBarbedWire.Effect.ACID).setBlockTextureName(RefStrings.MODID + ":barbed_wire_acid_model").setBlockName("barbed_wire_acid").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "barbed_wire_acid", ItemBlockBase::new);
		barbed_wire_wither = HbmRegistry.blockWithItem(new BlockBarbedWire(BlockBarbedWire.Effect.WITHER).setBlockTextureName(RefStrings.MODID + ":barbed_wire_wither_model").setBlockName("barbed_wire_wither").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "barbed_wire_wither", ItemBlockBase::new);
		barbed_wire_ultradeath = HbmRegistry.blockWithItem(new BlockBarbedWire(BlockBarbedWire.Effect.ULTRADEATH).setBlockTextureName(RefStrings.MODID + ":barbed_wire_ultradeath_model").setBlockName("barbed_wire_ultradeath").setCreativeTab(ModBlocks.blockTab).setHardness(5.0F).setResistance(10.0F), "barbed_wire_ultradeath", ItemBlockBase::new);

		//four wasteland grounds (the old WasteEarth family): waste_earth / waste_mycelium decay or spread on their own, frozen_grass slows trampling,
		//burning_earth sets trampling feet alight and ignites surrounding grass. Drops (three drop dirt, frozen_grass drops snowballs) go through four tables per §15,
		//named hbm:blocks/<registry name>; the class head records everything that changed faces here (icons→JSON models, canLightningStrikeAt, blockExists).
		waste_earth = HbmRegistry.blockWithItem(new BlockWasteEarth(Material.GROUND, true).setStepSound(SoundType.PLANT).setBlockTextureName(RefStrings.MODID + ":waste_earth").setBlockName("waste_earth").setCreativeTab(ModBlocks.blockTab).setHardness(0.6F), "waste_earth", ItemBlockBase::new);
		waste_mycelium = HbmRegistry.blockWithItem(new BlockWasteEarth(Material.GROUND, true).setStepSound(SoundType.PLANT).setBlockTextureName(RefStrings.MODID + ":waste_mycelium_side").setBlockName("waste_mycelium").setCreativeTab(ModBlocks.blockTab).setHardness(0.6F).setLightLevel(1.0F), "waste_mycelium", ItemBlockBase::new);
		frozen_grass = HbmRegistry.blockWithItem(new BlockWasteEarth(Material.GROUND, false).setStepSound(SoundType.GLASS).setBlockName("frozen_grass").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.5F), "frozen_grass", ItemBlockBase::new);
		burning_earth = HbmRegistry.blockWithItem(new BlockWasteEarth(Material.GROUND, true).setStepSound(SoundType.PLANT).setBlockTextureName(RefStrings.MODID + ":burning_earth").setBlockName("burning_earth").setCreativeTab(ModBlocks.blockTab).setHardness(0.6F), "burning_earth", ItemBlockBase::new);

		//one meteorite scorched earth (the only BlockDirt-family piece): drops regular dirt, table at hbm:blocks/impact_dirt;
		//it's also where burning_earth above ends up after burning (that link existed between the two families in the old version).
		impact_dirt = HbmRegistry.blockWithItem(new BlockDirt(Material.GROUND, true).setStepSound(SoundType.GROUND).setBlockTextureName(RefStrings.MODID + ":waste_earth_bottom").setBlockName("impact_dirt").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F), "impact_dirt", ItemBlockBase::new);

		//one wasteland mushroom (the only BlockMush-family piece): plant shape (geometry in JSON's block/cross, see BlockMush's class head),
		//drops itself and skips the BlockLoot bridge so no table; carries a half tier of full light (old setLightLevel(0.5F)).
		mush = HbmRegistry.blockWithItem(new BlockMush(Material.PLANTS).setStepSound(SoundType.PLANT).setBlockTextureName(RefStrings.MODID + ":mush").setBlockName("mush").setCreativeTab(ModBlocks.blockTab).setLightLevel(0.5F), "mush", ItemBlockBase::new);

		//stalactite and stalagmite pair (two instances of the old BlockStalagmite family sharing one eight-tier material enum): cross shape in JSON (parent block/cross),
		//support checks run opposite ways (stalagmite wants solid below, stalactite solid above, and both drop when unsupported); they block neither entities nor sight.
		//Drops are per-tier items (sulfur / asbestos powder / flesh / ice powder / snowball; the three bug tiers never dropped anything in the old version), so items go by subtype via ItemBlockMeta.
		//⚠ the old pair of OreCave-cavegen placement entries didn't follow: that feature isn't registered yet, so this batch only guarantees creative-tab placement — see the class comment.
		stalactite = HbmRegistry.blockWithItem(new BlockStalagmite(true).setBlockName("stalactite").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.0F), "stalactite", ItemBlockMeta::new);
		stalagmite = HbmRegistry.blockWithItem(new BlockStalagmite(false).setBlockName("stalagmite").setCreativeTab(ModBlocks.blockTab).setHardness(0.5F).setResistance(2.0F), "stalagmite", ItemBlockMeta::new);

		//Four surface layers (four instances of the old BlockLayering family): the eight thickness steps become a thickness property, geometry handed to JSON (step 0 uses <name>.json, steps 1..7 use <name>_N.json).
		//Mining them drops nothing; four empty tables hbm:blocks/<registry name> say so. The classes that would lay them down (firefighting foam / drained oil / fallen leaves / ground oil stains) are not registered yet,
		//and the old foam can cover reactor debris and blown-up thorium crates rule is logged as skipped along with them; the reasons live in the BlockLayering class comment.
		foam_layer = HbmRegistry.blockWithItem(new BlockLayering(Material.SNOW, true).setStepSound(SoundType.SNOW).setBlockTextureName(RefStrings.MODID + ":foam").setBlockName("foam_layer").setCreativeTab(ModBlocks.blockTab).setHardness(0.1F).setLightOpacity(0), "foam_layer", ItemBlockBase::new);
		sand_boron_layer = HbmRegistry.blockWithItem(new BlockLayering(Material.SAND, false).setStepSound(SoundType.SAND).setBlockTextureName(RefStrings.MODID + ":sand_boron").setBlockName("sand_boron_layer").setCreativeTab(ModBlocks.blockTab).setHardness(0.1F).setLightOpacity(0), "sand_boron_layer", ItemBlockBase::new);
		leaves_layer = HbmRegistry.blockWithItem(new BlockLayering(Material.LEAVES, true).setStepSound(SoundType.PLANT).setBlockTextureName(RefStrings.MODID + ":waste_leaves").setBlockName("leaves_layer").setCreativeTab(ModBlocks.blockTab).setHardness(0.1F).setLightOpacity(0), "leaves_layer", ItemBlockBase::new);
		oil_spill = HbmRegistry.blockWithItem(new BlockLayering(Material.GROUND, true).setStepSound(SoundType.SNOW).setBlockTextureName(RefStrings.MODID + ":oil_spill").setBlockName("oil_spill").setCreativeTab(ModBlocks.blockTab).setHardness(0.1F).setLightOpacity(0), "oil_spill", ItemBlockBase::new);
	}
}
