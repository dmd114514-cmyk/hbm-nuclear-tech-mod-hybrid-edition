package com.hbm.inventory.recipes;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.blocks.generic.ModBlocksGeneric;
import com.hbm.crafting.SerializableRecipe;
import com.hbm.interfaces.Untested;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.items.ItemEnums.EnumChunkType;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemBedrockOre.EnumBedrockOre;
import com.hbm.main.MainRegistry;
import com.hbm.util.Compat;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

/**
 * The shredder table: one in, one dust out — a one-to-one input-to-output map.
 * Split in two halves like the old version — the registerPost half generates in bulk off the ore dict (ingot/plate/gem/crystal/ore/block/dust),
 * while registerDefaults is the hand-written gameplay content. Since the bulk half only makes sense after every mod's ore dict registers,
 * it rides the framework's post hook, and the written template contains only the hand-written half.
 */
public class ShredderRecipes extends SerializableRecipe {

	public static HashMap<ComparableStack, ItemStack> shredderRecipes = new HashMap();
	/* The display side's lazy copies; called neiXxx in the old version — no NEI here, renamed by purpose */
	public static HashMap<Object, Object> displayRecipes;

	@Override
	public void registerPost() {

		String[] names = OreDictionary.getOreNames();

		for(int i = 0; i < names.length; i++) {

			String name = names[i];

			if(name == null || name.isEmpty())
				continue;

			if(name.contains("Any")) continue;

			List<ItemStack> matches = OreDictionary.getOres(name);

			if(matches == null || matches.isEmpty())
				continue;

			//1 ingot unit, metal
			generateRecipes("ingot", name, matches, 1);
			generateRecipes("plate", name, matches, 1);
			//1 ingot unit, crystalline
			generateRecipes("gem", name, matches, 1);
			generateRecipes("crystal", name, matches, 1);
			//2 ingot units, any
			generateRecipes("ore", name, matches, 2);

			if(name.length() > 5 && name.substring(0, 5).equals("block")) {
				ItemStack dust = getDustByName(name.substring(5));

				if(dust != null && dust.getItem() != ModItems.scrap) {

					dust.setCount(9);

					if(getIngotOrGemByName(name.substring(5)) == null)
						dust.setCount(4);

					for(ItemStack stack : matches) {
						putIfValid(stack, dust, name);
					}
				}
			}

			if(name.length() > 7 && name.substring(0, 8).equals("dustTiny")) {
				for(ItemStack stack : matches) {
					putIfValid(stack, new ItemStack(ModItems.dust_tiny), name);
				}
			} else if(name.length() > 3 && name.substring(0, 4).equals("dust")) {
				for(ItemStack stack : matches) {
					putIfValid(stack, new ItemStack(ModItems.dust), name);
				}
			}
		}
	}

	@Untested
	private static void generateRecipes(String prefix, String name, List<ItemStack> matches, int outCount) {

		int len = prefix.length();

		if(name.length() > len && name.substring(0, len).equals(prefix)) {

			String matName = name.substring(len);

			ItemStack dust = getDustByName(matName);

			if(dust != null && dust.getItem() != ModItems.scrap) {

				dust.setCount(outCount);

				for(ItemStack stack : matches) {
					putIfValid(stack, dust, name);
				}
			}
		}
	}

	private static void putIfValid(ItemStack in, ItemStack dust, String name) {

		if(in != null) {

			if(in.getItem() != null) {
				setRecipe(new ComparableStack(in), dust);
			} else {
				MainRegistry.logger.error("Ore dict entry '" + name + "' has a null item in its stack! How does that even happen?");
				Thread.currentThread().dumpStack();
			}

		} else {
			MainRegistry.logger.error("Ore dict entry '" + name + "' has a null stack!");
			Thread.currentThread().dumpStack();
		}
	}

	@Override
	public void registerDefaults() {

		/* Primary recipes */
		setRecipe(Items.WHEAT, new ItemStack(ModItems.flour));

		setRecipe(ModItems.scrap, new ItemStack(ModItems.dust));
		setRecipe(ModItems.dust, new ItemStack(ModItems.dust));
		setRecipe(ModItems.dust_tiny, new ItemStack(ModItems.dust_tiny));
		setRecipe(Blocks.GLOWSTONE, new ItemStack(Items.GLOWSTONE_DUST, 4));
		setRecipe(ModBlocks.reinforced_light, new ItemStack(Items.GLOWSTONE_DUST, 4));
		// skipped (piece not landed: ore_glowstone — ore block family is block-side): the glowstone ore row, old line 146
		setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 0), new ItemStack(ModItems.powder_quartz, 4));
		setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 1), new ItemStack(ModItems.powder_quartz, 4));
		setRecipe(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 2), new ItemStack(ModItems.powder_quartz, 4));
		setRecipe(Blocks.QUARTZ_STAIRS, new ItemStack(ModItems.powder_quartz, 3));
		/* This version's quartz slab is still STONE_SLAB meta 7 (BlockStoneSlab.EnumType.QUARTZ), same tier as before */
		setRecipe(new ItemStack(Blocks.STONE_SLAB, 1, 7), new ItemStack(ModItems.powder_quartz, 2));
		setRecipe(Items.QUARTZ, new ItemStack(ModItems.powder_quartz));
		setRecipe(Blocks.QUARTZ_ORE, new ItemStack(ModItems.powder_quartz, 2));
		// skipped (piece not landed: ore_quartz — ore block family is block-side): the mysterious ore row, old line 154
		// skipped (piece not landed: ore_fire — ore block family is block-side): the two fire ore rows (WILDCARD tier included), old lines 155~156
		setRecipe(Blocks.PACKED_ICE, new ItemStack(ModItems.powder_ice, 1));
		setRecipe(ModBlocks.brick_light, new ItemStack(Items.CLAY_BALL, 4));
		setRecipe(ModBlocksGeneric.concrete, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(ModBlocksGeneric.concrete_smooth, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(ModBlocksGeneric.brick_concrete, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(ModBlocks.brick_concrete_mossy, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(ModBlocks.brick_concrete_cracked, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(ModBlocks.brick_concrete_broken, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(ModBlocks.brick_obsidian, new ItemStack(ModBlocksGeneric.gravel_obsidian, 1));
		setRecipe(Blocks.OBSIDIAN, new ItemStack(ModBlocksGeneric.gravel_obsidian, 1));
		// skipped (piece not landed: ore_oil_empty / ore_gas_empty — ore block family is block-side): the empty oil ore and empty gas ore rows, old lines 167~168
		setRecipe(Blocks.COBBLESTONE, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(Blocks.STONEBRICK, new ItemStack(Blocks.GRAVEL, 1));
		setRecipe(Blocks.GRAVEL, new ItemStack(Blocks.SAND, 1));
		setRecipe(Blocks.BRICK_BLOCK, new ItemStack(Items.CLAY_BALL, 4));
		setRecipe(Blocks.BRICK_STAIRS, new ItemStack(Items.CLAY_BALL, 3));
		setRecipe(Items.FLOWER_POT, new ItemStack(Items.CLAY_BALL, 3));
		setRecipe(Items.BRICK, new ItemStack(Items.CLAY_BALL, 1));
		setRecipe(Blocks.SANDSTONE, new ItemStack(Blocks.SAND, 4));
		setRecipe(Blocks.SANDSTONE_STAIRS, new ItemStack(Blocks.SAND, 6));
		setRecipe(Blocks.CLAY, new ItemStack(Items.CLAY_BALL, 4));
		setRecipe(Blocks.HARDENED_CLAY, new ItemStack(Items.CLAY_BALL, 4));
		setRecipe(Blocks.TNT, new ItemStack(Items.GUNPOWDER, Compat.isModLoaded(Compat.MOD_GT6) ? 4 : 5));
		// skipped (piece not landed: stone_resource — the stone resource family is block-side): limestone's two rows (the restricted-stone and calcium tiers), old lines 181, 220~221
		setRecipe(ModBlocks.stone_gneiss, new ItemStack(ModItems.powder_lithium_tiny, 1));
		setRecipe(ModItems.fragment_neodymium, new ItemStack(ModItems.powder_neodymium_tiny, 1));
		setRecipe(ModItems.fragment_cobalt, new ItemStack(ModItems.powder_cobalt_tiny, 1));
		setRecipe(ModItems.fragment_niobium, new ItemStack(ModItems.powder_niobium_tiny, 1));
		setRecipe(ModItems.fragment_cerium, new ItemStack(ModItems.powder_cerium_tiny, 1));
		setRecipe(ModItems.fragment_lanthanium, new ItemStack(ModItems.powder_lanthanium_tiny, 1));
		setRecipe(ModItems.fragment_actinium, new ItemStack(ModItems.powder_actinium_tiny, 1));
		setRecipe(ModItems.fragment_boron, new ItemStack(ModItems.powder_boron_tiny, 1));
		setRecipe(ModItems.fragment_meteorite, new ItemStack(ModItems.powder_meteorite_tiny, 1));
		setRecipe(ModBlocksGeneric.block_meteor, new ItemStack(ModItems.powder_meteorite, 10));
		setRecipe(Items.ENCHANTED_BOOK, new ItemStack(ModItems.powder_magic, 1));
		setRecipe(ModBlocks.meteor_polished, new ItemStack(ModItems.powder_meteorite, 1));
		setRecipe(ModBlocks.meteor_brick, new ItemStack(ModItems.powder_meteorite, 1));
		setRecipe(ModBlocks.meteor_brick_mossy, new ItemStack(ModItems.powder_meteorite, 1));
		setRecipe(ModBlocks.meteor_brick_cracked, new ItemStack(ModItems.powder_meteorite, 1));
		setRecipe(ModBlocks.meteor_brick_chiseled, new ItemStack(ModItems.powder_meteorite, 1));
		setRecipe(ModBlocksGeneric.meteor_pillar, new ItemStack(ModItems.powder_meteorite, 1));
		// skipped (piece not landed: ore_rare / ore_sellafield_diamond / laythe_coral_block / ore_mineral / block_slag / ore_aluminium / ore_nickel — ore and slag block families are block-side): seven rows — rare ore, sellafield diamond ore, coral calcium block, vein dust, slag block, aluminium ore and nickel ore, around old lines 199~227
		setRecipe(ModItems.ingot_schrabidate, new ItemStack(ModItems.powder_schrabidate, 1));
		setRecipe(ModBlocksGeneric.block_schrabidate, new ItemStack(ModItems.powder_schrabidate, 9));
		setRecipe(ModItems.coal_infernal, new ItemStack(ModItems.powder_coal, 2));
		setRecipe(Items.FERMENTED_SPIDER_EYE, new ItemStack(ModItems.powder_poison, 3));
		setRecipe(Items.POISONOUS_POTATO, new ItemStack(ModItems.powder_poison, 1));
		setRecipe(ModBlocks.ore_tektite_osmiridium, new ItemStack(ModItems.powder_tektite, 1));
		setRecipe(Blocks.DIRT, new ItemStack(ModItems.dust, 1));
		setRecipe(Items.REEDS, new ItemStack(Items.SUGAR, 3));
		setRecipe(Items.APPLE, new ItemStack(Items.SUGAR, 1));
		setRecipe(Items.CARROT, new ItemStack(Items.SUGAR, 1));
		setRecipe(ModItems.crystal_cleaned, new ItemStack(ModItems.mineral_dust, 4));
		setRecipe(ModItems.bean_roast, new ItemStack(ModItems.powder_coffee, 1));
		setRecipe(ModItems.can_empty, new ItemStack(ModItems.powder_aluminium, 2));
		setRecipe(DictFrame.fromOne(ModItems.chunk_ore, EnumChunkType.RARE), new ItemStack(ModItems.powder_desh_mix));
		setRecipe(Blocks.SAND, new ItemStack(ModItems.dust, 2));

		// skipped (piece not landed: ModBlocks.boxcar — the caboose block is block-side): the caboose-shredded-into-steel-dust row, old line 202

		setRecipe(ModBlocksGeneric.vinyl_log, new ItemStack(ModItems.powder_rubber, 4));
		setRecipe(ModBlocksGeneric.pvc_log, new ItemStack(ModItems.powder_pvc, 4));
		setRecipe(ModBlocks.vinyl_planks, new ItemStack(ModItems.powder_rubber, 1));
		setRecipe(ModBlocks.pvc_planks, new ItemStack(ModItems.powder_pvc, 1));

		List<ItemStack> logs = OreDictionary.getOres(OreDictManager.KEY_LOG);
		List<ItemStack> planks = OreDictionary.getOres(OreDictManager.KEY_PLANKS);
		List<ItemStack> saplings = OreDictionary.getOres(OreDictManager.KEY_SAPLING);
		List<ItemStack> stones = OreDictionary.getOres(OreDictManager.KEY_STONE);
		List<ItemStack> cobbles = OreDictionary.getOres(OreDictManager.KEY_COBBLESTONE);
		List<ItemStack> sands = OreDictionary.getOres(OreDictManager.KEY_SAND);
		List<ItemStack> lapis = OreDictionary.getOres("dustLapis");

		for(ItemStack log : logs) setRecipe(log, new ItemStack(ModItems.powder_sawdust, 4));
		for(ItemStack plank : planks) setRecipe(plank, new ItemStack(ModItems.powder_sawdust, 1));
		for(ItemStack sapling : saplings) setRecipe(sapling, new ItemStack(Items.STICK, 1));
		for(ItemStack stone : stones) setRecipe(stone, new ItemStack(Blocks.GRAVEL, 1));
		for(ItemStack cobble : cobbles) setRecipe(cobble, new ItemStack(Blocks.GRAVEL, 1));
		for(ItemStack sand : sands) setRecipe(sand, new ItemStack(ModItems.dust, 2));
		for(ItemStack dust : lapis) setRecipe(dust, new ItemStack(ModItems.powder_cobalt_tiny, 1));

		for(EnumBedrockOre ore : EnumBedrockOre.values()) {
			int i = ore.ordinal();
			setRecipe(new ItemStack(ModItems.ore_bedrock, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_centrifuged, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_cleaned, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_separated, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_purified, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_nitrated, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_nitrocrystalline, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_deepcleaned, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
			setRecipe(new ItemStack(ModItems.ore_seared, 1, i), new ItemStack(ModItems.ore_enriched, 1, i));
		}

		for(int i = 0; i < 5; i++) setRecipe(new ItemStack(Items.SKULL, 1, i), new ItemStack(ModItems.biomass, 4));

		/* Crystal processing */
		setRecipe(ModItems.ingot_schraranium, new ItemStack(ModItems.nugget_schrabidium, 2));
		setRecipe(ModItems.crystal_coal, new ItemStack(ModItems.powder_coal, 3));
		setRecipe(ModItems.crystal_iron, new ItemStack(ModItems.powder_iron, 3));
		setRecipe(ModItems.crystal_gold, new ItemStack(ModItems.powder_gold, 3));
		setRecipe(ModItems.crystal_redstone, new ItemStack(Items.REDSTONE, 8));
		setRecipe(ModItems.crystal_lapis, new ItemStack(ModItems.powder_lapis, 8));
		setRecipe(ModItems.crystal_diamond, new ItemStack(ModItems.powder_diamond, 3));
		setRecipe(ModItems.crystal_uranium, new ItemStack(ModItems.powder_uranium, 3));
		setRecipe(ModItems.crystal_plutonium, new ItemStack(ModItems.powder_plutonium, 3));
		setRecipe(ModItems.crystal_thorium, new ItemStack(ModItems.powder_thorium, 3));
		setRecipe(ModItems.crystal_titanium, new ItemStack(ModItems.powder_titanium, 3));
		setRecipe(ModItems.crystal_sulfur, new ItemStack(ModItems.sulfur, 8));
		setRecipe(ModItems.crystal_niter, new ItemStack(ModItems.niter, 8));
		setRecipe(ModItems.crystal_copper, new ItemStack(ModItems.powder_copper, 3));
		setRecipe(ModItems.crystal_tungsten, new ItemStack(ModItems.powder_tungsten, 3));
		setRecipe(ModItems.crystal_aluminium, new ItemStack(ModItems.powder_aluminium, 3));
		setRecipe(ModItems.crystal_fluorite, new ItemStack(ModItems.fluorite, 8));
		setRecipe(ModItems.crystal_beryllium, new ItemStack(ModItems.powder_beryllium, 3));
		setRecipe(ModItems.crystal_lead, new ItemStack(ModItems.powder_lead, 3));
		setRecipe(ModItems.crystal_schraranium, new ItemStack(ModItems.nugget_schrabidium, 3));
		setRecipe(ModItems.crystal_schrabidium, new ItemStack(ModItems.powder_schrabidium, 3));
		setRecipe(ModItems.crystal_rare, new ItemStack(ModItems.powder_desh_mix, 2));
		setRecipe(ModItems.crystal_phosphorus, new ItemStack(ModItems.powder_fire, 8));
		setRecipe(ModItems.crystal_trixite, new ItemStack(ModItems.powder_plutonium, 6));
		setRecipe(ModItems.crystal_lithium, new ItemStack(ModItems.powder_lithium, 3));
		setRecipe(ModItems.crystal_starmetal, new ItemStack(ModItems.powder_dura_steel, 6));
		setRecipe(ModItems.crystal_cobalt, new ItemStack(ModItems.powder_cobalt, 3));
		setRecipe(ModItems.crystal_nickel, new ItemStack(ModItems.powder_nickel, 3));
		setRecipe(ModItems.crystal_niobium, new ItemStack(ModItems.powder_niobium, 3));
		setRecipe(ModItems.crystal_zinc, new ItemStack(ModItems.powder_zinc, 3));

		/* Misc recycling */
		// skipped (piece not landed: steel_poles / steel_roof / steel_wall / steel_corner / steel_beam / steel_scaffold / chain / steel_grate — the steel deco family is block-side): eight steel-piece recycling rows, old lines 298~303, 314~315
		setRecipe(ModItems.coil_copper, new ItemStack(ModItems.powder_red_copper, 1));
		setRecipe(ModItems.coil_copper_torus, new ItemStack(ModItems.powder_red_copper, 2));
		setRecipe(ModItems.coil_gold, new ItemStack(ModItems.powder_gold, 1));
		setRecipe(ModItems.coil_gold_torus, new ItemStack(ModItems.powder_gold, 2));
		setRecipe(ModItems.coil_tungsten, new ItemStack(ModItems.powder_tungsten, 1));
		setRecipe(ModItems.coil_magnetized_tungsten, new ItemStack(ModItems.powder_magnetized_tungsten, 1));
		setRecipe(ModBlocks.crate_iron, new ItemStack(ModItems.powder_iron, 8));
		setRecipe(ModBlocks.crate_steel, new ItemStack(ModItems.powder_steel, 8));
		setRecipe(ModBlocks.crate_tungsten, new ItemStack(ModItems.powder_tungsten, 36));
		setRecipe(Blocks.ANVIL, new ItemStack(ModItems.powder_iron, 31));
		setRecipe(ModItems.pipes_steel, new ItemStack(ModItems.powder_steel, 27));
		setRecipe(new ItemStack(ModItems.bedrock_ore, 1, OreDictionary.WILDCARD_VALUE), new ItemStack(Blocks.GRAVEL));

		/* Sellafite scrapping */
		// skipped (piece not landed: sellafield_slaked / sellafield — the sellafield block family is block-side): one slaked lime block row plus six tiers of nuclear waste fragments, old lines 320~326

		/* Fracking debris scrapping */
		setRecipe(ModBlocksGeneric.dirt_dead, new ItemStack(ModItems.scrap_oil, 1));
		setRecipe(ModBlocksGeneric.dirt_oily, new ItemStack(ModItems.scrap_oil, 1));
		setRecipe(ModBlocksGeneric.sand_dirty, new ItemStack(ModItems.scrap_oil, 1));
		setRecipe(ModBlocksGeneric.sand_dirty_red, new ItemStack(ModItems.scrap_oil, 1));
		setRecipe(ModBlocksGeneric.stone_cracked, new ItemStack(ModItems.scrap_oil, 1));
		// skipped (piece not landed: stone_porous — porous stone is block-side): the last fracking waste row, old line 334

		/* Deco pipe recycling */
		// skipped (piece not landed: the deco_pipe family's 24 tiers — deco pipes are block-side): the whole 24-row section, old lines 337~360

		/* Wool and clay scrapping */
		for(int i = 0; i < 16; i++) {
			setRecipe(new ItemStack(Blocks.STAINED_HARDENED_CLAY, 1, i), new ItemStack(Items.CLAY_BALL, 4));
			setRecipe(new ItemStack(Blocks.WOOL, 1, i), new ItemStack(Items.STRING, 4));
		}

		/* Shredding bobbleheads */
		for(int i = 0; i < BobbleType.values().length; i++) {
			BobbleType type = BobbleType.values()[i];
			setRecipe(new ItemStack(ModBlocks.bobblehead, 1, i), new ItemStack(ModItems.scrap_plastic, 1, type.scrap.ordinal()));
		}

		/* Debris shredding */
		setRecipe(ModItems.debris_concrete, new ItemStack(ModItems.scrap_nuclear, 2));
		setRecipe(ModItems.debris_shrapnel, new ItemStack(ModItems.powder_steel_tiny, 5));
		setRecipe(ModItems.debris_exchanger, new ItemStack(ModItems.powder_steel, 3));
		setRecipe(ModItems.debris_element, new ItemStack(ModItems.scrap_nuclear, 4));
		setRecipe(ModItems.debris_metal, new ItemStack(ModItems.powder_steel_tiny, 3));
		setRecipe(ModItems.debris_graphite, new ItemStack(ModItems.powder_coal, 1));

		/* GC COMPAT */
		Block gcMoonBlock = blockFromMod(Compat.MOD_GCC, "moonBlock");
		if(gcMoonBlock != null && gcMoonBlock != Blocks.AIR) {
			setRecipe(new ItemStack(gcMoonBlock, 1, 3), new ItemStack(ModBlocksGeneric.moon_turf)); //Moon dirt
			setRecipe(new ItemStack(gcMoonBlock, 1, 5), new ItemStack(ModBlocksGeneric.moon_turf)); //Moon topsoil
		}

		/* AR COMPAT */
		Block arMoonTurf = blockFromMod(Compat.MOD_AR, "turf");
		if(arMoonTurf != null && arMoonTurf != Blocks.AIR) setRecipe(arMoonTurf, new ItemStack(ModBlocksGeneric.moon_turf)); //i assume it's moon turf
		Block arMoonTurfDark = blockFromMod(Compat.MOD_AR, "turfDark");
		if(arMoonTurfDark != null && arMoonTurfDark != Blocks.AIR) setRecipe(arMoonTurfDark, new ItemStack(ModBlocksGeneric.moon_turf)); //probably moon dirt? would have helped if i had ever played AR for more than 5 seconds
	}

	/**
	 * The old version went through Compat.tryLoadBlock; this tree's Compat only has the tryLoadItem half;
	 * blocks are fetched inline in the same shape until the common compat layer fills in.
	 */
	private static Block blockFromMod(String domain, String name) {
		return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(domain, name));
	}

	/**
	 * Returns scrap when no dust is found, for quickly adding recipes
	 * @param name
	 * @return
	 */
	public static ItemStack getDustByName(String name) {

		List<ItemStack> matches = OreDictionary.getOres("dust" + name);

		if(matches != null && !matches.isEmpty()) {
			/* The old version picked the preferred mod's dust here via GeneralConfig.preferredOutputMod; that config entry hasn't landed,
			 * so the dict list's head entry goes (the old fallback behavior); swap back to the preferred pick once the config lands. */
			return matches.get(0).copy();
		}

		return new ItemStack(ModItems.scrap);
	}

	/**
	 * Returns null when no ingot or gem is found, for deciding whether the block shredding output should be 9 or 4 dusts
	 * @param name
	 * @return
	 */
	public static ItemStack getIngotOrGemByName(String name) {

		List<ItemStack> matches = OreDictionary.getOres("ingot" + name);

		if(matches != null && !matches.isEmpty())
			return matches.get(0).copy();

		matches = OreDictionary.getOres("gem" + name);

		if(matches != null && !matches.isEmpty())
			return matches.get(0).copy();

		return null;
	}

	public static void setRecipe(Item in, ItemStack out) {
		setRecipe(new ComparableStack(in), out);
	}

	public static void setRecipe(Block in, ItemStack out) {
		setRecipe(new ComparableStack(in), out);
	}

	public static void setRecipe(ItemStack in, ItemStack out) {
		setRecipe(new ComparableStack(in), out);
	}

	public static void setRecipe(ComparableStack in, ItemStack out) {
		if(!shredderRecipes.containsKey(in)) {
			shredderRecipes.put(in, out);
		}
	}

	public static Map<Object, Object> getShredderRecipes() {

		//convert the map only once to save on processing power (might be more ram intensive but that can't be THAT bad, right?)
		if(displayRecipes == null)
			displayRecipes = new HashMap(shredderRecipes);

		return displayRecipes;
	}

	public static ItemStack getShredderResult(ItemStack stack) {

		if(stack == null || stack.getItem() == null)
			return new ItemStack(ModItems.scrap);

		ComparableStack comp = new ComparableStack(stack).makeSingular();
		ItemStack sta = shredderRecipes.get(comp);

		if(sta == null) {
			comp.meta = OreDictionary.WILDCARD_VALUE;
			sta = shredderRecipes.get(comp);
		}

		return sta == null ? new ItemStack(ModItems.scrap) : sta;
	}

	@Override
	public String getFileName() {
		return "hbmShredder.json";
	}

	@Override
	public Object getRecipeObject() {
		return shredderRecipes;
	}

	@Override
	public void readRecipe(JsonElement recipe) {
		JsonObject obj = (JsonObject) recipe;
		ItemStack stack = this.readItemStack(obj.get("input").getAsJsonArray());
		ComparableStack comp = new ComparableStack(stack).makeSingular();
		ItemStack out = this.readItemStack(obj.get("output").getAsJsonArray());
		this.shredderRecipes.put(comp, out);
	}

	@Override
	public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
		Entry<ComparableStack, ItemStack> entry = (Entry<ComparableStack, ItemStack>) recipe;

		writer.name("input");
		this.writeItemStack(entry.getKey().toStack(), writer);
		writer.name("output");
		this.writeItemStack(entry.getValue(), writer);
	}

	@Override
	public void deleteRecipes() {
		this.shredderRecipes.clear();
		this.displayRecipes = null;
	}

	@Override
	public String getComment() {
		return "Ingot/block/ore -> dust recipes are generated in post and can therefore not be changed with the config. Non-auto recipes do not use ore dict.";
	}
}
