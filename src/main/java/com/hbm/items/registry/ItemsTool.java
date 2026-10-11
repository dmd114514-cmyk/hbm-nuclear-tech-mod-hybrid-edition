package com.hbm.items.registry;

import java.util.ArrayList;
import java.util.List;

import api.hbm.block.IToolable.ToolType;

import com.hbm.items.ModItems;
import com.hbm.items.tool.BigSword;
import com.hbm.items.weapon.BSword;
import com.hbm.items.tool.HoeSchrabidium;
import com.hbm.items.tool.ItemAmatExtractor;
import com.hbm.items.tool.ItemAnalysisTool;
import com.hbm.items.tool.ItemCouplingTool;
import com.hbm.items.tool.ItemCraftingDegradation;
import com.hbm.items.tool.ItemCounterfeitKeys;
import com.hbm.items.tool.ItemFilter;
import com.hbm.items.tool.ItemFusionCore;
import com.hbm.items.tool.ItemWandTime;
import com.hbm.items.tool.ItemWiring;
import com.hbm.items.tool.ModHoe;
import com.hbm.items.tool.ItemDetonator;
import com.hbm.items.tool.ItemDigammaDiagnostic;
import com.hbm.items.tool.ItemDiscord;
import com.hbm.items.tool.ItemMatch;
import com.hbm.items.tool.ItemMultiDetonator;
import com.hbm.items.tool.ItemPipette;
import com.hbm.items.tool.ItemPlasticBag;
import com.hbm.items.tool.ItemStructurePattern;
import com.hbm.items.tool.ItemStructureRandomized;
import com.hbm.items.tool.ItemStructureRandomly;
import com.hbm.items.tool.ItemStructureSingle;
import com.hbm.items.tool.ItemStructureSolid;
import com.hbm.items.tool.ItemDosimeter;
import com.hbm.items.tool.ItemFertilizer;
import com.hbm.items.tool.ItemGeigerCounter;
import com.hbm.items.tool.ItemKey;
import com.hbm.items.tool.ItemLeadBox;
import com.hbm.items.tool.ItemToolBox;
import com.hbm.items.tool.ItemTooling;
import com.hbm.items.tool.ItemToolingWeapon;
import com.hbm.items.tool.ModSword;
import com.hbm.items.tool.RedstoneSword;
import com.hbm.main.MainRegistry;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

/**
 * The tools subdomain registration unit.
 *
 * Only the "wears itself out while crafting" kind can land for now — the screwdriver/drill/torch family waits for `api.hbm.block.IToolable`
 * and `com.hbm.handler.ability.*` to freeze, or the interfaces won't line up. The spot is claimed; add on landing.
 */
public class ItemsTool implements IRegistrar {

	// The plated cobalt and starmetal tool materials: the old two tiers weren't in MainRegistry's tMat table — they were local variables built in place by the old ModItems.init,
	// shared by the family's sword/pick/axe/shovel. Those four pieces (ItemSwordAbility/ItemToolAbility) haven't landed, so the materials park on this registrar,
	// ready for the weapon family to reuse these fields — no second addToolMaterial.
	// Building the materials must happen in preInit, not as static initializers: ModItems' SUB list news up the registrars during its own class init,
	// when ingot_cobalt/ingot_starmetal are still unassigned, and the repair chain would burn into empty stacks.
	private static Item.ToolMaterial matDecCobalt;
	private static Item.ToolMaterial matStarmetal;

	private final List<Item> created = new ArrayList<>();

	@Override
	public String id() {
		return "items.tool";
	}

	@Override
	public void preInit() {
		// Both self-built materials land here, the repair chain matching the old version (this version's Item.ToolMaterial still has setRepairItem, javap-verified)
		matDecCobalt = EnumHelper.addToolMaterial("HBM_COBALT2", 3, 2500, 15.0F, 2.5F, 75).setRepairItem(new ItemStack(ModItems.ingot_cobalt));
		matStarmetal = EnumHelper.addToolMaterial("HBM_STARMETAL", 3, 3000, 20.0F, 2.5F, 100).setRepairItem(new ItemStack(ModItems.ingot_starmetal));

		created.add(ModItems.chemistry_set = HbmRegistry.item(new ItemCraftingDegradation(100).setUnlocalizedName("chemistry_set").setCreativeTab(ModItems.controlTab), "chemistry_set"));
		created.add(ModItems.chemistry_set_boron = HbmRegistry.item(new ItemCraftingDegradation(0).setUnlocalizedName("chemistry_set_boron").setCreativeTab(ModItems.controlTab), "chemistry_set_boron"));
		created.add(ModItems.powder_fertilizer = HbmRegistry.item(new ItemFertilizer().setUnlocalizedName("powder_fertilizer").setCreativeTab(ModItems.partsTab), "powder_fertilizer"));
		created.add(ModItems.key = HbmRegistry.item(new ItemKey().setUnlocalizedName("key").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "key"));
		created.add(ModItems.key_fake = HbmRegistry.item(new ItemKey().setUnlocalizedName("key_fake").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "key_fake"));
		created.add(ModItems.bismuth_tool = HbmRegistry.item(new ItemAmatExtractor().setUnlocalizedName("bismuth_tool").setMaxStackSize(1).setFull3D().setCreativeTab(null), "bismuth_tool"));
		created.add(ModItems.coupling_tool = HbmRegistry.item(new ItemCouplingTool().setUnlocalizedName("coupling_tool").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "coupling_tool"));
		// The lead box: opens from hand, riding GUIHandler's "held IGUIProvider" path
		created.add(ModItems.containment_box = HbmRegistry.item(new ItemLeadBox().setUnlocalizedName("containment_box"), "containment_box"));
		// The plastic bag: one item per slot, recognized by the hazard shielding, so the GUI likewise comes from the item
		created.add(ModItems.plastic_bag = HbmRegistry.item(new ItemPlasticBag().setUnlocalizedName("plastic_bag").setCreativeTab(ModItems.consumableTab), "plastic_bag"));
		// The toolbox: held sneak-right-click opens the GUI, plain right-click swaps a whole row of hot bars; hazard containers' radiation shielding recognizes it too
		created.add(ModItems.toolbox = HbmRegistry.item(new ItemToolBox().setUnlocalizedName("toolbox").setCreativeTab(ModItems.consumableTab), "toolbox"));

		// Only buildable once the material table is in: these three use HBM's own tool materials
		created.add(ModItems.crowbar = HbmRegistry.item(new ModSword(MainRegistry.tMatSteel).setUnlocalizedName("crowbar").setFull3D(), "crowbar"));
		created.add(ModItems.pipe_lead = HbmRegistry.item(new ModSword(MainRegistry.enumToolMaterialPipeLead).setUnlocalizedName("weapon_pipe_lead").setFull3D(), "pipe_lead"));
		created.add(ModItems.reer_graar = HbmRegistry.item(new ModSword(MainRegistry.tMatTitan).setUnlocalizedName("reer_graar").setFull3D(), "reer_graar"));
		created.add(ModItems.schrabidium_hoe = HbmRegistry.item(new HoeSchrabidium(MainRegistry.tMatSchrab).setUnlocalizedName("schrabidium_hoe"), "schrabidium_hoe"));
		// Six plain hoes: literally new ModHoe(material) in the old version, materials all present in the main table now; the old setTextureName finds its model by registry name here, not ported
		created.add(ModItems.titanium_hoe = HbmRegistry.item(new ModHoe(MainRegistry.tMatTitan).setUnlocalizedName("titanium_hoe"), "titanium_hoe"));
		created.add(ModItems.steel_hoe = HbmRegistry.item(new ModHoe(MainRegistry.tMatSteel).setUnlocalizedName("steel_hoe"), "steel_hoe"));
		created.add(ModItems.alloy_hoe = HbmRegistry.item(new ModHoe(MainRegistry.tMatAlloy).setUnlocalizedName("alloy_hoe"), "alloy_hoe"));
		created.add(ModItems.cmb_hoe = HbmRegistry.item(new ModHoe(MainRegistry.tMatCMB).setUnlocalizedName("cmb_hoe"), "cmb_hoe"));
		created.add(ModItems.desh_hoe = HbmRegistry.item(new ModHoe(MainRegistry.tMatDesh).setUnlocalizedName("desh_hoe"), "desh_hoe"));
		created.add(ModItems.cobalt_hoe = HbmRegistry.item(new ModHoe(MainRegistry.tMatCobalt).setUnlocalizedName("cobalt_hoe"), "cobalt_hoe"));
		// The plated-cobalt and starmetal hoes: materials are the two tiers above; those two old lines likewise only set material, no tab
		created.add(ModItems.cobalt_decorated_hoe = HbmRegistry.item(new ModHoe(matDecCobalt).setUnlocalizedName("cobalt_decorated_hoe"), "cobalt_decorated_hoe"));
		created.add(ModItems.starmetal_hoe = HbmRegistry.item(new ModHoe(matStarmetal).setUnlocalizedName("starmetal_hoe"), "starmetal_hoe"));
		// The spool: stringing lines between pylon columns; parts tab in the old version
		created.add(ModItems.wiring_red_copper = HbmRegistry.item(new ItemWiring().setUnlocalizedName("wiring_red_copper").setCreativeTab(ModItems.partsTab), "wiring_red_copper"));

		// Five gas mask filters: click onto a helmet to install; the swapped-out old filter returns to hand. Tab comes from the constructor uniformly (consumableTab)
		created.add(ModItems.gas_mask_filter = HbmRegistry.item(new ItemFilter().setUnlocalizedName("gas_mask_filter"), "gas_mask_filter"));
		created.add(ModItems.gas_mask_filter_mono = HbmRegistry.item(new ItemFilter().setUnlocalizedName("gas_mask_filter_mono"), "gas_mask_filter_mono"));
		created.add(ModItems.gas_mask_filter_combo = HbmRegistry.item(new ItemFilter().setUnlocalizedName("gas_mask_filter_combo"), "gas_mask_filter_combo"));
		created.add(ModItems.gas_mask_filter_rag = HbmRegistry.item(new ItemFilter().setUnlocalizedName("gas_mask_filter_rag"), "gas_mask_filter_rag"));
		created.add(ModItems.gas_mask_filter_piss = HbmRegistry.item(new ItemFilter().setUnlocalizedName("gas_mask_filter_piss"), "gas_mask_filter_piss"));

		created.add(ModItems.redstone_sword = HbmRegistry.item(new RedstoneSword(Item.ToolMaterial.STONE).setUnlocalizedName("redstone_sword").setCreativeTab(CreativeTabs.COMBAT), "redstone_sword"));
		// 1.12.2's ToolMaterial only has WOOD/STONE/IRON/DIAMOND/GOLD: the old EMERALD is today's DIAMOND
		created.add(ModItems.big_sword = HbmRegistry.item(new BigSword(Item.ToolMaterial.DIAMOND).setUnlocalizedName("big_sword").setCreativeTab(CreativeTabs.COMBAT), "big_sword"));
		created.add(ModItems.butter_sword = HbmRegistry.item(new BSword(Item.ToolMaterial.WOOD).setUnlocalizedName("butter_sword").setCreativeTab(CreativeTabs.COMBAT), "butter_sword"));

		// Two dosimeters: they chirp while carried, right-click prints the reading to chat; ArmorFSB's geiger audio yields to "are these two on the body"
		created.add(ModItems.geiger_counter = HbmRegistry.item(new ItemGeigerCounter().setUnlocalizedName("geiger_counter").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "geiger_counter"));
		created.add(ModItems.dosimeter = HbmRegistry.item(new ItemDosimeter().setUnlocalizedName("dosimeter").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "dosimeter"));

		// Two detonators: single-point and multi-point; both "sneak-right-click to record coordinates, right-click to detonate", with the block's own IBomb answering
		created.add(ModItems.detonator = HbmRegistry.item(new ItemDetonator().setUnlocalizedName("detonator").setMaxStackSize(1).setFull3D().setCreativeTab(ModItems.nukeTab), "detonator"));
		created.add(ModItems.detonator_multi = HbmRegistry.item(new ItemMultiDetonator().setUnlocalizedName("detonator_multi").setMaxStackSize(1).setFull3D().setCreativeTab(ModItems.nukeTab), "detonator_multi"));

		// Matches ride vanilla's tools tab (tabTools in the old version too); the discord wand and diagnostic go to the consumables tab
		created.add(ModItems.matchstick = HbmRegistry.item(new ItemMatch().setUnlocalizedName("matchstick").setFull3D().setCreativeTab(CreativeTabs.TOOLS), "matchstick"));
		created.add(ModItems.rod_of_discord = HbmRegistry.item(new ItemDiscord().setUnlocalizedName("rod_of_discord").setMaxStackSize(1).setFull3D().setCreativeTab(ModItems.consumableTab), "rod_of_discord"));
		created.add(ModItems.digamma_diagnostic = HbmRegistry.item(new ItemDigammaDiagnostic().setUnlocalizedName("digamma_diagnostic").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "digamma_diagnostic"));

		// Tool family: the block itself says which tool it wants; durability 0 means the "never wears" tier
		created.add(ModItems.screwdriver = HbmRegistry.item(new ItemTooling(ToolType.SCREWDRIVER, 100).setUnlocalizedName("screwdriver"), "screwdriver"));
		created.add(ModItems.screwdriver_desh = HbmRegistry.item(new ItemTooling(ToolType.SCREWDRIVER, 0).setUnlocalizedName("screwdriver_desh"), "screwdriver_desh"));
		created.add(ModItems.hand_drill = HbmRegistry.item(new ItemTooling(ToolType.HAND_DRILL, 100).setUnlocalizedName("hand_drill"), "hand_drill"));
		created.add(ModItems.hand_drill_desh = HbmRegistry.item(new ItemTooling(ToolType.HAND_DRILL, 0).setUnlocalizedName("hand_drill_desh"), "hand_drill_desh"));
		created.add(ModItems.wrench_archineer = HbmRegistry.item(new ItemToolingWeapon(ToolType.WRENCH, 1000, 12F).setUnlocalizedName("wrench_archineer"), "wrench_archineer"));
		created.add(ModItems.analysis_tool = HbmRegistry.item(new ItemAnalysisTool().setUnlocalizedName("analysis_tool").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "analysis_tool"));
		created.add(ModItems.wand_time = HbmRegistry.item(new ItemWandTime().setUnlocalizedName("wand_time").setMaxStackSize(1).setFull3D().setCreativeTab(ModItems.consumableTab), "wand_time"));

		// Three pipettes: the tube rides layer0, the fluid column is layer1 tinted by fluid
		created.add(ModItems.pipette = HbmRegistry.item(new ItemPipette().setUnlocalizedName("pipette").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "pipette"));
		created.add(ModItems.pipette_boron = HbmRegistry.item(new ItemPipette().setUnlocalizedName("pipette_boron").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "pipette_boron"));
		created.add(ModItems.pipette_laboratory = HbmRegistry.item(new ItemPipette().setUnlocalizedName("pipette_laboratory").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "pipette_laboratory"));

		// Five structure samplers: none in the creative tab (vanilla behavior), anchored to the bedrock origin
		created.add(ModItems.structure_single = HbmRegistry.item(new ItemStructureSingle().setUnlocalizedName("structure_single").setMaxStackSize(1).setFull3D().setCreativeTab(null), "structure_single"));
		created.add(ModItems.structure_solid = HbmRegistry.item(new ItemStructureSolid().setUnlocalizedName("structure_solid").setMaxStackSize(1).setFull3D().setCreativeTab(null), "structure_solid"));
		created.add(ModItems.structure_pattern = HbmRegistry.item(new ItemStructurePattern().setUnlocalizedName("structure_pattern").setMaxStackSize(1).setFull3D().setCreativeTab(null), "structure_pattern"));
		created.add(ModItems.structure_randomized = HbmRegistry.item(new ItemStructureRandomized().setUnlocalizedName("structure_randomized").setMaxStackSize(1).setFull3D().setCreativeTab(null), "structure_randomized"));
		created.add(ModItems.structure_randomly = HbmRegistry.item(new ItemStructureRandomly().setUnlocalizedName("structure_randomly").setMaxStackSize(1).setFull3D().setCreativeTab(null), "structure_randomly"));

		// Two fusion cores: one-shot full charge for the whole power armor set; tiers differ by how much they charge; also used to imprint key blanks
		created.add(ModItems.hev_battery = HbmRegistry.item(new ItemFusionCore(150000).setUnlocalizedName("hev_battery").setMaxStackSize(4).setCreativeTab(ModItems.controlTab), "hev_battery"));
		created.add(ModItems.fusion_core = HbmRegistry.item(new ItemFusionCore(2500000).setUnlocalizedName("fusion_core").setMaxStackSize(1).setCreativeTab(ModItems.controlTab), "fusion_core"));
		created.add(ModItems.key_kit = HbmRegistry.item(new ItemCounterfeitKeys().setUnlocalizedName("key_kit").setMaxStackSize(1).setCreativeTab(ModItems.consumableTab), "key_kit"));
	}

	@Override
	public void registerModels() {
		for(Item item : created) ItemModels.registerAll(item);
	}
}
