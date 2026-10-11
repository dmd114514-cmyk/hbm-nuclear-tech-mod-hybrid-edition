package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IColoredItem;
import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * The fluid-filled cells/barrels/bags. meta is directly the fluid id, hence hundreds of subtypes and no enum possible;
 * the look is one base image plus a white mask tinted by fluid color.
 *
 * The lead cell only holds fluids wanting lead containment (the strongly radioactive few); other fluids don't enter its subtypes.
 */
public class ItemFluidTank extends ItemBase implements IColoredItem {

	public ItemFluidTank() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		FluidType[] order = Fluids.getInNiceOrder();

		for(int i = 1; i < order.length; i++) {
			FluidType type = order[i];

			if(type.hasNoContainer()) continue;

			if(type.needsLeadContainer()) {
				if(this == ModItems.fluid_tank_lead_full) list.add(new ItemStack(this, 1, type.getID()));
			} else {
				list.add(new ItemStack(this, 1, type.getID()));
			}
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		// fromID returns null — NONE included — while the fluid table is uninitialized. StatList.reinit
		// fetches display names once per item (meta=0 included), and a crash here is a startup crash, so fall back to the base name.
		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type == null) return I18nUtil.resolveKey(this.getUnlocalizedName() + ".name").trim();

		String fluid = I18nUtil.resolveKey(type.getConditionalName()).trim();
		return I18nUtil.resolveKey(this.getUnlocalizedName() + ".name", fluid).trim();
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {

		if(tintIndex != 1) return 0xFFFFFF;

		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type == null) return 0xFFFFFF;

		int color = type.getColor();
		return color < 0 ? 0xFFFFFF : color;
	}
}
