package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/** The pin-coded key. The pin code rides NBT; the lock only opens on a match. Fakes can't be recoded or copied. */
public class ItemKeyPin extends ItemBase {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(getPins(stack) != 0) {
			list.add("Pin configuration: " + getPins(stack));
		} else {
			list.add("Pins not set!");
		}

		if(this == ModItems.key_fake) {
			list.add("");
			list.add("Pins can neither be changed, nor copied.");
		}
	}

	public static int getPins(ItemStack stack) {

		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getInteger("pins");
	}

	public static void setPins(ItemStack stack, int i) {

		if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("pins", i);
	}

	public boolean canTransfer() {
		return this != ModItems.key_fake;
	}
}
