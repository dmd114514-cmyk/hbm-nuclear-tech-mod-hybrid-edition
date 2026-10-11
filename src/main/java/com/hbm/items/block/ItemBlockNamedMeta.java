package com.hbm.items.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockNamedMeta extends ItemBlockMeta {

	public ItemBlockNamedMeta(Block block) {
		super(block);
	}

	// Deliberately not following the IBlockMulti enum-name route; the raw meta number becomes the suffix,
	// matching the existing tile.<name>.<number>.name lang entries
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getBlock().getUnlocalizedName() + "." + stack.getMetadata();
	}

}
