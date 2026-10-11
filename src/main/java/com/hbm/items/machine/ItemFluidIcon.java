package com.hbm.items.machine;

import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IColoredItem;
import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The fluid "icon" item — not a container, just a label carrying amount/pressure, a stand-in for recipes and the manual.
 * meta is the fluid id; the look is a mask tinted by the fluid's color.
 */
public class ItemFluidIcon extends ItemBase implements IColoredItem {

	public ItemFluidIcon() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		FluidType[] order = Fluids.getInNiceOrder();
		for(int i = 1; i < order.length; i++) {
			list.add(new ItemStack(this, 1, order[i].getID()));
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(stack.hasTagCompound()) {

			if(getQuantity(stack) > 0) {
				list.add(getQuantity(stack) + "mB");
			} else if(getAtmospheres(stack) > 0) {
				list.add(BobMathUtil.roundDecimal(getAtmospheres(stack), 3) + "atm");
			}

			if(getPressure(stack) > 0) {
				list.add(TextFormatting.RED + "" + getPressure(stack) + "PU");
				list.add((BobMathUtil.getBlink() ? TextFormatting.RED : TextFormatting.DARK_RED) + "Pressurized, use compressor!");
			}
		}

		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type != null) type.addInfo(list);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type == null) return "Unknown";

		return I18nUtil.resolveKey(type.getConditionalName()).trim();
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {

		FluidType type = Fluids.fromID(stack.getItemDamage());
		if(type == null) return 0xFFFFFF;

		int color = type.getColor();
		return color < 0 ? 0xFFFFFF : color;
	}

	public static ItemStack addQuantity(ItemStack stack, int i) {

		if(i <= 0) return stack;

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("fill", i);
		return stack;
	}

	public static ItemStack addPressure(ItemStack stack, int i) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("pressure", i);
		return stack;
	}

	public static ItemStack addAtmospheres(ItemStack stack, double atm) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setDouble("atm", atm);
		return stack;
	}

	public static ItemStack make(FluidStack stack) {
		return make(stack.type, stack.fill, stack.pressure);
	}

	public static ItemStack make(FluidType fluid, int i) {
		return make(fluid, i, 0);
	}

	public static ItemStack make(FluidType fluid, int i, int pressure) {
		// vanilla called fluid.ordinal() here, but that method was already a @Deprecated alias of getID() in 1.7.10; gone now
		return addPressure(addQuantity(new ItemStack(ModItems.fluid_icon, 1, fluid.getID()), i), pressure);
	}

	public static ItemStack make(FluidType fluid, double atm) {
		return addAtmospheres(new ItemStack(ModItems.fluid_icon, 1, fluid.getID()), atm);
	}

	public static int getQuantity(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("fill");
	}

	public static int getPressure(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("pressure");
	}

	public static double getAtmospheres(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getDouble("atm");
	}
}
