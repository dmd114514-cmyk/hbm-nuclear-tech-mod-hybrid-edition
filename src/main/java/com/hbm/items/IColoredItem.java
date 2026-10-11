package com.hbm.items;

import net.minecraft.item.ItemStack;

/**
 * The item-tinting convention.
 *
 * 1.12.2 deleted `Item.getColorFromItemStack`; colors now come from client-side `ItemColors` registered callbacks,
 * and this gives one landing spot so items don't each hand-roll registration.
 * tintIndex is the number declared by the model's layer; the -1 semantics for un-tinted layers are the caller's convention.
 */
public interface IColoredItem {

	int getColor(ItemStack stack, int tintIndex);
}
