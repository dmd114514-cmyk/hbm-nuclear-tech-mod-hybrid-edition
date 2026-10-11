package com.hbm.items.special;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The long-half-life nuclear waste. meta is the nuclide kind; the recycler reads liquid/gas yields off the enum.
 */
public class ItemWasteLong extends ItemNuclearWaste {

	public ItemWasteLong() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(int i = 0; i < WasteClass.values().length; i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add(TextFormatting.ITALIC + WasteClass.values()[rectify(stack.getItemDamage())].name);
	}

	public static int rectify(int meta) {
		return Math.abs(meta) % WasteClass.values().length;
	}

	public enum WasteClass {

		//all decayed versions include lead-types and classic nuclear waste
		URANIUM235("Uranium-235", 0, 0),	//plutonium 239 and 240, neptunium 237 / -
		URANIUM233("Uranium-233", 0, 50),	//uranium 235, plutonium 239, neptunium 237 / -
		NEPTUNIUM("Neptunium-237", 0, 100),	//plutonium 239 and uranium 238 / -
		THORIUM("Thorium-232", 0, 0),		//uranium 233 and uranium 235 / -
		SCHRABIDIUM("Schrabidium-326", 0, 250); //tantalum, neodymium, solinium, euphemium, ghiorsium-336 / -

		public String name;
		public int liquid;
		public int gas;

		private WasteClass(String name, int liquid, int gas) {
			this.name = name;
			this.liquid = liquid;
			this.gas = gas;
		}
	}
}
