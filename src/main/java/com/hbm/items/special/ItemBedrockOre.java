package com.hbm.items.special;

import static com.hbm.items.special.ItemByproduct.EnumByproduct.*;

import java.util.Locale;

import com.hbm.items.IColoredItem;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.special.ItemByproduct.EnumByproduct;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.item.ItemStack;

/**
 * The bedrock ore series. A base image plus a white mask tinted by ore kind (two layers, the second taking color),
 * so these nineteen metas share one base, told apart only by tint — which is why the model hangs just one.
 *
 * The chain: bedrock ore → centrifuge → washed → separated → purified → enriched; each step's byproducts under EnumByproduct.
 */
public class ItemBedrockOre extends ItemEnumMulti implements IColoredItem {

	public ItemBedrockOre() {
		super(EnumBedrockOre.class, false, false);
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex != 1) return 0xFFFFFF;
		return ((EnumBedrockOre) grab(EnumBedrockOre.class, stack.getItemDamage())).color;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		EnumBedrockOre ore = (EnumBedrockOre) grab(EnumBedrockOre.class, stack.getItemDamage());
		String oreName = I18nUtil.resolveKey("item.ore." + ore.oreName.toLowerCase(Locale.US));
		return I18nUtil.resolveKey(this.getUnlocalizedNameInefficiently(stack) + ".name", oreName);
	}

	/*
	 * BYPRODUCT TIER 1: NITRIC ACID - CHEMPLANT GATE / NO GATE
	 * BYPRODUCT TIER 2: ORGANIC SOLVENT - CRACKING OIL GATE
	 * BYPRODUCT TIER 3: HIPERF SOLVENT - RBMK GATE
	 * BYPRODUCT TIER 4: SCHRABIDIC ACID - FUSION GATE?
	 *
	 * [BEDROCK x1] -C-> [CENTRIFUGED x4] -(PER)-> [CLEANED x4] -C-> [SEPARATED x16] -(SUL)-> [PURIFIED x16] -C-> [ENRICHED x64]
	 */

	public static enum EnumBedrockOre {
		//Ore								Byproduct	1,			2,			3
		IRON("Iron", 0xE2C0AA,						B_SULFUR,	B_TITANIUM,	B_TITANIUM), //titanium, sulfur from pyrite
		COPPER("Copper", 0xEC9A63,					B_SULFUR,	B_SULFUR,	B_SULFUR), //sulfur sulfur sulfur sulfur
		BORAX("Borax", 0xE4BE74, 					B_LITHIUM, 	B_CALCIUM, 	B_CALCIUM), //calcium from ulexite, uhhh lithium?
		ASBESTOS("Asbestos", 0xBFBFB9,				B_SILICON,	B_SILICON,	B_SILICON), //quartz i guess?
		NIOBIUM("Niobium", 0xAF58D8,				B_IRON,		B_IRON,		B_IRON), //iron in columbite, often found along tantalite
		NICKEL("Nickel", 0xBAB6AA,				B_SULFUR,	B_IRON,		B_ARSENIC), //nickel is generally found with sulfur, iron, and trace amounts of arsenic
		ZINC("Zinc", 0xA79DA8,					B_SULFUR,	B_LEAD,		B_IRON),
		TITANIUM("Titanium", 0xF2EFE2,				B_SILICON,	B_CALCIUM,	B_ALUMINIUM), //titanite is titanium + calcium + silicon with traces of iron and aluminium
		TUNGSTEN("Tungsten", 0x2C293C,				B_LEAD,		B_IRON,		B_BISMUTH), //ferberite has iron, raspite has lead, russelite is bismuth tungsten
		GOLD("Gold", 0xF9D738,						B_LEAD,		B_COPPER,	B_BISMUTH), //occurs with copper, lead and rare bismuthide
		BISMUTH("Bismuth", 0xffffff,				B_LEAD,		B_COPPER,	B_SULFUR), //seems to be similar, based off Aikinite
		CAD("Cadmium", 0xA85400,					B_SULFUR,	B_CALCIUM,	B_STRONTIUM), //strontium for utility, no other real place for it (and this old BRO shit is approaching nuketh)
		URANIUM("Uranium", 0x868D82,				B_LEAD,		B_RADIUM,	B_POLONIUM), //uranium and its decay products
		THORIUM("Thorium232", 0x7D401D,				B_SILICON,	B_URANIUM,	B_TECHNETIUM), //thorium occours with uraninite and decay products
		CHLOROCALCITE("Chlorocalcite", 0xCDE036, 	B_LITHIUM, 	B_SILICON, 	B_SILICON), //i guess?
		FLUORITE("Fluorite", 0xF6F3E7, 				B_SILICON, 	B_LITHIUM, 	B_ALUMINIUM), //different silicon-bearing gemstones, generic lithium, aluminium from sodium compound trailings
		HEMATITE("Hematite", 0xA37B72,				B_SULFUR,	B_TITANIUM,	B_TITANIUM), //titanium, sulfur from pyrite
		MALACHITE("Malachite", 0x66B48C,			B_SULFUR,	B_SULFUR,	B_SULFUR), //sulfur sulfur sulfur sulfur
		NEODYMIUM("Neodymium", 0x8F8F5F,			B_LITHIUM,	B_SILICON,	B_BISMUTH); //yeah whatever

		public String oreName;
		public int color;
		public EnumByproduct[] byproducts;

		/*Byproduct counts must match the current processing stage; pass NULL when there are none */
		private EnumBedrockOre(String name, int color, EnumByproduct... by) {
			this.oreName = name;
			this.color = color;
			this.byproducts = by;
		}
	}
}
