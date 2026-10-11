package com.hbm.items.registry;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.armor.ArmorAJR;
import com.hbm.items.armor.ArmorAshGlasses;
import com.hbm.items.armor.ArmorBJ;
import com.hbm.items.armor.ArmorDesh;
import com.hbm.items.armor.ArmorDiesel;
import com.hbm.items.armor.ArmorEnvsuit;
import com.hbm.items.armor.ArmorHEV;
import com.hbm.items.armor.ArmorTrenchmaster;
import com.hbm.items.armor.ArmorAJRO;
import com.hbm.items.armor.ArmorBismuth;
import com.hbm.items.armor.ArmorDigamma;
import com.hbm.items.armor.ArmorEuphemium;
import com.hbm.items.armor.ArmorFSB;
import com.hbm.items.armor.ArmorHazmat;
import com.hbm.items.armor.ArmorHazmatMask;
import com.hbm.items.armor.ArmorLiquidator;
import com.hbm.items.armor.ArmorLiquidatorMask;
import com.hbm.items.armor.ArmorT51;
import com.hbm.items.armor.ArmorTaurun;
import com.hbm.items.armor.IArmorDisableModel.EnumPlayerPart;
import com.hbm.items.armor.ItemModAuto;
import com.hbm.items.armor.ItemModBandaid;
import com.hbm.items.armor.ItemModBathwater;
import com.hbm.items.armor.ItemModBattery;
import com.hbm.items.armor.ItemModCard;
import com.hbm.items.armor.ItemModCharm;
import com.hbm.items.armor.ItemModCladding;
import com.hbm.items.armor.ItemModDefuser;
import com.hbm.items.armor.ItemModFlippers;
import com.hbm.items.armor.ItemModHealth;
import com.hbm.items.armor.ItemModInk;
import com.hbm.items.armor.ItemModInsert;
import com.hbm.items.armor.ItemModIron;
import com.hbm.items.armor.ItemModLodestone;
import com.hbm.items.armor.ItemModMedal;
import com.hbm.items.armor.ItemModMilk;
import com.hbm.items.armor.ItemModMorningGlory;
import com.hbm.items.armor.ItemModNightVision;
import com.hbm.items.armor.ItemModObsidian;
import com.hbm.items.armor.ItemModPads;
import com.hbm.items.armor.ItemModPolish;
import com.hbm.items.armor.ItemModQuartz;
import com.hbm.items.armor.ItemModRevive;
import com.hbm.items.armor.ItemModSerum;
import com.hbm.items.armor.ItemModServos;
import com.hbm.items.armor.ItemModShackles;
import com.hbm.items.armor.ItemModShield;
import com.hbm.items.armor.ItemModTwoKick;
import com.hbm.items.armor.ItemModV1;
import com.hbm.items.armor.ItemModWD40;
import com.hbm.items.armor.MaskOfInfamy;
import com.hbm.items.armor.ModArmor;
import com.hbm.main.MainRegistry;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.util.ArmorUtil;

import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.EnumHelper;

/**
 * The armor subdomain registration unit.
 *
 * Two waves here: one independent of the render layer (rags/outlaw mask/yewmimor set, plus the set-detection family
 * hazmat/liquidator/paa), the other wanting the 3D helmet model (M65) and held looks (`IItemRendererProvider`),
 * which waits for the render layer. This wave's set detection, filters, stats and stacking are complete — only the M65 hat's look is missing.
 */
public class ItemsArmor implements IRegistrar {

	private final List<Item> created = new ArrayList<>();

	@Override
	public String id() {
		return "items.armor";
	}

	@Override
	public void preInit() {
		created.add(ModItems.mask_of_infamy = HbmRegistry.item(new MaskOfInfamy(ArmorMaterial.IRON, EntityEquipmentSlot.HEAD).setUnlocalizedName("mask_of_infamy").setMaxStackSize(1), "mask_of_infamy"));
		// Ash goggles: vanilla iron material + goggles model; the grey zone's two spots check this one for "worn or not"
		created.add(ModItems.ashglasses = HbmRegistry.item(new ArmorAshGlasses(ArmorMaterial.IRON, EntityEquipmentSlot.HEAD).setUnlocalizedName("ashglasses").setMaxStackSize(1), "ashglasses"));

		// The rags set has no ready-made material, so one is built on the spot: 1.12.2's addArmorMaterial takes three more
		// arguments than before (texture name, equip sound, toughness; the texture name is a label only, these items override getArmorTexture themselves)
		ArmorMaterial rags = EnumHelper.addArmorMaterial("HBM_RAGS", "hbm:rags", 150, new int[] { 1, 1, 1, 1 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0F);

		created.add(ModItems.mask_rag = HbmRegistry.item(new ModArmor(rags, 0).setUnlocalizedName("mask_rag").setMaxStackSize(1), "mask_rag"));
		created.add(ModItems.mask_piss = HbmRegistry.item(new ModArmor(rags, 0).setUnlocalizedName("mask_piss").setMaxStackSize(1), "mask_piss"));

		created.add(ModItems.jackt = HbmRegistry.item(new ModArmor(MainRegistry.aMatSteel, 1).setUnlocalizedName("jackt"), "jackt"));
		created.add(ModItems.jackt2 = HbmRegistry.item(new ModArmor(MainRegistry.aMatSteel, 1).setUnlocalizedName("jackt2"), "jackt2"));

		created.add(ModItems.euphemium_helmet = HbmRegistry.item(new ArmorEuphemium(MainRegistry.aMatEuph, 0).setUnlocalizedName("euphemium_helmet").setMaxStackSize(1), "euphemium_helmet"));
		created.add(ModItems.euphemium_plate = HbmRegistry.item(new ArmorEuphemium(MainRegistry.aMatEuph, 1).setUnlocalizedName("euphemium_plate").setMaxStackSize(1), "euphemium_plate"));
		created.add(ModItems.euphemium_legs = HbmRegistry.item(new ArmorEuphemium(MainRegistry.aMatEuph, 2).setUnlocalizedName("euphemium_legs").setMaxStackSize(1), "euphemium_legs"));
		created.add(ModItems.euphemium_boots = HbmRegistry.item(new ArmorEuphemium(MainRegistry.aMatEuph, 3).setUnlocalizedName("euphemium_boots").setMaxStackSize(1), "euphemium_boots"));

		// Anvil repair materials hang off the material (the old customCraftingMaterial assignments)
		MainRegistry.aMatHaz.repairMaterial = new ItemStack(ModItems.hazmat_cloth);
		MainRegistry.aMatHaz2.repairMaterial = new ItemStack(ModItems.hazmat_cloth_red);
		MainRegistry.aMatHaz3.repairMaterial = new ItemStack(ModItems.hazmat_cloth_grey);
		MainRegistry.aMatPaa.repairMaterial = new ItemStack(ModItems.plate_paa);

		// The three hazmat colors: one white piece comes with a hood (filter socket included); the other three are ordinary hazmat armor
		created.add(ModItems.hazmat_helmet = HbmRegistry.item(new ArmorHazmatMask(MainRegistry.aMatHaz, 0, "hbm:textures/armor/hazmat_1.png").setUnlocalizedName("hazmat_helmet"), "hazmat_helmet"));
		created.add(ModItems.hazmat_plate = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz, 1, "hbm:textures/armor/hazmat_1.png").setUnlocalizedName("hazmat_plate"), "hazmat_plate"));
		created.add(ModItems.hazmat_legs = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz, 2, "hbm:textures/armor/hazmat_2.png").setUnlocalizedName("hazmat_legs"), "hazmat_legs"));
		created.add(ModItems.hazmat_boots = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz, 3, "hbm:textures/armor/hazmat_1.png").setUnlocalizedName("hazmat_boots"), "hazmat_boots"));

		created.add(ModItems.hazmat_helmet_red = HbmRegistry.item(new ArmorHazmatMask(MainRegistry.aMatHaz2, 0, "hbm:textures/models/ModelHazRed.png").setUnlocalizedName("hazmat_helmet_red"), "hazmat_helmet_red"));
		created.add(ModItems.hazmat_plate_red = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz2, 1, "hbm:textures/armor/hazmat_1_red.png").setUnlocalizedName("hazmat_plate_red"), "hazmat_plate_red"));
		created.add(ModItems.hazmat_legs_red = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz2, 2, "hbm:textures/armor/hazmat_2_red.png").setUnlocalizedName("hazmat_legs_red"), "hazmat_legs_red"));
		created.add(ModItems.hazmat_boots_red = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz2, 3, "hbm:textures/armor/hazmat_1_red.png").setUnlocalizedName("hazmat_boots_red"), "hazmat_boots_red"));

		// The grey and PAA helmets carry the set marker; the other three copy it over via cloneStats
		created.add(ModItems.hazmat_helmet_grey = HbmRegistry.item(new ArmorHazmatMask(MainRegistry.aMatHaz3, 0, "hbm:textures/models/ModelHazGrey.png")
				.setUnlocalizedName("hazmat_helmet_grey"), "hazmat_helmet_grey"));
		created.add(ModItems.hazmat_plate_grey = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz3, 1, "hbm:textures/armor/hazmat_1_grey.png").cloneStats((ArmorFSB) ModItems.hazmat_helmet_grey).setUnlocalizedName("hazmat_plate_grey"), "hazmat_plate_grey"));
		created.add(ModItems.hazmat_legs_grey = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz3, 2, "hbm:textures/armor/hazmat_2_grey.png").cloneStats((ArmorFSB) ModItems.hazmat_helmet_grey).setUnlocalizedName("hazmat_legs_grey"), "hazmat_legs_grey"));
		created.add(ModItems.hazmat_boots_grey = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatHaz3, 3, "hbm:textures/armor/hazmat_1_grey.png").cloneStats((ArmorFSB) ModItems.hazmat_helmet_grey).setUnlocalizedName("hazmat_boots_grey"), "hazmat_boots_grey"));

		created.add(ModItems.hazmat_paa_helmet = HbmRegistry.item(new ArmorHazmatMask(MainRegistry.aMatPaa, 0, "hbm:textures/armor/hazmat_paa_1.png")
				.setUnlocalizedName("hazmat_paa_helmet"), "hazmat_paa_helmet"));
		created.add(ModItems.hazmat_paa_plate = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatPaa, 1, "hbm:textures/armor/hazmat_paa_1.png").cloneStats((ArmorFSB) ModItems.hazmat_paa_helmet).setUnlocalizedName("hazmat_paa_plate"), "hazmat_paa_plate"));
		created.add(ModItems.hazmat_paa_legs = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatPaa, 2, "hbm:textures/armor/hazmat_paa_2.png").cloneStats((ArmorFSB) ModItems.hazmat_paa_helmet).setUnlocalizedName("hazmat_paa_legs"), "hazmat_paa_legs"));
		created.add(ModItems.hazmat_paa_boots = HbmRegistry.item(new ArmorHazmat(MainRegistry.aMatPaa, 3, "hbm:textures/armor/hazmat_paa_1.png").cloneStats((ArmorFSB) ModItems.hazmat_paa_helmet).setUnlocalizedName("hazmat_paa_boots"), "hazmat_paa_boots"));

		// The liquidator's material was built on the spot in the old version; done the same here
		ArmorMaterial aMatLiquidator = EnumHelper.addArmorMaterial("HBM_LIQUIDATOR", "liquidator", 750, new int[] { 3, 8, 6, 3 }, 10,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatLiquidator.repairMaterial = new ItemStack(ModItems.plate_lead);

		created.add(ModItems.liquidator_helmet = HbmRegistry.item(new ArmorLiquidatorMask(aMatLiquidator, 0, "hbm:textures/armor/liquidator_helmet.png")
				.setStep("hbm:step.metal")
				.setJump("hbm:step.iron_jump")
				.setFall("hbm:step.iron_land").setUnlocalizedName("liquidator_helmet").setMaxStackSize(1), "liquidator_helmet"));
		created.add(ModItems.liquidator_plate = HbmRegistry.item(new ArmorLiquidator(aMatLiquidator, 1, "hbm:textures/armor/liquidator_1.png").cloneStats((ArmorFSB) ModItems.liquidator_helmet).setUnlocalizedName("liquidator_plate").setMaxStackSize(1), "liquidator_plate"));
		created.add(ModItems.liquidator_legs = HbmRegistry.item(new ArmorLiquidator(aMatLiquidator, 2, "hbm:textures/armor/liquidator_2.png").cloneStats((ArmorFSB) ModItems.liquidator_helmet).setUnlocalizedName("liquidator_legs").setMaxStackSize(1), "liquidator_legs"));
		created.add(ModItems.liquidator_boots = HbmRegistry.item(new ArmorLiquidator(aMatLiquidator, 3, "hbm:textures/armor/liquidator_1.png").cloneStats((ArmorFSB) ModItems.liquidator_helmet).setUnlocalizedName("liquidator_boots").setMaxStackSize(1), "liquidator_boots"));

		// The rest of this big wave are "bare ArmorFSB": set detection and potion buffs only, no 3D looks,
		// the helmet carrying the full buffs and the other three copying via cloneStats (the old layout exactly).
		created.add(ModItems.schrabidium_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSchrab, 0, "hbm:textures/armor/schrabidium_1.png")
				.addEffect(new PotionEffect(MobEffects.HASTE, 20, 2))
				.addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 2))
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 1))
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 2))
				.setUnlocalizedName("schrabidium_helmet"), "schrabidium_helmet"));
		created.add(ModItems.schrabidium_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSchrab, 1, "hbm:textures/armor/schrabidium_1.png").cloneStats((ArmorFSB) ModItems.schrabidium_helmet).setUnlocalizedName("schrabidium_plate"), "schrabidium_plate"));
		created.add(ModItems.schrabidium_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSchrab, 2, "hbm:textures/armor/schrabidium_2.png").cloneStats((ArmorFSB) ModItems.schrabidium_helmet).setUnlocalizedName("schrabidium_legs"), "schrabidium_legs"));
		created.add(ModItems.schrabidium_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSchrab, 3, "hbm:textures/armor/schrabidium_1.png").cloneStats((ArmorFSB) ModItems.schrabidium_helmet).setUnlocalizedName("schrabidium_boots"), "schrabidium_boots"));

		created.add(ModItems.titanium_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatTitan, 0, "hbm:textures/armor/titanium_1.png").setUnlocalizedName("titanium_helmet"), "titanium_helmet"));
		created.add(ModItems.titanium_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatTitan, 1, "hbm:textures/armor/titanium_1.png").cloneStats((ArmorFSB) ModItems.titanium_helmet).setUnlocalizedName("titanium_plate"), "titanium_plate"));
		created.add(ModItems.titanium_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatTitan, 2, "hbm:textures/armor/titanium_2.png").cloneStats((ArmorFSB) ModItems.titanium_helmet).setUnlocalizedName("titanium_legs"), "titanium_legs"));
		created.add(ModItems.titanium_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatTitan, 3, "hbm:textures/armor/titanium_1.png").cloneStats((ArmorFSB) ModItems.titanium_helmet).setUnlocalizedName("titanium_boots"), "titanium_boots"));

		created.add(ModItems.steel_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSteel, 0, "hbm:textures/armor/steel_1.png").setUnlocalizedName("steel_helmet"), "steel_helmet"));
		created.add(ModItems.steel_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSteel, 1, "hbm:textures/armor/steel_1.png").cloneStats((ArmorFSB) ModItems.steel_helmet).setUnlocalizedName("steel_plate"), "steel_plate"));
		created.add(ModItems.steel_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSteel, 2, "hbm:textures/armor/steel_2.png").cloneStats((ArmorFSB) ModItems.steel_helmet).setUnlocalizedName("steel_legs"), "steel_legs"));
		created.add(ModItems.steel_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSteel, 3, "hbm:textures/armor/steel_1.png").cloneStats((ArmorFSB) ModItems.steel_helmet).setUnlocalizedName("steel_boots"), "steel_boots"));

		created.add(ModItems.alloy_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAlloy, 0, "hbm:textures/armor/alloy_1.png").setUnlocalizedName("alloy_helmet"), "alloy_helmet"));
		created.add(ModItems.alloy_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAlloy, 1, "hbm:textures/armor/alloy_1.png").cloneStats((ArmorFSB) ModItems.alloy_helmet).setUnlocalizedName("alloy_plate"), "alloy_plate"));
		created.add(ModItems.alloy_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAlloy, 2, "hbm:textures/armor/alloy_2.png").cloneStats((ArmorFSB) ModItems.alloy_helmet).setUnlocalizedName("alloy_legs"), "alloy_legs"));
		created.add(ModItems.alloy_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAlloy, 3, "hbm:textures/armor/alloy_1.png").cloneStats((ArmorFSB) ModItems.alloy_helmet).setUnlocalizedName("alloy_boots"), "alloy_boots"));

		created.add(ModItems.cmb_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCMB, 0, "hbm:textures/armor/cmb_1.png")
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 2))
				.addEffect(new PotionEffect(MobEffects.HASTE, 20, 2))
				.addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 4))
				.setUnlocalizedName("cmb_helmet"), "cmb_helmet"));
		created.add(ModItems.cmb_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCMB, 1, "hbm:textures/armor/cmb_1.png").cloneStats((ArmorFSB) ModItems.cmb_helmet).setUnlocalizedName("cmb_plate"), "cmb_plate"));
		created.add(ModItems.cmb_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCMB, 2, "hbm:textures/armor/cmb_2.png").cloneStats((ArmorFSB) ModItems.cmb_helmet).setUnlocalizedName("cmb_legs"), "cmb_legs"));
		created.add(ModItems.cmb_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCMB, 3, "hbm:textures/armor/cmb_1.png").cloneStats((ArmorFSB) ModItems.cmb_helmet).setUnlocalizedName("cmb_boots"), "cmb_boots"));

		// The PAA set has no helmet piece; the head slot leans on hazmat_paa_helmet; these three form a set on their own
		created.add(ModItems.paa_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatPaa, 1, "hbm:textures/armor/paa_1.png").setNoHelmet(true)
				.addEffect(new PotionEffect(MobEffects.HASTE, 20, 0)).setUnlocalizedName("paa_plate"), "paa_plate"));
		created.add(ModItems.paa_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatPaa, 2, "hbm:textures/armor/paa_2.png").cloneStats((ArmorFSB) ModItems.paa_plate).setUnlocalizedName("paa_legs"), "paa_legs"));
		created.add(ModItems.paa_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatPaa, 3, "hbm:textures/armor/paa_1.png").cloneStats((ArmorFSB) ModItems.paa_plate).setUnlocalizedName("paa_boots"), "paa_boots"));

		created.add(ModItems.asbestos_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAsbestos, 0, "hbm:textures/armor/asbestos_1.png").setOverlay("hbm:textures/misc/overlay_asbestos.png").setUnlocalizedName("asbestos_helmet"), "asbestos_helmet"));
		created.add(ModItems.asbestos_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAsbestos, 1, "hbm:textures/armor/asbestos_1.png").setUnlocalizedName("asbestos_plate"), "asbestos_plate"));
		created.add(ModItems.asbestos_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAsbestos, 2, "hbm:textures/armor/asbestos_2.png").setUnlocalizedName("asbestos_legs"), "asbestos_legs"));
		created.add(ModItems.asbestos_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatAsbestos, 3, "hbm:textures/armor/asbestos_1.png").setUnlocalizedName("asbestos_boots"), "asbestos_boots"));

		created.add(ModItems.security_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSecurity, 0, "hbm:textures/armor/security_1.png").setUnlocalizedName("security_helmet"), "security_helmet"));
		created.add(ModItems.security_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSecurity, 1, "hbm:textures/armor/security_1.png").cloneStats((ArmorFSB) ModItems.security_helmet).setUnlocalizedName("security_plate"), "security_plate"));
		created.add(ModItems.security_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSecurity, 2, "hbm:textures/armor/security_2.png").cloneStats((ArmorFSB) ModItems.security_helmet).setUnlocalizedName("security_legs"), "security_legs"));
		created.add(ModItems.security_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatSecurity, 3, "hbm:textures/armor/security_1.png").cloneStats((ArmorFSB) ModItems.security_helmet).setUnlocalizedName("security_boots"), "security_boots"));

		created.add(ModItems.cobalt_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCobalt, 0, "hbm:textures/armor/cobalt_1.png").setUnlocalizedName("cobalt_helmet"), "cobalt_helmet"));
		created.add(ModItems.cobalt_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCobalt, 1, "hbm:textures/armor/cobalt_1.png").cloneStats((ArmorFSB) ModItems.cobalt_helmet).setUnlocalizedName("cobalt_plate"), "cobalt_plate"));
		created.add(ModItems.cobalt_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCobalt, 2, "hbm:textures/armor/cobalt_2.png").cloneStats((ArmorFSB) ModItems.cobalt_helmet).setUnlocalizedName("cobalt_legs"), "cobalt_legs"));
		created.add(ModItems.cobalt_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatCobalt, 3, "hbm:textures/armor/cobalt_1.png").cloneStats((ArmorFSB) ModItems.cobalt_helmet).setUnlocalizedName("cobalt_boots"), "cobalt_boots"));

		created.add(ModItems.starmetal_helmet = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatStarmetal, 0, "hbm:textures/armor/starmetal_1.png").setUnlocalizedName("starmetal_helmet"), "starmetal_helmet"));
		created.add(ModItems.starmetal_plate = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatStarmetal, 1, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.starmetal_helmet).setUnlocalizedName("starmetal_plate"), "starmetal_plate"));
		created.add(ModItems.starmetal_legs = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatStarmetal, 2, "hbm:textures/armor/starmetal_2.png").cloneStats((ArmorFSB) ModItems.starmetal_helmet).setUnlocalizedName("starmetal_legs"), "starmetal_legs"));
		created.add(ModItems.starmetal_boots = HbmRegistry.item(new ArmorFSB(MainRegistry.aMatStarmetal, 3, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.starmetal_helmet).setUnlocalizedName("starmetal_boots"), "starmetal_boots"));

		created.add(ModItems.robes_helmet = HbmRegistry.item(new ArmorFSB(ArmorMaterial.CHAIN, 0, "hbm:textures/armor/robes_1.png").setUnlocalizedName("robes_helmet"), "robes_helmet"));
		created.add(ModItems.robes_plate = HbmRegistry.item(new ArmorFSB(ArmorMaterial.CHAIN, 1, "hbm:textures/armor/robes_1.png").cloneStats((ArmorFSB) ModItems.robes_helmet).setUnlocalizedName("robes_plate"), "robes_plate"));
		created.add(ModItems.robes_legs = HbmRegistry.item(new ArmorFSB(ArmorMaterial.CHAIN, 2, "hbm:textures/armor/robes_2.png").cloneStats((ArmorFSB) ModItems.robes_helmet).setUnlocalizedName("robes_legs"), "robes_legs"));
		created.add(ModItems.robes_boots = HbmRegistry.item(new ArmorFSB(ArmorMaterial.CHAIN, 3, "hbm:textures/armor/robes_1.png").cloneStats((ArmorFSB) ModItems.robes_helmet).setUnlocalizedName("robes_boots"), "robes_boots"));

		// Both materials were built on the spot in the old version; same here; repair materials hang directly on items
		ArmorMaterial aMatZirconium = EnumHelper.addArmorMaterial("HBM_ZIRCONIUM", "zirconium", 1000, new int[] { 2, 5, 3, 1 }, 1000,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatZirconium.repairMaterial = new ItemStack(ModItems.ingot_zirconium);
		created.add(ModItems.zirconium_legs = HbmRegistry.item(new ArmorFSB(aMatZirconium, 2, "hbm:textures/armor/zirconium_2.png").setUnlocalizedName("zirconium_legs"), "zirconium_legs"));

		ArmorMaterial aMatDNT = EnumHelper.addArmorMaterial("HBM_DNT_LOLOLOL", "dnt", 3, new int[] { 1, 1, 1, 1 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatDNT.repairMaterial = new ItemStack(ModItems.ingot_dineutronium);
		created.add(ModItems.dnt_helmet = HbmRegistry.item(new ArmorFSB(aMatDNT, 0, "hbm:textures/armor/dnt_1.png").setUnlocalizedName("dnt_helmet"), "dnt_helmet"));
		created.add(ModItems.dnt_plate = HbmRegistry.item(new ArmorFSB(aMatDNT, 1, "hbm:textures/armor/dnt_1.png").cloneStats((ArmorFSB) ModItems.dnt_helmet).setUnlocalizedName("dnt_plate"), "dnt_plate"));
		created.add(ModItems.dnt_legs = HbmRegistry.item(new ArmorFSB(aMatDNT, 2, "hbm:textures/armor/dnt_2.png").cloneStats((ArmorFSB) ModItems.dnt_helmet).setUnlocalizedName("dnt_legs"), "dnt_legs"));
		created.add(ModItems.dnt_boots = HbmRegistry.item(new ArmorFSB(aMatDNT, 3, "hbm:textures/armor/dnt_1.png").cloneStats((ArmorFSB) ModItems.dnt_helmet).setUnlocalizedName("dnt_boots"), "dnt_boots"));

		// The bismuth armor: its one difference is the obj-parts model when worn (hooked in ArmorBismuth.getArmorModel)
		ArmorMaterial aMatBismuth = EnumHelper.addArmorMaterial("HBM_BISMUTH", "hbm:bismuth", 100, new int[] { 3, 8, 6, 3 }, 100,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatBismuth.repairMaterial = new ItemStack(ModItems.plate_bismuth);
		created.add(ModItems.bismuth_helmet = HbmRegistry.item(new ArmorBismuth(aMatBismuth, 0, "hbm:textures/armor/starmetal_1.png")
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 6))
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 6))
				.addEffect(new PotionEffect(MobEffects.REGENERATION, 20, 1))
				.addEffect(new PotionEffect(MobEffects.NIGHT_VISION, 15 * 20, 0))
				.setDashCount(3)
				.setUnlocalizedName("bismuth_helmet"), "bismuth_helmet"));
		created.add(ModItems.bismuth_plate = HbmRegistry.item(new ArmorBismuth(aMatBismuth, 1, "hbm:textures/armor/starmetal_2.png").cloneStats((ArmorFSB) ModItems.bismuth_helmet).setUnlocalizedName("bismuth_plate"), "bismuth_plate"));
		created.add(ModItems.bismuth_legs = HbmRegistry.item(new ArmorBismuth(aMatBismuth, 2, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.bismuth_helmet).setUnlocalizedName("bismuth_legs"), "bismuth_legs"));
		created.add(ModItems.bismuth_boots = HbmRegistry.item(new ArmorBismuth(aMatBismuth, 3, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.bismuth_helmet).setUnlocalizedName("bismuth_boots"), "bismuth_boots"));

		// The five families below are "obj-parts-model armors": differing from the batch above only in which model set they wear; values and buffs ported entry by entry.
		// Held looks are another matter — the old ones were bound to the deleted IItemRenderer; wiring them now means the render/item TESR door.
		ArmorMaterial aMatT51 = EnumHelper.addArmorMaterial("HBM_T51", "hbm:t51", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatT51.repairMaterial = new ItemStack(ModItems.plate_armor_titanium);
		created.add(ModItems.t51_helmet = HbmRegistry.item(new ArmorT51(aMatT51, 0, "hbm:textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 0))
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_NO_LIGHT).setRadResist(1D)
				.setMaxStackSize(1).setUnlocalizedName("t51_helmet"), "t51_helmet"));
		created.add(ModItems.t51_plate = HbmRegistry.item(new ArmorT51(aMatT51, 1, "hbm:textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) ModItems.t51_helmet).setMaxStackSize(1).setUnlocalizedName("t51_plate"), "t51_plate"));
		created.add(ModItems.t51_legs = HbmRegistry.item(new ArmorT51(aMatT51, 2, "hbm:textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) ModItems.t51_helmet).setMaxStackSize(1).setUnlocalizedName("t51_legs"), "t51_legs"));
		created.add(ModItems.t51_boots = HbmRegistry.item(new ArmorT51(aMatT51, 3, "hbm:textures/armor/starmetal_1.png", 1000000, 10000, 1000, 5).cloneStats((ArmorFSB) ModItems.t51_helmet).setMaxStackSize(1).setUnlocalizedName("t51_boots"), "t51_boots"));

		// AJR and AJRO differ only in looks and textures; the four power tiers share values, so both tables reference the same material
		ArmorMaterial aMatAJR = EnumHelper.addArmorMaterial("HBM_T45AJR", "hbm:ajr", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatAJR.repairMaterial = new ItemStack(ModItems.plate_armor_ajr);
		created.add(ModItems.ajr_helmet = HbmRegistry.item(new ArmorAJR(aMatAJR, 0, "hbm:textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 0))
				.addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 0))
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.3D)
				.setUnlocalizedName("ajr_helmet"), "ajr_helmet"));
		created.add(ModItems.ajr_plate = HbmRegistry.item(new ArmorAJR(aMatAJR, 1, "hbm:textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ModItems.ajr_helmet).setUnlocalizedName("ajr_plate"), "ajr_plate"));
		created.add(ModItems.ajr_legs = HbmRegistry.item(new ArmorAJR(aMatAJR, 2, "hbm:textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ModItems.ajr_helmet).setUnlocalizedName("ajr_legs"), "ajr_legs"));
		created.add(ModItems.ajr_boots = HbmRegistry.item(new ArmorAJR(aMatAJR, 3, "hbm:textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ModItems.ajr_helmet).setUnlocalizedName("ajr_boots"), "ajr_boots"));

		created.add(ModItems.ajro_helmet = HbmRegistry.item(new ArmorAJRO(aMatAJR, 0, "hbm:textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25)
				.enableVATS(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 0))
				.addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 0))
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.3D)
				.setUnlocalizedName("ajro_helmet"), "ajro_helmet"));
		created.add(ModItems.ajro_plate = HbmRegistry.item(new ArmorAJRO(aMatAJR, 1, "hbm:textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ModItems.ajro_helmet).setUnlocalizedName("ajro_plate"), "ajro_plate"));
		created.add(ModItems.ajro_legs = HbmRegistry.item(new ArmorAJRO(aMatAJR, 2, "hbm:textures/armor/starmetal_2.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ModItems.ajro_helmet).setUnlocalizedName("ajro_legs"), "ajro_legs"));
		created.add(ModItems.ajro_boots = HbmRegistry.item(new ArmorAJRO(aMatAJR, 3, "hbm:textures/armor/starmetal_1.png", 2500000, 10000, 2000, 25).cloneStats((ArmorFSB) ModItems.ajro_helmet).setUnlocalizedName("ajro_boots"), "ajro_boots"));

		// The fau armor (called fau in game): thermal vision and geiger both on; the radiation resistance table folds to 99.99% for the full set
		ArmorMaterial aMatFau = EnumHelper.addArmorMaterial("HBM_DIGAMMA", "hbm:digamma", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatFau.repairMaterial = new ItemStack(ModItems.plate_armor_fau);
		created.add(ModItems.fau_helmet = HbmRegistry.item(new ArmorDigamma(aMatFau, 0, "hbm:textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0)
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 1))
				.setHasGeigerSound(true)
				.enableThermalSight(true)
				.setHasHardLanding(true)
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(4D)
				.setUnlocalizedName("fau_helmet"), "fau_helmet"));
		created.add(ModItems.fau_plate = HbmRegistry.item(new ArmorDigamma(aMatFau, 1, "hbm:textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) ModItems.fau_helmet).setFullSetForHide().setUnlocalizedName("fau_plate"), "fau_plate"));
		created.add(ModItems.fau_legs = HbmRegistry.item(new ArmorDigamma(aMatFau, 2, "hbm:textures/armor/starmetal_2.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) ModItems.fau_helmet).hides(EnumPlayerPart.LEFT_LEG, EnumPlayerPart.RIGHT_LEG).setFullSetForHide().setUnlocalizedName("fau_legs"), "fau_legs"));
		created.add(ModItems.fau_boots = HbmRegistry.item(new ArmorDigamma(aMatFau, 3, "hbm:textures/armor/starmetal_1.png", 10000000, 10000, 2500, 0).cloneStats((ArmorFSB) ModItems.fau_helmet).setFullSetForHide().setUnlocalizedName("fau_boots"), "fau_boots"));

		// The taurun set carries no battery; its material enchantability was 10 in the old version, kept here
		ArmorMaterial aMatTaurun = EnumHelper.addArmorMaterial("HBM_TAURUN", "hbm:taurun", 150, new int[] { 3, 8, 6, 3 }, 10,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatTaurun.repairMaterial = new ItemStack(ModItems.plate_iron);
		created.add(ModItems.taurun_helmet = HbmRegistry.item(new ArmorTaurun(aMatTaurun, 0, "hbm:textures/armor/starmetal_1.png")
				.addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 0))
				.setStepSize(1)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(0.125D)
				.setUnlocalizedName("taurun_helmet"), "taurun_helmet"));
		created.add(ModItems.taurun_plate = HbmRegistry.item(new ArmorTaurun(aMatTaurun, 1, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.taurun_helmet).setUnlocalizedName("taurun_plate"), "taurun_plate"));
		created.add(ModItems.taurun_legs = HbmRegistry.item(new ArmorTaurun(aMatTaurun, 2, "hbm:textures/armor/starmetal_2.png").cloneStats((ArmorFSB) ModItems.taurun_helmet).setUnlocalizedName("taurun_legs"), "taurun_legs"));
		created.add(ModItems.taurun_boots = HbmRegistry.item(new ArmorTaurun(aMatTaurun, 3, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.taurun_helmet).setUnlocalizedName("taurun_boots"), "taurun_boots"));

		// Mods: one piece = one slot toggle + one slot number, all in constructor args; the numeric differences (battery multiplier, plate HP, revives) belong to only these few
		created.add(ModItems.injector_5htp = HbmRegistry.item(new ItemModAuto().setUnlocalizedName("injector_5htp"), "injector_5htp"));
		created.add(ModItems.bandaid = HbmRegistry.item(new ItemModBandaid().setUnlocalizedName("bandaid"), "bandaid"));
		created.add(ModItems.bathwater = HbmRegistry.item(new ItemModBathwater().setUnlocalizedName("bathwater"), "bathwater"));
		created.add(ModItems.bathwater_mk2 = HbmRegistry.item(new ItemModBathwater().setUnlocalizedName("bathwater_mk2"), "bathwater_mk2"));
		created.add(ModItems.armor_battery = HbmRegistry.item(new ItemModBattery(1.25D).setUnlocalizedName("armor_battery"), "armor_battery"));
		created.add(ModItems.armor_battery_mk2 = HbmRegistry.item(new ItemModBattery(1.5D).setUnlocalizedName("armor_battery_mk2"), "armor_battery_mk2"));
		created.add(ModItems.armor_battery_mk3 = HbmRegistry.item(new ItemModBattery(2D).setUnlocalizedName("armor_battery_mk3"), "armor_battery_mk3"));
		created.add(ModItems.protection_charm = HbmRegistry.item(new ItemModCharm().setUnlocalizedName("protection_charm"), "protection_charm"));
		created.add(ModItems.meteor_charm = HbmRegistry.item(new ItemModCharm().setUnlocalizedName("meteor_charm"), "meteor_charm"));
		created.add(ModItems.defuser_gold = HbmRegistry.item(new ItemModDefuser().setUnlocalizedName("defuser_gold"), "defuser_gold"));
		created.add(ModItems.heart_piece = HbmRegistry.item(new ItemModHealth(5F).setUnlocalizedName("heart_piece"), "heart_piece"));
		created.add(ModItems.heart_container = HbmRegistry.item(new ItemModHealth(20F).setUnlocalizedName("heart_container"), "heart_container"));
		created.add(ModItems.heart_booster = HbmRegistry.item(new ItemModHealth(40F).setUnlocalizedName("heart_booster"), "heart_booster"));
		created.add(ModItems.heart_fab = HbmRegistry.item(new ItemModHealth(60F).setUnlocalizedName("heart_fab"), "heart_fab"));
		created.add(ModItems.black_diamond = HbmRegistry.item(new ItemModHealth(40F).setUnlocalizedName("black_diamond"), "black_diamond"));
		created.add(ModItems.ink = HbmRegistry.item(new ItemModInk().setUnlocalizedName("ink"), "ink"));
		created.add(ModItems.cladding_iron = HbmRegistry.item(new ItemModIron().setUnlocalizedName("cladding_iron"), "cladding_iron"));
		created.add(ModItems.medal_liquidator = HbmRegistry.item(new ItemModMedal().setUnlocalizedName("medal_liquidator"), "medal_liquidator"));
		created.add(ModItems.morning_glory = HbmRegistry.item(new ItemModMorningGlory().setUnlocalizedName("morning_glory"), "morning_glory"));
		created.add(ModItems.cladding_obsidian = HbmRegistry.item(new ItemModObsidian().setUnlocalizedName("cladding_obsidian"), "cladding_obsidian"));
		created.add(ModItems.armor_polish = HbmRegistry.item(new ItemModPolish().setUnlocalizedName("armor_polish"), "armor_polish"));
		created.add(ModItems.quartz_plutonium = HbmRegistry.item(new ItemModQuartz().setUnlocalizedName("quartz_plutonium"), "quartz_plutonium"));
		created.add(ModItems.scrumpy = HbmRegistry.item(new ItemModRevive(1).setUnlocalizedName("scrumpy"), "scrumpy"));
		created.add(ModItems.wild_p = HbmRegistry.item(new ItemModRevive(3).setUnlocalizedName("wild_p"), "wild_p"));
		created.add(ModItems.serum = HbmRegistry.item(new ItemModSerum().setUnlocalizedName("serum"), "serum"));
		created.add(ModItems.servo_set = HbmRegistry.item(new ItemModServos().setUnlocalizedName("servo_set"), "servo_set"));
		created.add(ModItems.servo_set_desh = HbmRegistry.item(new ItemModServos().setUnlocalizedName("servo_set_desh"), "servo_set_desh"));
		created.add(ModItems.shackles = HbmRegistry.item(new ItemModShackles().setUnlocalizedName("shackles"), "shackles"));
		created.add(ModItems.australium_iii = HbmRegistry.item(new ItemModShield(25F).setUnlocalizedName("australium_iii"), "australium_iii"));
		created.add(ModItems.ballistic_gauntlet = HbmRegistry.item(new ItemModTwoKick().setUnlocalizedName("ballistic_gauntlet"), "ballistic_gauntlet"));
		created.add(ModItems.v1 = HbmRegistry.item(new ItemModV1().setUnlocalizedName("v1"), "v1"));
		created.add(ModItems.wd40 = HbmRegistry.item(new ItemModWD40().setUnlocalizedName("wd40"), "wd40"));
		// Three landing pads, the parameter being the fall-damage retention ratio; the static one also recharges battery-carrying armor
		created.add(ModItems.pads_rubber = HbmRegistry.item(new ItemModPads(0.5F).setUnlocalizedName("pads_rubber"), "pads_rubber"));
		created.add(ModItems.pads_slime = HbmRegistry.item(new ItemModPads(0.25F).setUnlocalizedName("pads_slime"), "pads_slime"));
		created.add(ModItems.pads_static = HbmRegistry.item(new ItemModPads(0.75F).setUnlocalizedName("pads_static"), "pads_static"));

		// Five coatings: the constructor arg is the radiation resistance increment, values running low to high, no behavioral branches
		created.add(ModItems.cladding_paint = HbmRegistry.item(new ItemModCladding(0.025).setUnlocalizedName("cladding_paint"), "cladding_paint"));
		created.add(ModItems.cladding_rubber = HbmRegistry.item(new ItemModCladding(0.005).setUnlocalizedName("cladding_rubber"), "cladding_rubber"));
		created.add(ModItems.cladding_lead = HbmRegistry.item(new ItemModCladding(0.1).setUnlocalizedName("cladding_lead"), "cladding_lead"));
		created.add(ModItems.cladding_desh = HbmRegistry.item(new ItemModCladding(0.2).setUnlocalizedName("cladding_desh"), "cladding_desh"));
		created.add(ModItems.cladding_ghiorsium = HbmRegistry.item(new ItemModCladding(0.5).setUnlocalizedName("cladding_ghiorsium"), "cladding_ghiorsium"));

		// Three magnets likewise, the parameter being the attraction radius
		created.add(ModItems.lodestone = HbmRegistry.item(new ItemModLodestone(5).setUnlocalizedName("lodestone"), "lodestone"));
		created.add(ModItems.horseshoe_magnet = HbmRegistry.item(new ItemModLodestone(8).setUnlocalizedName("horseshoe_magnet"), "horseshoe_magnet"));
		created.add(ModItems.industrial_magnet = HbmRegistry.item(new ItemModLodestone(12).setUnlocalizedName("industrial_magnet"), "industrial_magnet"));

		created.add(ModItems.spider_milk = HbmRegistry.item(new ItemModMilk().setUnlocalizedName("spider_milk"), "spider_milk"));

		// The night vision module only works with a powered armor set on the body; worn alone it does nothing
		created.add(ModItems.night_vision = HbmRegistry.item(new ItemModNightVision().setUnlocalizedName("night_vision"), "night_vision"));

		// Two red room cards: excluded from the creative tab, loot only; tooltips and immunity branch by item identity
		created.add(ModItems.card_aos = HbmRegistry.item(new ItemModCard().setUnlocalizedName("card_aos"), "card_aos"));
		created.add(ModItems.card_qos = HbmRegistry.item(new ItemModCard().setUnlocalizedName("card_qos"), "card_qos"));

		// Twelve insert plates: durability + four multipliers (total/projectile/explosion/speed) all in constructor args;
		// the polonium one feeds radiation back, the ERA one detonates when blasted, and both carry the anti-corrosion atmosphere marker
		created.add(ModItems.insert_kevlar = HbmRegistry.item(new ItemModInsert(1500, 1F, 0.9F, 1F, 1F).setUnlocalizedName("insert_kevlar"), "insert_kevlar"));
		created.add(ModItems.insert_sapi = HbmRegistry.item(new ItemModInsert(1750, 1F, 0.85F, 1F, 1F).setUnlocalizedName("insert_sapi"), "insert_sapi"));
		created.add(ModItems.insert_esapi = HbmRegistry.item(new ItemModInsert(2000, 0.95F, 0.8F, 1F, 1F).setUnlocalizedName("insert_esapi"), "insert_esapi"));
		created.add(ModItems.insert_xsapi = HbmRegistry.item(new ItemModInsert(2500, 0.9F, 0.75F, 1F, 1F).setUnlocalizedName("insert_xsapi"), "insert_xsapi"));
		created.add(ModItems.insert_steel = HbmRegistry.item(new ItemModInsert(1000, 1F, 0.95F, 0.75F, 0.95F).setUnlocalizedName("insert_steel"), "insert_steel"));
		created.add(ModItems.insert_du = HbmRegistry.item(new ItemModInsert(1500, 0.9F, 0.85F, 0.5F, 0.9F).setUnlocalizedName("insert_du"), "insert_du"));
		created.add(ModItems.insert_polonium = HbmRegistry.item(new ItemModInsert(500, 0.9F, 1F, 0.95F, 0.9F).setUnlocalizedName("insert_polonium"), "insert_polonium"));
		created.add(ModItems.insert_ghiorsium = HbmRegistry.item(new ItemModInsert(2000, 0.8F, 0.75F, 0.35F, 0.9F).setUnlocalizedName("insert_ghiorsium"), "insert_ghiorsium"));
		created.add(ModItems.insert_cmb = HbmRegistry.item(new ItemModInsert(9999, 0.7F, 0.9F, 0.4F, 1F).withCorrosionProtection().setUnlocalizedName("insert_cmb"), "insert_cmb"));
		created.add(ModItems.insert_era = HbmRegistry.item(new ItemModInsert(25, 0.5F, 1F, 0.25F, 1F).setUnlocalizedName("insert_era"), "insert_era"));
		created.add(ModItems.insert_yharonite = HbmRegistry.item(new ItemModInsert(9999, 0.01F, 1F, 1F, 1F).withCorrosionProtection().setUnlocalizedName("insert_yharonite"), "insert_yharonite"));
		created.add(ModItems.insert_doxium = HbmRegistry.item(new ItemModInsert(9999, 5.0F, 1F, 1F, 1F).setUnlocalizedName("insert_doxium"), "insert_doxium"));

		// Flippers: occupy the boots slot, push you toward your look direction in water
		created.add(ModItems.flippers = HbmRegistry.item(new ItemModFlippers().setUnlocalizedName("flippers"), "flippers"));

		// The blackjack set: the old material was addArmorMaterial'd in place inside ModItemsArmor (no aMatBJ in main); built in place here too
		ArmorMaterial aMatBJ = EnumHelper.addArmorMaterial("HBM_BLACKJACK", "hbm:bj", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatBJ.repairMaterial = new ItemStack(ModItems.plate_armor_lunar);
		created.add(ModItems.bj_helmet = HbmRegistry.item(new ArmorBJ(aMatBJ, 0, "hbm:textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100)
				.enableVATS(true)
				.enableThermalSight(true)
				.setHasGeigerSound(true)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 1))
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 0))
				.addEffect(new PotionEffect(MobEffects.SATURATION, 20, 0))
				.addEffect(new PotionEffect(HbmPotion.radx, 20, 0))
				.setStep("hbm:step.metal").setJump("hbm:step.iron_jump").setFall("hbm:step.iron_land")
				.setRadResist(1D)
				.setUnlocalizedName("bj_helmet"), "bj_helmet"));
		created.add(ModItems.bj_plate = HbmRegistry.item(new ArmorBJ(aMatBJ, 1, "hbm:textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) ModItems.bj_helmet).setUnlocalizedName("bj_plate"), "bj_plate"));
		created.add(ModItems.bj_legs = HbmRegistry.item(new ArmorBJ(aMatBJ, 2, "hbm:textures/armor/starmetal_2.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) ModItems.bj_helmet).setUnlocalizedName("bj_legs"), "bj_legs"));
		created.add(ModItems.bj_boots = HbmRegistry.item(new ArmorBJ(aMatBJ, 3, "hbm:textures/armor/starmetal_1.png", 10000000, 10000, 1000, 100).cloneStats((ArmorFSB) ModItems.bj_helmet).setUnlocalizedName("bj_boots"), "bj_boots"));

		// The steam suit (ArmorDesh): a battery-fed armor that drinks steam; the material likewise built in place (as in the old version)
		ArmorMaterial aMatDesh = EnumHelper.addArmorMaterial("HBM_DESH", "hbm:desh", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatDesh.repairMaterial = new ItemStack(ModItems.ingot_desh);
		created.add(ModItems.steamsuit_helmet = HbmRegistry.item(new ArmorDesh(aMatDesh, 0, "hbm:textures/armor/starmetal_1.png", Fluids.STEAM, 64000, 500, 50, 1)
				.setHasHardLanding(true)
				.addEffect(new PotionEffect(MobEffects.HASTE, 20, 4))
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.3D)
				.setMaxStackSize(1)
				.setUnlocalizedName("steamsuit_helmet"), "steamsuit_helmet"));
		created.add(ModItems.steamsuit_plate = HbmRegistry.item(new ArmorDesh(aMatDesh, 1, "hbm:textures/armor/starmetal_1.png", Fluids.STEAM, 64000, 500, 50, 1).cloneStats((ArmorFSB) ModItems.steamsuit_helmet).setMaxStackSize(1).setUnlocalizedName("steamsuit_plate"), "steamsuit_plate"));
		created.add(ModItems.steamsuit_legs = HbmRegistry.item(new ArmorDesh(aMatDesh, 2, "hbm:textures/armor/starmetal_2.png", Fluids.STEAM, 64000, 500, 50, 1).cloneStats((ArmorFSB) ModItems.steamsuit_helmet).setMaxStackSize(1).setUnlocalizedName("steamsuit_legs"), "steamsuit_legs"));
		created.add(ModItems.steamsuit_boots = HbmRegistry.item(new ArmorDesh(aMatDesh, 3, "hbm:textures/armor/starmetal_1.png", Fluids.STEAM, 64000, 500, 50, 1).cloneStats((ArmorFSB) ModItems.steamsuit_helmet).setMaxStackSize(1).setUnlocalizedName("steamsuit_boots"), "steamsuit_boots"));

		// The diesel suit (ArmorDiesel): the same fuel base, burning diesel, knockback-proof
		ArmorMaterial aMatDiesel = EnumHelper.addArmorMaterial("HBM_BNUUY", "hbm:diesel", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatDiesel.repairMaterial = new ItemStack(ModItems.plate_copper);
		created.add(ModItems.dieselsuit_helmet = HbmRegistry.item(new ArmorDiesel(aMatDiesel, 0, "hbm:textures/armor/starmetal_1.png", Fluids.DIESEL, 64000, 500, 50, 1)
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 2))
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 2))
				.enableThermalSight(true)
				.enableVATS(true)
				.setMaxStackSize(1)
				.setUnlocalizedName("dieselsuit_helmet"), "dieselsuit_helmet"));
		created.add(ModItems.dieselsuit_plate = HbmRegistry.item(new ArmorDiesel(aMatDiesel, 1, "hbm:textures/armor/starmetal_1.png", Fluids.DIESEL, 64000, 500, 50, 1).cloneStats((ArmorFSB) ModItems.dieselsuit_helmet).setMaxStackSize(1).setUnlocalizedName("dieselsuit_plate"), "dieselsuit_plate"));
		created.add(ModItems.dieselsuit_legs = HbmRegistry.item(new ArmorDiesel(aMatDiesel, 2, "hbm:textures/armor/starmetal_2.png", Fluids.DIESEL, 64000, 500, 50, 1).cloneStats((ArmorFSB) ModItems.dieselsuit_helmet).setMaxStackSize(1).setUnlocalizedName("dieselsuit_legs"), "dieselsuit_legs"));
		created.add(ModItems.dieselsuit_boots = HbmRegistry.item(new ArmorDiesel(aMatDiesel, 3, "hbm:textures/armor/starmetal_1.png", Fluids.DIESEL, 64000, 500, 50, 1).cloneStats((ArmorFSB) ModItems.dieselsuit_helmet).setMaxStackSize(1).setUnlocalizedName("dieselsuit_boots"), "dieselsuit_boots"));

		// The envsuit (ArmorEnvsuit): mobility rides the chestplate (+10% sprint speed, underwater night vision + thrust)
		ArmorMaterial aMatEnv = EnumHelper.addArmorMaterial("HBM_ENV", "hbm:env", 150, new int[] { 3, 8, 6, 3 }, 10,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatEnv.repairMaterial = new ItemStack(ModItems.plate_armor_hev);
		created.add(ModItems.envsuit_helmet = HbmRegistry.item(new ArmorEnvsuit(aMatEnv, 0, "hbm:textures/armor/starmetal_1.png", 100000, 1000, 250, 0)
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 1))
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 0))
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(1.0D)
				.setUnlocalizedName("envsuit_helmet"), "envsuit_helmet"));
		created.add(ModItems.envsuit_plate = HbmRegistry.item(new ArmorEnvsuit(aMatEnv, 1, "hbm:textures/armor/starmetal_1.png", 100000, 1000, 250, 0).cloneStats((ArmorFSB) ModItems.envsuit_helmet).setUnlocalizedName("envsuit_plate"), "envsuit_plate"));
		created.add(ModItems.envsuit_legs = HbmRegistry.item(new ArmorEnvsuit(aMatEnv, 2, "hbm:textures/armor/starmetal_2.png", 100000, 1000, 250, 0).cloneStats((ArmorFSB) ModItems.envsuit_helmet).setUnlocalizedName("envsuit_legs"), "envsuit_legs"));
		created.add(ModItems.envsuit_boots = HbmRegistry.item(new ArmorEnvsuit(aMatEnv, 3, "hbm:textures/armor/starmetal_1.png", 100000, 1000, 250, 0).cloneStats((ArmorFSB) ModItems.envsuit_helmet).setUnlocalizedName("envsuit_boots"), "envsuit_boots"));

		// The HEV suit (ArmorHEV): the old self-drawn HUD isn't ported yet (the base handleOverlay has no callers in the tree, see the class comment)
		ArmorMaterial aMatHEV = EnumHelper.addArmorMaterial("HBM_HEV", "hbm:hev", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatHEV.repairMaterial = new ItemStack(ModItems.plate_armor_hev);
		created.add(ModItems.hev_helmet = HbmRegistry.item(new ArmorHEV(aMatHEV, 0, "hbm:textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0)
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 1))
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 0))
				.setHasGeigerSound(true)
				.setHasCustomGeiger(true)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setHazardClass(ArmorUtil.FULL_PACKAGE).setRadResist(2.3D)
				.setUnlocalizedName("hev_helmet"), "hev_helmet"));
		created.add(ModItems.hev_plate = HbmRegistry.item(new ArmorHEV(aMatHEV, 1, "hbm:textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) ModItems.hev_helmet).setUnlocalizedName("hev_plate"), "hev_plate"));
		created.add(ModItems.hev_legs = HbmRegistry.item(new ArmorHEV(aMatHEV, 2, "hbm:textures/armor/starmetal_2.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) ModItems.hev_helmet).setUnlocalizedName("hev_legs"), "hev_legs"));
		created.add(ModItems.hev_boots = HbmRegistry.item(new ArmorHEV(aMatHEV, 3, "hbm:textures/armor/starmetal_1.png", 1000000, 10000, 2500, 0).cloneStats((ArmorFSB) ModItems.hev_helmet).setUnlocalizedName("hev_boots"), "hev_boots"));

		// The trenchmaster (ArmorTrenchmaster): an unbreakable set of buffs; the two combat-port overrides stay unported (base hooks have no callers)
		ArmorMaterial aMatTrench = EnumHelper.addArmorMaterial("HBM_TRENCH", "hbm:trench", 150, new int[] { 3, 8, 6, 3 }, 0,
				SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, 0F);
		aMatTrench.repairMaterial = new ItemStack(ModItems.plate_iron);
		created.add(ModItems.trenchmaster_helmet = HbmRegistry.item(new ArmorTrenchmaster(aMatTrench, 0, "hbm:textures/armor/starmetal_1.png")
				.addEffect(new PotionEffect(MobEffects.STRENGTH, 20, 2))
				.addEffect(new PotionEffect(MobEffects.HASTE, 20, 1))
				.addEffect(new PotionEffect(MobEffects.JUMP_BOOST, 20, 1))
				.addEffect(new PotionEffect(MobEffects.SPEED, 20, 0))
				.enableVATS(true)
				.setStepSize(1)
				.setSealed(true)
				.hides(EnumPlayerPart.HAT)
				.setRadResist(1D)
				.setHazardClass(ArmorUtil.FULL_PACKAGE)
				.setUnlocalizedName("trenchmaster_helmet"), "trenchmaster_helmet"));
		created.add(ModItems.trenchmaster_plate = HbmRegistry.item(new ArmorTrenchmaster(aMatTrench, 1, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.trenchmaster_helmet).setUnlocalizedName("trenchmaster_plate"), "trenchmaster_plate"));
		created.add(ModItems.trenchmaster_legs = HbmRegistry.item(new ArmorTrenchmaster(aMatTrench, 2, "hbm:textures/armor/starmetal_2.png").cloneStats((ArmorFSB) ModItems.trenchmaster_helmet).setUnlocalizedName("trenchmaster_legs"), "trenchmaster_legs"));
		created.add(ModItems.trenchmaster_boots = HbmRegistry.item(new ArmorTrenchmaster(aMatTrench, 3, "hbm:textures/armor/starmetal_1.png").cloneStats((ArmorFSB) ModItems.trenchmaster_helmet).setUnlocalizedName("trenchmaster_boots"), "trenchmaster_boots"));
	}

	@Override
	public void registerModels() {
		for(Item item : created) ItemModels.registerAll(item);
	}
}
