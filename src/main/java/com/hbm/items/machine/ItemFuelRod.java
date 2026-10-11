package com.hbm.items.machine;

import com.hbm.items.ItemBase;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * The fuel rod base. Lifespan rides NBT's `life` key; no tag yet counts as 0 —
 * so "freshly made" and "fully spent" look identical on the durability bar. That's vanilla behavior.
 */
public class ItemFuelRod extends ItemBase {

	public int lifeTime;

	public ItemFuelRod(int life) {
		this.lifeTime = life;
		this.canRepair = false;
	}

	public static void setLifeTime(ItemStack stack, int time) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("life", time);
	}

	public static int getLifeTime(ItemStack stack) {

		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getInteger("life");
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) getLifeTime(stack) / (double) ((ItemFuelRod) stack.getItem()).lifeTime;
	}
}
