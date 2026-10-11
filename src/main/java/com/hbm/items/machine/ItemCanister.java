package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.Fluids.CD_Canister;
import com.hbm.items.IColoredItem;
import com.hbm.items.ItemBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * The canned fluid. meta is the fluid id; a base image plus a white mask tinted by the fluid's registered can color.
 * Only fluids "registered as can containers" get subtypes — not every fluid can be canned.
 */
public class ItemCanister extends ItemBase implements IColoredItem {

	public ItemCanister() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		FluidType[] order = Fluids.getInNiceOrder();

		for(int i = 1; i < order.length; i++) {
			FluidType type = order[i];
			if(type.getContainer(CD_Canister.class) != null) list.add(new ItemStack(this, 1, type.getID()));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		String base = I18nUtil.resolveKey(this.getUnlocalizedName() + ".name").trim();

		// fromID returns null while the fluid table is uninitialized; StatList.reinit calls this once per item, so fall back to the base name
		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type == null) return base;

		return base + " " + I18nUtil.resolveKey(type.getConditionalName()).trim();
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {

		if(tintIndex != 1) return 0xFFFFFF;

		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type == null) return 0xFFFFFF;

		CD_Canister canister = type.getContainer(CD_Canister.class);
		int color = canister == null ? -1 : canister.color;

		return color < 0 ? 0xFFFFFF : color;
	}
}
