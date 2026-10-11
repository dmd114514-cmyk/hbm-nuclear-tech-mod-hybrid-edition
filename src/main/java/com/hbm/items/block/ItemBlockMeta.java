package com.hbm.items.block;

import com.hbm.items.ItemBlockBase;

import net.minecraft.block.Block;

/**
 * A block item with subtypes.
 * Note 1.12.2's ItemBlock no longer carries a getMetadata override; you have to fall back to Item.getMetadata(int),
 * so meta is forwarded explicitly here for subtypes to land on the right block state.
 */
public class ItemBlockMeta extends ItemBlockBase {

	public ItemBlockMeta(Block block) {
		super(block);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}
}
