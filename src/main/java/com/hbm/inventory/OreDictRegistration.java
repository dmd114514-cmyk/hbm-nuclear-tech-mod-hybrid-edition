package com.hbm.inventory;

import static com.hbm.blocks.ModBlocks.*;
import static com.hbm.blocks.generic.ModBlocksGeneric.*;
import static com.hbm.inventory.OreDictManager.*;
import static com.hbm.inventory.OreDictManager.DictFrame.*;
import static com.hbm.items.ModItems.*;

import com.hbm.blocks.ModBlocks;
import com.hbm.hazard.HazardRegistry;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.items.ModItems;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ItemEnums.EnumBriquetteType;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ItemEnums.EnumCokeType;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.special.ItemBedrockOre.EnumBedrockOre;
import com.hbm.registry.IRegistrar;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreDictionary.OreRegisterEvent;

/**
 * The ore dictionary's registration surface: hangs raw materials — ingots, dusts, plates, blocks — into the dict per OreDictManager's naming rules.
 *
 * This face must land — most recipe tables key their inputs on ore dict names; without it everything compiles, starts,
 * logs nothing, yet not one entry matches at runtime.
 *
 * The old version ran this in MainRegistry's preInit; content here registers via RegistryEvent, and at preInit items
 * don't even have ids yet, so it moved to init(), ahead of the recipe tables.
 *
 * Entries marked "skip" in the table reference pieces not yet registered (ore block family, glass family, colored concrete, blowtorch and lantern and friends);
 * once those land, un-comment the lines and restore them as written.
 */
public class OreDictRegistration implements IRegistrar {

	@Override
	public String id() {
		return "inventory.oredict";
	}

	@Override
	public void init() {
		// Hang the event bus first, then build the alias table: every later ore registration re-derives collection keys from it
		MinecraftForge.EVENT_BUS.register(this);
		OreDictManager.registerGroups();
		registerOres();
		OreDictManager.selfCheck();
	}

	@SubscribeEvent
	public void onOreRegister(OreRegisterEvent event) {
		OreDictManager.onRegisterOre(event);
	}

	private static void registerOres() {


		/*
		 * VANILLA
		 */
		COAL.gem(Items.COAL).dustSmall(powder_coal_tiny).dust(powder_coal);
		IRON.plate(plate_iron).dust(powder_iron).ore(ore_gneiss_iron);	// skipped: ore_iron
		GOLD.plate(plate_gold).dust(powder_gold).ore(ore_gneiss_gold);	// skipped: ore_gold
		LAPIS.dust(powder_lapis);	// skipped: ore_lapis
		// skipped (piece not landed: ore_redstone) REDSTONE.oreAll(ore_redstone);
		NETHERQUARTZ.gem(Items.QUARTZ).dust(powder_quartz).ore(Blocks.QUARTZ_ORE);	// skipped: ore_quartz
		QUARTZ.dust(powder_quartz);
		DIAMOND.dust(powder_diamond).ore(gravel_diamond);	// skipped: ore_diamond,ore_sellafield_diamond
		EMERALD.dust(powder_emerald);	// skipped: ore_emerald,ore_sellafield_emerald
		GLOWSTONE.dust(Items.GLOWSTONE_DUST);

		/*
		 * RADIOACTIVE
		 */
		U.rad(HazardRegistry.u).nugget(nugget_uranium).billet(billet_uranium).ingot(ingot_uranium).dust(powder_uranium).block(block_uranium).ore(ore_uranium_scorched, ore_gneiss_uranium, ore_gneiss_uranium_scorched, ore_nether_uranium, ore_nether_uranium_scorched).oreNether(ore_nether_uranium, ore_nether_uranium_scorched);	// skipped: ore_sellafield_uranium_scorched,ore_uranium
		U233.rad(HazardRegistry.u233).nugget(nugget_u233).billet(billet_u233).ingot(ingot_u233).block(block_u233);
		U235.rad(HazardRegistry.u235).nugget(nugget_u235).billet(billet_u235).ingot(ingot_u235).block(block_u235);
		U238.rad(HazardRegistry.u238).nugget(nugget_u238).billet(billet_u238).ingot(ingot_u238).block(block_u238);
		TH232.rad(HazardRegistry.th232).nugget(nugget_th232).billet(billet_th232).ingot(ingot_th232).dust(powder_thorium).block(block_thorium);	// skipped: ore_thorium
		PU.rad(HazardRegistry.pu).nugget(nugget_plutonium).billet(billet_plutonium).ingot(ingot_plutonium).dust(powder_plutonium).block(block_plutonium).ore(ore_nether_plutonium).oreNether(ore_nether_plutonium);	// skipped: ore_plutonium
		PURG.rad(HazardRegistry.purg).neutron(HazardRegistry.purg/60f).nugget(nugget_pu_mix).billet(billet_pu_mix).ingot(ingot_pu_mix).block(block_pu_mix);
		PU238.rad(HazardRegistry.pu238).hot(3F).nugget(nugget_pu238).billet(billet_pu238).ingot(ingot_pu238);	// skipped: block_pu238
		PU239.rad(HazardRegistry.pu239).nugget(nugget_pu239).billet(billet_pu239).ingot(ingot_pu239).block(block_pu239);
		PU240.rad(HazardRegistry.pu240).neutron(HazardRegistry.pu240/120f).nugget(nugget_pu240).billet(billet_pu240).ingot(ingot_pu240).block(block_pu240);
		PU241.rad(HazardRegistry.pu241).nugget(nugget_pu241).billet(billet_pu241).ingot(ingot_pu241);	//.block(block_pu241);
		AM241.rad(HazardRegistry.am241).nugget(nugget_am241).billet(billet_am241).ingot(ingot_am241);
		AM242.rad(HazardRegistry.am242).nugget(nugget_am242).billet(billet_am242).ingot(ingot_am242);
		CM242.rad(HazardRegistry.cm242).nugget(nugget_cm242).billet(billet_cm242).ingot(ingot_cm242);
		CM243.rad(HazardRegistry.cm243).neutron(HazardRegistry.cm243/80f).nugget(nugget_cm243).billet(billet_cm243).ingot(ingot_cm243);
		CM244.rad(HazardRegistry.cm244).neutron(HazardRegistry.cm244/50f).nugget(nugget_cm244).billet(billet_cm244).ingot(ingot_cm244);
		CM245.rad(HazardRegistry.cm245).neutron(HazardRegistry.cm245/100f).nugget(nugget_cm245).billet(billet_cm245).ingot(ingot_cm245);
		CM246.rad(HazardRegistry.cm246).neutron(HazardRegistry.cm246/50f).nugget(nugget_cm246).billet(billet_cm246).ingot(ingot_cm246);
		CM247.rad(HazardRegistry.cm247).nugget(nugget_cm247).billet(billet_cm247).ingot(ingot_cm247);
		CMRG.rad(HazardRegistry.cmrg).neutron(HazardRegistry.cmrg/60f).nugget(nugget_cm_mix).billet(billet_cm_mix).ingot(ingot_cm_mix);
		CMF.rad(HazardRegistry.cmf).neutron(HazardRegistry.cmf/120f).nugget(nugget_cm_fuel).billet(billet_cm_fuel).ingot(ingot_cm_fuel);
		BK247.rad(HazardRegistry.bk247).nugget(nugget_bk247).billet(billet_bk247).ingot(ingot_bk247);
		CF251.rad(HazardRegistry.cf251).hot(2).nugget(nugget_cf251).billet(billet_cf251).ingot(ingot_cf251);
		CF252.rad(HazardRegistry.cf252).neutron(HazardRegistry.cf252/5f).hot(3).nugget(nugget_cf252).billet(billet_cf252).ingot(ingot_cf252);
		ES253.rad(HazardRegistry.es253).neutron(HazardRegistry.es253/40).hot(4).nugget(nugget_es253).billet(billet_es253).ingot(ingot_es253);
		ES255.rad(HazardRegistry.es255).ingot(ingot_es255);
		AMRG.rad(HazardRegistry.amrg).nugget(nugget_am_mix).billet(billet_am_mix).ingot(ingot_am_mix);
		NP237.rad(HazardRegistry.np237).nugget(nugget_neptunium).billet(billet_neptunium).ingot(ingot_neptunium).dust(powder_neptunium).block(block_neptunium);
		PO210.rad(HazardRegistry.po210).hot(3).nugget(nugget_polonium).billet(billet_polonium).ingot(ingot_polonium).dust(powder_polonium);	// skipped: block_polonium
		TC99.rad(HazardRegistry.tc99).nugget(nugget_technetium).billet(billet_technetium).ingot(ingot_technetium);
		RA226.rad(HazardRegistry.ra226).nugget(nugget_ra226).billet(billet_ra226).ingot(ingot_ra226).dust(powder_ra226).block(block_ra226);
		AC227.rad(HazardRegistry.ac227).nugget(nugget_actinium).billet(billet_actinium).ingot(ingot_actinium).dust(powder_actinium).block(block_actinium).dustSmall(powder_actinium_tiny);
		CO60.rad(HazardRegistry.co60).hot(1).nugget(nugget_co60).billet(billet_co60).ingot(ingot_co60).dust(powder_co60);
		AU198.rad(HazardRegistry.au198).hot(5).nugget(nugget_au198).billet(billet_au198).ingot(ingot_au198).dust(powder_au198);
		PB209.rad(HazardRegistry.pb209).blinding(50F).hot(7).nugget(nugget_pb209).billet(billet_pb209).ingot(ingot_pb209);
		SA326.rad(HazardRegistry.sa326).blinding(50F).nugget(nugget_schrabidium).billet(billet_schrabidium).ingot(ingot_schrabidium).dust(powder_schrabidium).plate(plate_schrabidium).plateCast(Mats.MAT_SCHRABIDIUM.make(plate_cast)).block(block_schrabidium).ore(ore_gneiss_schrabidium, ore_nether_schrabidium).oreNether(ore_nether_schrabidium);	// skipped: ore_schrabidium,ore_sellafield_schrabidium
		SA327.rad(HazardRegistry.sa327).blinding(50F).nugget(nugget_solinium).billet(billet_solinium).ingot(ingot_solinium).block(block_solinium);
		SBD.rad(HazardRegistry.sb).blinding(50F).ingot(ingot_schrabidate).dust(powder_schrabidate).block(block_schrabidate);
		SRN.rad(HazardRegistry.sr).blinding(50F).ingot(ingot_schraranium).block(block_schraranium);
		GH336.rad(HazardRegistry.gh336).nugget(nugget_gh336).billet(billet_gh336).ingot(ingot_gh336);
		MUD.rad(HazardRegistry.mud).ingot(ingot_mud);
		CN989.rad(HazardRegistry.cn989).hot(4F).nugget(nugget_cn989).billet(billet_cn989).ingot(ingot_cn989).dust(powder_cn989).plate(plate_cn989);

		/*
		 * STABLE
		 */
		NI.ingot(ingot_nickel).dust(powder_nickel).plate(plate_nickel).block(block_nickel).nugget(nugget_nickel);	// skipped: ore_nickel
		NIM.dust(fromOne(chunk_ore, EnumChunkType.PENTLANDITE));	// dust selected for compat reasons
		TI.ingot(ingot_titanium).dust(powder_titanium).plate(plate_titanium).block(block_titanium);	// skipped: ore_titanium
		CU.ingot(ingot_copper).dust(powder_copper).plate(plate_copper).block(block_copper).ore(ore_gneiss_copper);	// skipped: ore_copper
		MINGRADE.ingot(ingot_red_copper).dust(powder_red_copper).billet(billet_red_copper).block(block_red_copper);
		W.ingot(ingot_tungsten).dust(powder_tungsten).block(block_tungsten).ore(ore_nether_tungsten).oreNether(ore_nether_tungsten);	// skipped: ore_tungsten
		WC.ingot(ingot_tungsten_carbide);
		AL.ingot(ingot_aluminium).dust(powder_aluminium).plate(plate_aluminium).block(block_aluminium);	// skipped: ore_aluminium
		STEEL.ingot(ingot_steel).dustSmall(powder_steel_tiny).dust(powder_steel).plate(plate_steel).block(block_steel);
		STAINLESS.ingot(ingot_stainless).plate(plate_stainless);
		TCALLOY.ingot(ingot_tcalloy).dust(powder_tcalloy).block(block_tcalloy);
		CDALLOY.ingot(ingot_cdalloy).block(block_cdalloy);
		BBRONZE.ingot(ingot_bismuth_bronze);
		ABRONZE.ingot(ingot_arsenic_bronze);
		PB.nugget(nugget_lead).ingot(ingot_lead).dust(powder_lead).plate(plate_lead).block(block_lead);	// skipped: ore_lead
		BSCCO.ingot(ingot_bscco);
		BI.nugget(nugget_bismuth).billet(billet_bismuth).ingot(ingot_bismuth).dust(powder_bismuth).block(block_bismuth);
		MEN.nugget(nugget_menthol).billet(billet_menthol).ingot(ingot_menthol);
		AS.nugget(nugget_arsenic).ingot(ingot_arsenic);
		CA.ingot(ingot_calcium).dust(powder_calcium);
		CD.ingot(ingot_cadmium).dust(powder_cadmium).block(block_cadmium);
		TA.nugget(nugget_tantalium).gem(gem_tantalium).ingot(ingot_tantalium).dust(powder_tantalium).block(block_tantalium);
		COLTAN.ingot(fragment_coltan).dust(powder_coltan_ore).block(block_coltan);	// skipped: ore_coltan
		NB.nugget(nugget_niobium, fragment_niobium).ingot(ingot_niobium).dustSmall(powder_niobium_tiny).dust(powder_niobium).block(block_niobium);	// skipped: ore_niobium
		BE.nugget(nugget_beryllium).billet(billet_beryllium).ingot(ingot_beryllium).dust(powder_beryllium).block(block_beryllium);	// skipped: ore_beryllium
		CO.nugget(fragment_cobalt).nugget(nugget_cobalt).billet(billet_cobalt).ingot(ingot_cobalt).dust(powder_cobalt).dustSmall(powder_cobalt_tiny).block(block_cobalt).ore(ore_nether_cobalt);	// skipped: ore_cobalt
		B.nugget(fragment_boron).ingot(ingot_boron).dustSmall(powder_boron_tiny).dust(powder_boron).block(block_boron);
		SI.nugget(nugget_silicon).billet(billet_silicon).ingot(ingot_silicon);
		GRAPHITE.ingot(ingot_graphite);	// skipped: block_graphite
		CARBON.ingot(ingot_graphite);	// skipped: block_graphite
		DURA.ingot(ingot_dura_steel).dust(powder_dura_steel).plate(plate_dura_steel).block(block_dura_steel);
		POLYMER.ingot(ingot_polymer).dust(powder_polymer).block(block_polymer);
		BAKELITE.ingot(ingot_bakelite).dust(powder_bakelite).block(block_bakelite);
		LATEX.gem(ball_resin).ingot(ingot_biorubber);
		RUBBER.ingot(ingot_rubber).dust(powder_rubber).block(block_rubber);
		//PET																	.ingot(ingot_pet);
		PC.ingot(ingot_pc);
		PVC.ingot(ingot_pvc).dust(powder_pvc);
		SEMTEX.ingot(ingot_semtex);	// skipped: block_semtex
		MAGTUNG.ingot(ingot_magnetized_tungsten).dust(powder_magnetized_tungsten).block(block_magnetized_tungsten);
		CMB.ingot(ingot_combine_steel).dust(powder_combine_steel).plate(plate_combine_steel).block(block_combine_steel);
		DESH.nugget(nugget_desh).ingot(ingot_desh).dust(powder_desh).block(block_desh);
		STAR.ingot(ingot_starmetal).block(block_starmetal);
		GUNMETAL.ingot(ingot_gunmetal).plate(plate_gunmetal);
		WEAPONSTEEL.ingot(ingot_weaponsteel).plate(plate_weaponsteel);
		BIGMT.ingot(ingot_saturnite).plate(plate_saturnite);
		FERRO.ingot(ingot_ferrouranium);
		EUPH.nugget(nugget_euphemium).ingot(ingot_euphemium).dust(powder_euphemium).block(block_euphemium);
		DNT.nugget(nugget_dineutronium).ingot(ingot_dineutronium).dust(powder_dineutronium).block(block_dineutronium);
		FIBER.ingot(ingot_fiberglass).block(block_fiberglass);
		ASBESTOS.asbestos(1F).ingot(ingot_asbestos).dust(powder_asbestos).block(block_asbestos).ore(ore_gneiss_asbestos);	// skipped: EnumBasaltOreType,ore_asbestos,ore_basalt,stone_resource
		OSMIRIDIUM.nugget(nugget_osmiridium).ingot(ingot_osmiridium).block(block_osmiridium);
		ZI.nugget(nugget_zinc).ingot(ingot_zinc).dust(powder_zinc);	// skipped: ore_zinc
		GALLIUM.nugget(nugget_gallium).ingot(ingot_gallium).dust(powder_gallium).dustSmall(powder_gallium_tiny);
		GAAS.nugget(nugget_gaas).ingot(ingot_gaas).billet(billet_gaas);
		HAFNIUM.nugget(nugget_hafnium).ingot(ingot_hafnium);
		IRIDIUM.ingot(ingot_iridium);


		OSMIRIDIUM.nugget(nugget_osmiridium).ingot(ingot_osmiridium);

		/*
		 * DUST AND GEM ORES
		 */
		S.dust(sulfur).block(block_sulfur).ore(ore_nether_sulfur).oreNether(ore_nether_sulfur);	// skipped: EnumBasaltOreType,ore_basalt,ore_sulfur,stone_resource
		KNO.dust(niter).block(block_niter);	// skipped: ore_niter
		F.dust(fluorite).block(block_fluorite);	// skipped: EnumBasaltOreType,ore_basalt,ore_fluorite
		LIGNITE.gem(lignite).dust(powder_lignite);	// skipped: ore_lignite
		COALCOKE.gem(fromOne(coke, EnumCokeType.COAL)).dust(fromOne(powder_coke, EnumCokeType.COAL));	// skipped: block_coke
		PETCOKE.gem(fromOne(coke, EnumCokeType.PETROLEUM)).dust(fromOne(powder_coke, EnumCokeType.PETROLEUM));	// skipped: block_coke
		LIGCOKE.gem(fromOne(coke, EnumCokeType.LIGNITE)).dust(fromOne(powder_coke, EnumCokeType.LIGNITE));	// skipped: block_coke
		CINNABAR.crystal(cinnebar).gem(cinnebar).ore(ore_depth_cinnebar);	// skipped: ore_cinnebar
		BORAX.dust(powder_borax).ore(ore_depth_borax);
		CHLOROCALCITE.dust(powder_chlorocalcite);
		MOLYSITE.dust(powder_molysite);	// skipped: EnumBasaltOreType,ore_basalt
		SODALITE.gem(gem_sodalite);
		VOLCANIC.gem(gem_volcanic);	// skipped: EnumBasaltOreType,ore_basalt
		// skipped (piece not landed: stone_resource) HEMATITE																														.ore(fromOne(stone_resource, EnumStoneType.HEMATITE));
		MALACHITE.ingot(DictFrame.fromOne(chunk_ore, EnumChunkType.MALACHITE));	// skipped: stone_resource
		LIMESTONE.dust(powder_limestone);	// skipped: stone_resource
		// skipped (piece not landed: stone_resource) BAUXITE																															.gem(fromOne(stone_resource, EnumStoneType.BAUXITE));
		CRYOLITE.crystal(fromOne(chunk_ore, EnumChunkType.CRYOLITE));
		// skipped (piece not landed: block_slag) SLAG																									.block(block_slag);
		// skipped (piece not landed: stone_resource) CONGLOMERATE																													.ore(fromOne(stone_resource, EnumStoneType.CONGLOMERATE));
		RICHMAGMA.ingot(ingot_magma);
		PENTLANDITE.crystal(fromOne(chunk_ore, EnumChunkType.PENTLANDITE));

		/*
		 * HAZARDS, MISC
		 */
		LI.hydro(1F).ingot(lithium).dustSmall(powder_lithium_tiny).dust(powder_lithium).ore(ore_gneiss_lithium);	// skipped: block_lithium,ore_lithium
		NA.hydro(1F).hazIngot().dust(powder_sodium);

		/*
		 * PHOSPHORUS
		 */
		P_WHITE.hot(5).ingot(ingot_phosphorus).block(block_white_phosphorus);
		P_RED.dust(powder_fire).block(block_red_phosphorus);	// skipped: ore_fire

		/*
		 * RARE METALS
		 */
		AUSTRALIUM.nugget(nugget_australium).billet(billet_australium).ingot(ingot_australium).dust(powder_australium).block(block_australium);	// skipped: ore_australium
		TASMANITE.nugget(nugget_australium_lesser).billet(billet_australium_lesser).ingot(ingot_australium_lesser);
		AYERITE.nugget(nugget_australium_greater).billet(billet_australium_greater).ingot(ingot_australium_greater);


		/*
		 * RARE EARTHS
		 */
		RAREEARTH.ingot(DictFrame.fromOne(ModItems.chunk_ore, EnumChunkType.RARE)).ore(ore_gneiss_rare);	// skipped: ore_rare
		LA.nugget(nugget_lanthanium, fragment_lanthanium).ingot(ingot_lanthanium).dustSmall(powder_lanthanium_tiny).dust(powder_lanthanium).block(block_lanthanium);	// skipped: ore_lanthanium
		ZR.nugget(nugget_zirconium).ingot(ingot_zirconium).billet(billet_zirconium).dust(powder_zirconium).block(block_zirconium).ore(ore_depth_zirconium);
		ND.nugget(fragment_neodymium).dustSmall(powder_neodymium_tiny).dust(powder_neodymium).ore(ore_depth_nether_neodymium).oreNether(ore_depth_nether_neodymium);
		CE.nugget(fragment_cerium).dustSmall(powder_cerium_tiny).dust(powder_cerium);

		/*
		 * NITAN
		 */
		I.dust(powder_iodine);	// skipped: ore_iodine
		AT.dust(powder_astatine);
		CS.dust(powder_caesium);
		ST.dust(powder_strontium);
		BR.dust(powder_bromine);
		TS.dust(powder_tennessine);

		/*
		 * FISSION FRAGMENTS
		 */
		SR.hot(1F).hydro(1F).hazIngot().dust(powder_strontium);
		SR90.rad(HazardRegistry.sr90).hot(1F).hydro(1F).dustSmall(powder_sr90_tiny).dust(powder_sr90).ingot(ingot_sr90).billet(billet_sr90).nugget(nugget_sr90);
		I131.rad(HazardRegistry.i131).hot(1F).dustSmall(powder_i131_tiny).dust(powder_i131);
		XE135.rad(HazardRegistry.xe135).hot(10F).dustSmall(powder_xe135_tiny).dust(powder_xe135);
		CS137.rad(HazardRegistry.cs137).hot(3F).hydro(3F).dustSmall(powder_cs137_tiny).dust(powder_cs137);
		AT209.rad(HazardRegistry.at209).hot(20F).dust(powder_at209);

		/*
		 * COLLECTIONS
		 */
		ANY_GUNPOWDER.dust(Items.GUNPOWDER, ballistite, cordite);
		ANY_SMOKELESS.dust(ballistite, cordite);
		ANY_PLASTICEXPLOSIVE.ingot(ingot_semtex, ingot_c4);
		ANY_HIGHEXPLOSIVE.ingot(ball_tnt).ingot(ball_tatb);
		ANY_CONCRETE.any(concrete, concrete_smooth, concrete_asbestos, ducrete, ducrete_smooth);
		// skipped (piece not landed: concrete_colored) for(int i = 0; i < 16; i++) { ANY_CONCRETE.any(new ItemStack(ModBlocks.concrete_colored, 1, i)); }
		// skipped (piece not landed: concrete_colored_ext) for(int i = 0; i < 16; i++) { ANY_CONCRETE.any(new ItemStack(ModBlocks.concrete_colored_ext, 1, i)); }
		ANY_COKE.gem(fromAll(coke, EnumCokeType.class)).dust(fromAll(powder_coke, EnumCokeType.class));	// skipped: block_coke
		ANY_ASH.any(fromOne(ModItems.powder_ash, EnumAshType.WOOD), fromOne(ModItems.powder_ash, EnumAshType.COAL), fromOne(ModItems.powder_ash, EnumAshType.MISC), fromOne(ModItems.powder_ash, EnumAshType.FLY), fromOne(ModItems.powder_ash, EnumAshType.SOOT));

		/*
		 * TAR
		 */
		OreDictionary.registerOre(KEY_OIL_TAR, fromOne(oil_tar, EnumTarType.CRUDE));
		OreDictionary.registerOre(KEY_CRACK_TAR, fromOne(oil_tar, EnumTarType.CRACK));
		OreDictionary.registerOre(KEY_COAL_TAR, fromOne(oil_tar, EnumTarType.COAL));
		OreDictionary.registerOre(KEY_WOOD_TAR, fromOne(oil_tar, EnumTarType.WOOD));

		/*
		 * TANKS
		 */
		OreDictionary.registerOre(KEY_UNIVERSAL_TANK, new ItemStack(fluid_tank_full, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(KEY_HAZARD_TANK, new ItemStack(fluid_tank_lead_full, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(KEY_UNIVERSAL_BARREL, new ItemStack(fluid_barrel_full, 1, OreDictionary.WILDCARD_VALUE));

		/*
		 * TOOLS
		 */
		OreDictionary.registerOre(KEY_TOOL_SCREWDRIVER, new ItemStack(screwdriver, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(KEY_TOOL_SCREWDRIVER, new ItemStack(screwdriver_desh, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(KEY_TOOL_HANDDRILL, new ItemStack(hand_drill, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(KEY_TOOL_HANDDRILL, new ItemStack(hand_drill_desh, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(KEY_TOOL_CHEMISTRYSET, new ItemStack(chemistry_set, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre(KEY_TOOL_CHEMISTRYSET, new ItemStack(chemistry_set_boron, 1, OreDictionary.WILDCARD_VALUE));
		// skipped (piece not landed: blowtorch) OreDictionary.registerOre(KEY_TOOL_TORCH, new ItemStack(blowtorch, 1, OreDictionary.WILDCARD_VALUE));
		// skipped (piece not landed: acetylene_torch) OreDictionary.registerOre(KEY_TOOL_TORCH, new ItemStack(acetylene_torch, 1, OreDictionary.WILDCARD_VALUE));

		/*
		 * GLYPHID M E A T
		 */
		OreDictionary.registerOre(KEY_GLYPHID_MEAT, new ItemStack(glyphid_meat));
		OreDictionary.registerOre(KEY_GLYPHID_MEAT, new ItemStack(glyphid_meat_grilled));

		/*
		 * foude
		 */
		OreDictionary.registerOre(KEY_FLOUR, new ItemStack(flour));
		OreDictionary.registerOre(KEY_BUTTER, new ItemStack(butter));
		OreDictionary.registerOre(KEY_CHEESE, new ItemStack(cheese));
		// skipped (piece not landed: strawberry) OreDictionary.registerOre(KEY_STRAWBERRY, new ItemStack(strawberry));
		OreDictionary.registerOre(KEY_STRAWBERRY_ICECREAM, new ItemStack(s_cream));
		OreDictionary.registerOre(KEY_STRAWBERRY_MILKSHAKE, new ItemStack(glass_smilk));

		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.smeltable == SmeltingBehavior.SMELTABLE) {
				if(mat.autogen.contains(MaterialShapes.CASTPLATE)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.CASTPLATE.name() + name, new ItemStack(ModItems.plate_cast, 1, mat.id));
				if(mat.autogen.contains(MaterialShapes.WELDEDPLATE)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.WELDEDPLATE.name() + name, new ItemStack(ModItems.plate_welded, 1, mat.id));
				if(mat.autogen.contains(MaterialShapes.DENSEWIRE)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.DENSEWIRE.name() + name, new ItemStack(ModItems.wire_dense, 1, mat.id));
			}
			if(mat.autogen.contains(MaterialShapes.BOLT)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.BOLT.name() + name, new ItemStack(ModItems.bolt, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.INGOT)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.INGOT.name() + name, new ItemStack(ModItems.ingot_raw, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.SHELL)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.SHELL.name() + name, new ItemStack(ModItems.shell, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.PIPE)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.PIPE.name() + name, new ItemStack(ModItems.pipe, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.FRAGMENT)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.FRAGMENT.name() + name, new ItemStack(ModItems.bedrock_ore_fragment, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.WIRE)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.WIRE.name() + name, new ItemStack(ModItems.wire_fine, 1, mat.id));

			if(mat.autogen.contains(MaterialShapes.LIGHTBARREL)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.LIGHTBARREL.name() + name, new ItemStack(ModItems.part_barrel_light, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.HEAVYBARREL)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.HEAVYBARREL.name() + name, new ItemStack(ModItems.part_barrel_heavy, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.LIGHTRECEIVER)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.LIGHTRECEIVER.name() + name, new ItemStack(ModItems.part_receiver_light, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.HEAVYRECEIVER)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.HEAVYRECEIVER.name() + name, new ItemStack(ModItems.part_receiver_heavy, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.MECHANISM)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.MECHANISM.name() + name, new ItemStack(ModItems.part_mechanism, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.STOCK)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.STOCK.name() + name, new ItemStack(ModItems.part_stock, 1, mat.id));
			if(mat.autogen.contains(MaterialShapes.GRIP)) for(String name : mat.names) OreDictionary.registerOre(MaterialShapes.GRIP.name() + name, new ItemStack(ModItems.part_grip, 1, mat.id));
		}

		for(EnumBedrockOre ore : EnumBedrockOre.values()) {
			OreDictionary.registerOre("ore" + ore.oreName, new ItemStack(ModItems.ore_enriched, 1, ore.ordinal()));
		}

		OreDictionary.registerOre("itemRubber", ingot_rubber);

		OreDictionary.registerOre("coalCoke", fromOne(coke, EnumCokeType.COAL));

		for(String name : new String[] {"fuelCoke", "coke"}) {
			OreDictionary.registerOre(name, fromOne(coke, EnumCokeType.COAL));
			OreDictionary.registerOre(name, fromOne(coke, EnumCokeType.LIGNITE));
			OreDictionary.registerOre(name, fromOne(coke, EnumCokeType.PETROLEUM));
		}

		OreDictionary.registerOre("briquetteCoal", fromOne(briquette, EnumBriquetteType.COAL));
		OreDictionary.registerOre("briquetteLignite", fromOne(briquette, EnumBriquetteType.LIGNITE));
		OreDictionary.registerOre("briquetteWood", fromOne(briquette, EnumBriquetteType.WOOD));

		OreDictionary.registerOre("logWood", pink_log);
		OreDictionary.registerOre("logWoodPink", pink_log);
		OreDictionary.registerOre("plankWood", pink_planks);
		OreDictionary.registerOre("plankWoodPink", pink_planks);
		// skipped (piece not landed: pink_slab) OreDictionary.registerOre("slabWood", pink_slab);
		// skipped (piece not landed: pink_slab) OreDictionary.registerOre("slabWoodPink", pink_slab);
		OreDictionary.registerOre("stairWood", pink_stairs);
		OreDictionary.registerOre("stairWoodPink", pink_stairs);

		String[] dyes = { "Black", "Red", "Green", "Brown", "Blue", "Purple", "Cyan", "LightGray", "Gray", "Pink", "Lime", "Yellow", "LightBlue", "Magenta", "Orange", "White" };
		for(int i = 0; i < 16; i++) {
			String dyeName = "dye" + dyes[i];

			OreDictionary.registerOre(dyeName, new ItemStack(ModItems.chemical_dye, 1, i));

			OreDictionary.registerOre(dyeName, new ItemStack(ModItems.crayon, 1, i));
		}
		OreDictionary.registerOre("dye", new ItemStack(chemical_dye, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("dye", new ItemStack(crayon, 1, OreDictionary.WILDCARD_VALUE));

		OreDictionary.registerOre("dyeRed", cinnebar);
		OreDictionary.registerOre("dye", cinnebar);
		OreDictionary.registerOre("dyeYellow", sulfur);
		OreDictionary.registerOre("dye", sulfur);
		OreDictionary.registerOre("dyeBlack", powder_coal);
		OreDictionary.registerOre("dye", powder_coal);
		OreDictionary.registerOre("dyeBrown", powder_lignite);
		OreDictionary.registerOre("dye", powder_lignite);
		OreDictionary.registerOre("dyeLightGray", powder_titanium);
		OreDictionary.registerOre("dye", powder_titanium);
		OreDictionary.registerOre("dyeWhite", fluorite);
		OreDictionary.registerOre("dye", fluorite);
		OreDictionary.registerOre("dyeBlue", powder_lapis);
		OreDictionary.registerOre("dye", powder_lapis);
		OreDictionary.registerOre("dyeBlack", fromOne(oil_tar, EnumTarType.CRUDE));
		OreDictionary.registerOre("dyeBlack", fromOne(oil_tar, EnumTarType.CRACK));
		OreDictionary.registerOre("dyeGray", fromOne(oil_tar, EnumTarType.COAL));
		OreDictionary.registerOre("dyeBrown", fromOne(oil_tar, EnumTarType.WOOD));
		OreDictionary.registerOre("dyeCyan", fromOne(oil_tar, EnumTarType.WAX));
		OreDictionary.registerOre("dyeWhite", fromOne(oil_tar, EnumTarType.PARAFFIN));
		OreDictionary.registerOre("dye", new ItemStack(oil_tar, 1, OreDictionary.WILDCARD_VALUE));
		OreDictionary.registerOre("dyeOrange", powder_cadmium);
		OreDictionary.registerOre("dye", powder_cadmium);
		OreDictionary.registerOre("dyeLightGray", fromOne(powder_ash, EnumAshType.WOOD));
		OreDictionary.registerOre("dyeBlack", fromOne(powder_ash, EnumAshType.COAL));
		OreDictionary.registerOre("dyeGray", fromOne(powder_ash, EnumAshType.MISC));
		OreDictionary.registerOre("dyeBrown", fromOne(powder_ash, EnumAshType.FLY));
		OreDictionary.registerOre("dyeBlack", fromOne(powder_ash, EnumAshType.SOOT));
		OreDictionary.registerOre("dyeMagenta", fromOne(powder_ash, EnumAshType.FULLERENE));
		OreDictionary.registerOre("dye", new ItemStack(powder_ash, 1, OreDictionary.WILDCARD_VALUE));

		// skipped (piece not landed: glass_boron) OreDictionary.registerOre("blockGlass", glass_boron);
		// skipped (piece not landed: glass_lead) OreDictionary.registerOre("blockGlass", glass_lead);
		// skipped (piece not landed: glass_uranium) OreDictionary.registerOre("blockGlass", glass_uranium);
		// skipped (piece not landed: glass_trinitite) OreDictionary.registerOre("blockGlass", glass_trinitite);
		// skipped (piece not landed: glass_polonium) OreDictionary.registerOre("blockGlass", glass_polonium);
		// skipped (piece not landed: glass_ash) OreDictionary.registerOre("blockGlass", glass_ash);
		// skipped (piece not landed: glass_uranium) OreDictionary.registerOre("blockGlassYellow", glass_uranium);
		// skipped (piece not landed: glass_trinitite) OreDictionary.registerOre("blockGlassLime", glass_trinitite);
		// skipped (piece not landed: glass_polonium) OreDictionary.registerOre("blockGlassRed", glass_polonium);
		// skipped (piece not landed: glass_ash) OreDictionary.registerOre("blockGlassBlack", glass_ash);

		OreDictionary.registerOre("container1000lubricant", bdcl);
		OreDictionary.registerOre("itemSilicon", billet_silicon);

		OreDictionary.registerOre(KEY_SAND, duna_sands);
		// skipped (piece not landed: laythe_silt) OreDictionary.registerOre(KEY_SAND, laythe_silt);
		OreDictionary.registerOre(KEY_SAND, eve_silt);
		OreDictionary.registerOre(KEY_SAND, moon_turf);
		OreDictionary.registerOre(KEY_SAND, rubber_silt);
		OreDictionary.registerOre(KEY_SAND, vinyl_sand);

		OreDictionary.registerOre(KEY_STONE, duna_rock);
		OreDictionary.registerOre(KEY_COBBLESTONE, duna_cobble);
		OreDictionary.registerOre(KEY_COBBLESTONE, dres_rock);
		// skipped (piece not landed: ike_regolith) OreDictionary.registerOre(KEY_COBBLESTONE, ike_regolith);
		OreDictionary.registerOre(KEY_STONE, ike_stone);
		OreDictionary.registerOre(KEY_COBBLESTONE, eve_rock);
		OreDictionary.registerOre(KEY_COBBLESTONE, moho_regolith);
		OreDictionary.registerOre(KEY_STONE, moho_stone);
		OreDictionary.registerOre(KEY_COBBLESTONE, moon_rock);
		OreDictionary.registerOre(KEY_COBBLESTONE, minmus_regolith);
		OreDictionary.registerOre(KEY_STONE, minmus_smooth);
		OreDictionary.registerOre(KEY_STONE, minmus_stone);
		OreDictionary.registerOre(KEY_COBBLESTONE, basalt);
		OreDictionary.registerOre(KEY_STONE, basalt_smooth);

		OreDictionary.registerOre(KEY_LOG, vinyl_log);
		OreDictionary.registerOre(KEY_LOG, pvc_log);
		// skipped (piece not landed: laythe_root) OreDictionary.registerOre(KEY_LOG, laythe_root);

		OreDictionary.registerOre(KEY_PLANKS, vinyl_planks);
		OreDictionary.registerOre(KEY_PLANKS, pvc_planks);

		OreDictionary.registerOre(KEY_STICK, stick_vinyl);
		OreDictionary.registerOre(KEY_STICK, stick_pvc);

		/*
		 * skip: the old section hooked fragment 9-into-1 recipes onto the crushed ore / gem / crystal / ingot / billet chains per material,
		 * It's com.hbm.crafting.MineralRecipes' business; that table hasn't landed, so the whole section sits untouched.
		 */

		MaterialShapes.registerCompatShapes();
		compensateMojangSpaghettiBullshit();

	}
}
