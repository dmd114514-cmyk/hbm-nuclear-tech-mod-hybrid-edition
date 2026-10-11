package com.hbm.blocks.generic;

import com.hbm.blocks.IEnumMulti;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** concrete_brick_slab's slab subtypes (values are the base block names, ordered like the constructor's base list) */
public class BlockSlabConcreteBrick extends BlockMultiSlab {

	public enum Variant implements IEnumMulti {
		BRICK_CONCRETE,
		BRICK_CONCRETE_MOSSY,
		BRICK_CONCRETE_CRACKED,
		BRICK_CONCRETE_BROKEN,
		BRICK_DUCRETE
	}

	public BlockSlabConcreteBrick(boolean isDouble, Block single, Block... materials) {
		super(isDouble, single, Material.ROCK, materials);
	}

	@Override
	protected Class<? extends Enum<?>> variantEnum() {
		return Variant.class;
	}
}
