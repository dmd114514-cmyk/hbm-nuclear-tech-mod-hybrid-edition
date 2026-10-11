package com.hbm.render.item;

import net.minecraft.item.Item;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;

/**
 * "My block renderer also happens to handle its own item's look."
 *
 * The old same-named interface returned an {@code IItemRenderer}, whose callbacks are deleted in 1.12.2; this version's in-item dynamic rendering
 * belongs to {@link TileEntityItemStackRenderer}, so the semantics stay and only the return type swaps.
 * Registration goes through {@link HbmItemRenderers} uniformly — don't go touching ClientProxy on your own; the old registerItemRenderer ring was
 * one several-hundred-line method everyone wanted to shove things into.
 */
public interface IItemRendererProvider {

	Item getItemForRenderer();

	public default Item[] getItemsForRenderer() {
		return new Item[] { this.getItemForRenderer() };
	}

	TileEntityItemStackRenderer getRenderer();
}
