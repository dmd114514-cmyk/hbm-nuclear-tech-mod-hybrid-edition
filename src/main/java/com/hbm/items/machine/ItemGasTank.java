package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.Fluids.CD_Gastank;
import com.hbm.items.IColoredItem;
import com.hbm.items.ItemBase;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * The steel bottle. One layer more than the cell: base image, bottle color and label color, all three tinted separately.
 * Also picking up only fluids registered as bottle containers.
 */
public class ItemGasTank extends ItemBase implements IColoredItem {

	public ItemGasTank() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		FluidType[] order = Fluids.getInNiceOrder();

		for(int i = 1; i < order.length; i++) {
			FluidType type = order[i];
			if(type.getContainer(CD_Gastank.class) != null) list.add(new ItemStack(this, 1, type.getID()));
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

		if(tintIndex == 0) return 0xFFFFFF;

		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type == null) return 0xFFFFFF;

		CD_Gastank tank = type.getContainer(CD_Gastank.class);
		if(tank == null) return 0xFFFFFF;

		return tintIndex == 1 ? tank.bottleColor : tank.labelColor;
	}
}
