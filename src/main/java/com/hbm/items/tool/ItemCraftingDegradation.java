package com.hbm.items.tool;

import com.hbm.items.ItemBase;

import net.minecraft.item.ItemStack;

/**
 * Tool-ish items that wear as they work. When a crafting step spends them, they stay in the slot and keep grinding,
 * so `hasContainerItem` is always true and `getContainerItem` returns the stack itself with a bit of damage added.
 */
public class ItemCraftingDegradation extends ItemBase {

	public ItemCraftingDegradation(int durability) {
		this.setMaxStackSize(1);
		this.setMaxDamage(durability);
		this.setNoRepair();
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {

		if(this.getMaxDamage() > 0) {
			stack.setItemDamage(stack.getItemDamage() + 1);
		}

		return stack;
	}
}
