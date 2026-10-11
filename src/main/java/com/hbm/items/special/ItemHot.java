package com.hbm.items.special;

import com.hbm.items.ItemBase;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Hot ingots. Fresh-from-the-furnace items carry residual heat in NBT, bleeding a little every tick; when it's gone the tag comes off.
 *
 * Vanilla kept the heat cap on a static field, so "whichever item was constructed last" would overwrite all of its kind —
 * a 200-degree meteoric iron ingot at the smithing table becomes 100. Reading per item instance here matches the original intent.
 */
public class ItemHot extends ItemBase {

	protected int heat;

	public ItemHot(int heat) {
		this.heat = heat;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {

		if(!world.isRemote && stack.hasTagCompound()) {

			int h = stack.getTagCompound().getInteger("heat");

			if(h > 0) {
				stack.getTagCompound().setInteger("heat", h - 1);
			} else {
				stack.setTagCompound(null);
			}
		}
	}

	public static ItemStack heatUp(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot)) return stack;

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", getMaxHeat(stack));
		return stack;
	}

	public static ItemStack heatUp(ItemStack stack, double d) {

		if(!(stack.getItem() instanceof ItemHot)) return stack;

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("heat", (int) (d * getMaxHeat(stack)));
		return stack;
	}

	public static double getHeat(ItemStack stack) {

		if(!(stack.getItem() instanceof ItemHot)) return 0;
		if(!stack.hasTagCompound()) return 0;

		int h = stack.getTagCompound().getInteger("heat");
		return (double) h / (double) getMaxHeat(stack);
	}

	public static int getMaxHeat(ItemStack stack) {
		return stack.getItem() instanceof ItemHot ? ((ItemHot) stack.getItem()).getHeatCapacity(stack) : 0;
	}

	/** Subclasses override this to set "how many degrees this rod can hold" */
	protected int getHeatCapacity(ItemStack stack) {
		return this.heat;
	}
}
