package com.hbm.blocks.generic;

import com.hbm.blocks.IEnumMulti;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** brick_slab's slab subtypes (values are the base block names, ordered like the constructor's base list) */
public class BlockSlabBrick extends BlockMultiSlab {

	public enum Variant implements IEnumMulti {
		REINFORCED_STONE,
		REINFORCED_BRICK,
		BRICK_OBSIDIAN,
		BRICK_LIGHT,
		BRICK_COMPOUND,
		BRICK_ASBESTOS,
		BRICK_FIRE
	}

	public BlockSlabBrick(boolean isDouble, Block single, Block... materials) {
		super(isDouble, single, Material.ROCK, materials);
	}

	@Override
	protected Class<? extends Enum<?>> variantEnum() {
		return Variant.class;
	}
}
