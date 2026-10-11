package com.hbm.items.machine;

import com.hbm.items.IMultiModelItem;
import com.hbm.items.ItemBase;

import net.minecraft.item.ItemStack;

/**
 * The large gear. Two metas: plain and hardened steel, sharing one texture, told apart by name only,
 * hence one model.
 */
public class ItemGear extends ItemBase implements IMultiModelItem {

	public ItemGear() {
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	public String[] subtypeModels() {
		String name = this.getRegistryName().getResourcePath();
		return new String[] { name, name };
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName() + (stack.getItemDamage() == 1 ? "_steel" : "");
	}
}
