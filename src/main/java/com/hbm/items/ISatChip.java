package com.hbm.items;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface ISatChip {

	public static int getFreqS(ItemStack stack) {
		if(!stack.isEmpty() && stack.getItem() instanceof ISatChip) {
			return ((ISatChip) stack.getItem()).getFreq(stack);
		}

		return 0;
	}

	public static void setFreqS(ItemStack stack, int freq) {
		if(!stack.isEmpty() && stack.getItem() instanceof ISatChip) {
			((ISatChip) stack.getItem()).setFreq(stack, freq);
		}
	}

	public default int getFreq(ItemStack stack) {
		// 1.12.2 no longer allows touching the stackTagCompound field directly; go through the getter/setter
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}
		return stack.getTagCompound().getInteger("freq");
	}

	public default void setFreq(ItemStack stack, int freq) {
		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("freq", freq);
	}
}
