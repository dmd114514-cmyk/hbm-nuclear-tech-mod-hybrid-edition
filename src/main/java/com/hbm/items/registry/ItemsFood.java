package com.hbm.items.registry;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ModItems;
import com.hbm.items.food.ItemAppleEuphemium;
import com.hbm.items.food.ItemAppleSchrabidium;
import com.hbm.items.food.ItemBDCL;
import com.hbm.items.food.ItemCanteen;
import com.hbm.items.food.ItemCottonCandy;
import com.hbm.items.food.ItemCrayon;
import com.hbm.items.food.ItemEnergy;
import com.hbm.items.food.ItemFlask;
import com.hbm.items.food.ItemLemon;
import com.hbm.items.food.ItemMarshmallow;
import com.hbm.items.food.ItemMuchoMango;
import com.hbm.items.food.ItemPancake;
import com.hbm.items.food.ItemPill;
import com.hbm.items.food.ItemSchnitzelVegan;
import com.hbm.items.food.ItemTemFlakes;
import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;

import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSoup;
import net.minecraft.potion.PotionEffect;

/**
 * The food subdomain registration unit.
 *
 * The bulk (`ItemLemon`/`ItemEnergy`/`ItemSimpleConsumable` batches) is stuck on
 * `HbmPlayerProps` (the player extension capability) and a PacketThreading replacement, waiting on A2/A4.
 * Landing whatever doesn't depend on them first.
 */
public class ItemsFood implements IRegistrar {

	private final List<Item> created = new ArrayList<>();

	@Override
	public String id() {
		return "items.food";
	}

	@Override
	public void preInit() {
		created.add(ModItems.crayon = HbmRegistry.item(new ItemCrayon().setUnlocalizedName("crayon").setCreativeTab(ModItems.partsTab), "crayon"));
		created.add(ModItems.bdcl = HbmRegistry.item(new ItemBDCL().setUnlocalizedName("bdcl").setCreativeTab(ModItems.consumableTab), "bdcl"));
		created.add(ModItems.marshmallow = HbmRegistry.item(new ItemMarshmallow().setUnlocalizedName("marshmallow").setMaxStackSize(1).setFull3D().setCreativeTab(ModItems.consumableTab), "marshmallow"));
		created.add(ModItems.mucho_mango = HbmRegistry.item(new ItemMuchoMango(10).setUnlocalizedName("mucho_mango").setCreativeTab(ModItems.consumableTab), "mucho_mango"));
		created.add(ModItems.cotton_candy = HbmRegistry.item(new ItemCottonCandy(5, false).setUnlocalizedName("cotton_candy").setCreativeTab(ModItems.consumableTab).setFull3D(), "cotton_candy"));
		created.add(ModItems.tem_flakes = HbmRegistry.item(new ItemTemFlakes(0, 0, false).setUnlocalizedName("tem_flakes").setCreativeTab(ModItems.consumableTab), "tem_flakes"));
		created.add(ModItems.schnitzel_vegan = HbmRegistry.item(new ItemSchnitzelVegan(0, true).setUnlocalizedName("schnitzel_vegan").setCreativeTab(ModItems.consumableTab), "schnitzel_vegan"));
		created.add(ModItems.apple_euphemium = HbmRegistry.item(new ItemAppleEuphemium(20, 100, false).setUnlocalizedName("apple_euphemium").setMaxStackSize(1), "apple_euphemium"));
		created.add(ModItems.apple_schrabidium = HbmRegistry.item(new ItemAppleSchrabidium(20, 100, false).setUnlocalizedName("apple_schrabidium").setCreativeTab(ModItems.consumableTab), "apple_schrabidium"));
		created.add(ModItems.apple_lead = HbmRegistry.item(new ItemAppleSchrabidium(5, 0, false).setUnlocalizedName("apple_lead").setCreativeTab(ModItems.consumableTab), "apple_lead"));
		created.add(ModItems.pancake = HbmRegistry.item(new ItemPancake(20, 1.0F, false).setUnlocalizedName("pancake").setCreativeTab(ModItems.consumableTab), "pancake"));
		// Chicken nuggets: the old version was literally ItemLemon(200, 1F, false); ItemFoodBase takes ItemLemon's seat here (and stays CT-tweakable)
		created.add(ModItems.nugget = HbmRegistry.item(new com.hbm.items.food.ItemFoodBase(200, 1.0F, false).setUnlocalizedName("nugget").setCreativeTab(ModItems.consumableTab), "nugget"));

		// The flask's two contents split by meta, name and texture derived from the enum name lowercase, two extra models provided
		created.add(ModItems.flask_infusion = HbmRegistry.item(new ItemFlask().setUnlocalizedName("flask_infusion").setCreativeTab(ModItems.consumableTab), "flask_infusion"));

		// The canteen: cooldown after drinking, the seconds living in constructor args
		created.add(ModItems.canteen_vodka = HbmRegistry.item(new ItemCanteen(3 * 60).setUnlocalizedName("canteen_vodka").setCreativeTab(ModItems.consumableTab), "canteen_vodka"));

		// The drink family (ItemEnergy): container/cap assembled by makeCan/makeBottle/makeGlass,
		// each piece a separate instance (effects hard-branch by item identity); the old setTextureName finds its model by registry name here
		created.add(ModItems.can_smart = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_smart"), "can_smart"));
		created.add(ModItems.can_creature = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_creature"), "can_creature"));
		created.add(ModItems.can_redbomb = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_redbomb"), "can_redbomb"));
		created.add(ModItems.can_mrsugar = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_mrsugar"), "can_mrsugar"));
		created.add(ModItems.can_overcharge = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_overcharge"), "can_overcharge"));
		created.add(ModItems.can_luna = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_luna"), "can_luna"));
		created.add(ModItems.can_bepis = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_bepis"), "can_bepis"));
		created.add(ModItems.can_breen = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_breen"), "can_breen"));
		created.add(ModItems.can_mug = HbmRegistry.item(new ItemEnergy().makeCan().setUnlocalizedName("can_mug"), "can_mug"));
		created.add(ModItems.bottle_nuka = HbmRegistry.item(new ItemEnergy().makeBottle(ModItems.bottle_empty, ModItems.cap_nuka).setUnlocalizedName("bottle_nuka"), "bottle_nuka"));
		created.add(ModItems.bottle_cherry = HbmRegistry.item(new ItemEnergy().makeBottle(ModItems.bottle_empty, ModItems.cap_nuka).setUnlocalizedName("bottle_cherry"), "bottle_cherry"));
		created.add(ModItems.bottle_quantum = HbmRegistry.item(new ItemEnergy().makeBottle(ModItems.bottle_empty, ModItems.cap_quantum).setUnlocalizedName("bottle_quantum"), "bottle_quantum"));
		created.add(ModItems.bottle_sparkle = HbmRegistry.item(new ItemEnergy().makeBottle(ModItems.bottle_empty, ModItems.cap_sparkle).setUnlocalizedName("bottle_sparkle"), "bottle_sparkle"));
		created.add(ModItems.bottle_rad = HbmRegistry.item(new ItemEnergy().makeBottle(ModItems.bottle_empty, ModItems.cap_rad).setUnlocalizedName("bottle_rad"), "bottle_rad"));
		created.add(ModItems.bottle2_korl = HbmRegistry.item(new ItemEnergy().makeBottle(ModItems.bottle2_empty, ModItems.cap_korl).setUnlocalizedName("bottle2_korl"), "bottle2_korl"));
		created.add(ModItems.bottle2_fritz = HbmRegistry.item(new ItemEnergy().makeBottle(ModItems.bottle2_empty, ModItems.cap_fritz).setUnlocalizedName("bottle2_fritz"), "bottle2_fritz"));
		created.add(ModItems.chocolate_milk = HbmRegistry.item(new ItemEnergy().setUnlocalizedName("chocolate_milk"), "chocolate_milk"));
		created.add(ModItems.coffee = HbmRegistry.item(new ItemEnergy().setUnlocalizedName("coffee"), "coffee"));
		created.add(ModItems.coffee_radium = HbmRegistry.item(new ItemEnergy().setUnlocalizedName("coffee_radium"), "coffee_radium"));
		created.add(ModItems.glass_smilk = HbmRegistry.item(new ItemEnergy().makeGlass().setUnlocalizedName("glass_smilk"), "glass_smilk"));
		created.add(ModItems.teacup = HbmRegistry.item(new ItemEnergy().makeGlass().setUnlocalizedName("teacup"), "teacup"));
		created.add(ModItems.bottle_honey = HbmRegistry.item(new ItemEnergy().makeGlass().setUnlocalizedName("bottle_honey"), "bottle_honey"));

		// The pill family: zero food, effects branch per piece inside onFoodEaten (old MainRegistry lines 3196~3208 plus the chocolate line)
		created.add(ModItems.radx = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("radx").setCreativeTab(ModItems.consumableTab), "radx"));
		created.add(ModItems.siox = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("siox").setCreativeTab(ModItems.consumableTab), "siox"));
		created.add(ModItems.pill_herbal = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("pill_herbal").setCreativeTab(ModItems.consumableTab), "pill_herbal"));
		created.add(ModItems.xanax = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("xanax").setCreativeTab(ModItems.consumableTab), "xanax"));
		created.add(ModItems.animan = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("animan").setCreativeTab(ModItems.consumableTab), "animan"));
		created.add(ModItems.fmn = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("fmn").setCreativeTab(ModItems.consumableTab), "fmn"));
		created.add(ModItems.five_htp = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("five_htp").setCreativeTab(ModItems.consumableTab), "five_htp"));
		created.add(ModItems.pill_iodine = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("pill_iodine").setCreativeTab(ModItems.consumableTab), "pill_iodine"));
		created.add(ModItems.plan_c = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("plan_c").setCreativeTab(ModItems.consumableTab), "plan_c"));
		created.add(ModItems.pill_red = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("pill_red").setCreativeTab(ModItems.consumableTab), "pill_red"));
		created.add(ModItems.chocolate = HbmRegistry.item(new ItemPill(0).setUnlocalizedName("chocolate").setCreativeTab(ModItems.consumableTab), "chocolate"));

		// The three soups stay vanilla ItemSoup: it isn't an ItemFoodBase, so CT's food knob can't reach these three's food values yet,
		// and the old version was also vanilla ItemSoup (bowl return handled by vanilla); table-tweakable waits until the family joins this tree's food base
		created.add(ModItems.glowing_stew = HbmRegistry.item(new ItemSoup(6).setUnlocalizedName("glowing_stew").setCreativeTab(ModItems.consumableTab), "glowing_stew"));
		created.add(ModItems.balefire_scrambled = HbmRegistry.item(new ItemSoup(6).setUnlocalizedName("balefire_scrambled").setCreativeTab(ModItems.consumableTab), "balefire_scrambled"));
		created.add(ModItems.balefire_and_ham = HbmRegistry.item(new ItemSoup(6).setUnlocalizedName("balefire_and_ham").setCreativeTab(ModItems.consumableTab), "balefire_and_ham"));

		// The ItemLemon family · parts-tab batch (the old textureName differing from the registry name; models here are laid out by registry name)
		created.add(ModItems.ingot_semtex = HbmRegistry.item(new ItemLemon(4, 5, true).setUnlocalizedName("ingot_semtex").setCreativeTab(ModItems.partsTab), "ingot_semtex"));
		created.add(ModItems.ingot_c4 = HbmRegistry.item(new ItemLemon(4, 5, true).setUnlocalizedName("ingot_c4").setCreativeTab(ModItems.partsTab), "ingot_c4"));
		created.add(ModItems.powder_cement = HbmRegistry.item(new ItemLemon(2, 0.5F, false).setUnlocalizedName("powder_cement").setCreativeTab(ModItems.partsTab), "powder_cement"));
		created.add(ModItems.saltleaf = HbmRegistry.item(new ItemLemon(2, 0.5F, false).setUnlocalizedName("saltleaf").setCreativeTab(ModItems.partsTab), "saltleaf"));
		created.add(ModItems.bio_wafer = HbmRegistry.item(new ItemLemon(4, 0.6F, false).setUnlocalizedName("bio_wafer").setCreativeTab(ModItems.partsTab), "bio_wafer"));
		created.add(ModItems.flesh_wafer = HbmRegistry.item(new ItemLemon(5, 0.6F, false).setUnlocalizedName("flesh_wafer").setCreativeTab(ModItems.partsTab), "flesh_wafer"));
		created.add(ModItems.grilled_flesh = HbmRegistry.item(new ItemLemon(6, 0.8F, false).setUnlocalizedName("grilled_flesh").setCreativeTab(ModItems.partsTab), "grilled_flesh"));
		created.add(ModItems.flesh_burger = HbmRegistry.item(new ItemLemon(7, 1.0F, false).setUnlocalizedName("flesh_burger").setCreativeTab(ModItems.partsTab), "flesh_burger"));

		// The ItemLemon family · intake-tab batch (nuggets already landed above as ItemFoodBase; not repeated here)
		created.add(ModItems.s_cream = HbmRegistry.item(new ItemLemon(8, 1.0F, false).setUnlocalizedName("s_cream").setCreativeTab(ModItems.consumableTab), "s_cream"));
		created.add(ModItems.min_cream = HbmRegistry.item(new ItemLemon(10, 1.0F, false).setUnlocalizedName("min_cream").setCreativeTab(ModItems.consumableTab), "min_cream"));
		created.add(ModItems.lemon = HbmRegistry.item(new ItemLemon(3, 0.5F, false).setUnlocalizedName("lemon").setCreativeTab(ModItems.consumableTab), "lemon"));
		created.add(ModItems.definitelyfood = HbmRegistry.item(new ItemLemon(3, 0.5F, false).setUnlocalizedName("definitelyfood").setCreativeTab(ModItems.consumableTab), "definitelyfood"));
		created.add(ModItems.med_ipecac = HbmRegistry.item(new ItemLemon(0, 0, false).setUnlocalizedName("med_ipecac").setCreativeTab(ModItems.consumableTab), "med_ipecac"));
		created.add(ModItems.med_ptsd = HbmRegistry.item(new ItemLemon(0, 0, false).setUnlocalizedName("med_ptsd").setCreativeTab(ModItems.consumableTab), "med_ptsd"));
		created.add(ModItems.med_schizophrenia = HbmRegistry.item(new ItemLemon(0, 0, false).setUnlocalizedName("med_schizophrenia").setCreativeTab(ModItems.consumableTab), "med_schizophrenia"));
		created.add(ModItems.loops = HbmRegistry.item(new ItemLemon(4, 0.25F, false).setUnlocalizedName("loops").setCreativeTab(ModItems.consumableTab), "loops"));
		created.add(ModItems.loop_stew = HbmRegistry.item(new ItemLemon(10, 0.5F, false).setMaxStackSize(1).setUnlocalizedName("loop_stew").setCreativeTab(ModItems.consumableTab), "loop_stew"));
		created.add(ModItems.spongebob_macaroni = HbmRegistry.item(new ItemLemon(5, 1F, false).setUnlocalizedName("spongebob_macaroni").setCreativeTab(ModItems.consumableTab), "spongebob_macaroni"));
		created.add(ModItems.fooditem = HbmRegistry.item(new ItemLemon(2, 0.6F, false).setUnlocalizedName("fooditem").setCreativeTab(ModItems.consumableTab), "fooditem"));
		created.add(ModItems.twinkie = HbmRegistry.item(new ItemLemon(3, 0.25F, false).setUnlocalizedName("twinkie").setCreativeTab(ModItems.consumableTab), "twinkie"));
		created.add(ModItems.static_sandwich = HbmRegistry.item(new ItemLemon(6, 1F, false).setUnlocalizedName("static_sandwich").setCreativeTab(ModItems.consumableTab), "static_sandwich"));
		created.add(ModItems.pudding = HbmRegistry.item(new ItemLemon(6, 0.6F, false).setUnlocalizedName("pudding").setCreativeTab(ModItems.consumableTab), "pudding"));
		created.add(ModItems.cheese = HbmRegistry.item(new ItemLemon(5, 0.75F, false).setUnlocalizedName("cheese").setCreativeTab(ModItems.consumableTab), "cheese"));
		// The old version spelled the unlocalized name cheese_quesadilla (lang and models built on it); the registry name must follow to line up
		created.add(ModItems.quesadilla = HbmRegistry.item(new ItemLemon(8, 1F, false).setUnlocalizedName("cheese_quesadilla").setCreativeTab(ModItems.consumableTab), "cheese_quesadilla"));
		created.add(ModItems.glyphid_meat = HbmRegistry.item(new ItemLemon(3, 0.5F, true).setUnlocalizedName("glyphid_meat").setCreativeTab(ModItems.consumableTab), "glyphid_meat"));
		// Cooked lizard meat: the old setPotionEffect(id, duration, level, chance); the first three fold into a PotionEffect object here
		created.add(ModItems.glyphid_meat_grilled = HbmRegistry.item(new ItemLemon(8, 0.75F, true).setPotionEffect(new PotionEffect(MobEffects.STRENGTH, 180, 1), 1F).setUnlocalizedName("glyphid_meat_grilled").setCreativeTab(ModItems.consumableTab), "glyphid_meat_grilled"));
		created.add(ModItems.chocolate_mint_billet = HbmRegistry.item(new ItemLemon(5, 5F, true).setUnlocalizedName("chocolate_mint_billet").setCreativeTab(ModItems.consumableTab), "chocolate_mint_billet"));
	}

	@Override
	public void registerModels() {
		for(Item item : created) ItemModels.registerAll(item);

		// The two apples' three meta tiers share one texture; registerAll's one-item-one-model approach only reaches tier 0
		ItemModels.registerRange(ModItems.apple_schrabidium, 3);
		ItemModels.registerRange(ModItems.apple_lead, 3);
	}

	@Override
	public void postInit() {
		// Self-tests the food domain's table semantics (default / set / multiply / illegal fields) only with debug mode on, and clears the table after
		if(com.hbm.config.GeneralConfig.enableDebugMode && ModItems.tem_flakes != null)
			com.hbm.items.food.ItemFoodBase.selfCheck(new net.minecraft.item.ItemStack(ModItems.tem_flakes));
	}
}
