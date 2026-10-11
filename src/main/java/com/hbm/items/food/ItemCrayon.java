package com.hbm.items.food;

import java.util.Arrays;
import java.util.Locale;

import com.hbm.items.IColoredItem;
import com.hbm.items.IMultiModelItem;
import com.hbm.items.machine.ItemChemicalDye.EnumChemDye;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * The crayons. Sixteen colors, drawable on walls and edible — vanilla's own writing; don't ask about nutrition.
 * One shared base image; the color comes entirely from the mask layer tinted by dye color.
 */
public class ItemCrayon extends ItemFoodBase implements IColoredItem, IMultiModelItem {

	public ItemCrayon() {
		super(3, false);
		this.setHasSubtypes(true);
		this.setAlwaysEdible();
	}

	/** Sixteen colorways sharing one base image; one model */
	@Override
	public String[] subtypeModels() {
		String[] names = new String[EnumChemDye.values().length];
		Arrays.fill(names, this.getRegistryName().getResourcePath());
		return names;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(int i = 0; i < EnumChemDye.values().length; i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + grab(stack.getItemDamage()).name().toLowerCase(Locale.US);
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex != 1) return 0xFFFFFF;
		return grab(stack.getItemDamage()).color;
	}

	private EnumChemDye grab(int meta) {
		EnumChemDye[] values = EnumChemDye.values();
		return values[Math.abs(meta % values.length)];
	}
}
