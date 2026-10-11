package com.hbm.blocks.generic;

import com.hbm.blocks.IEnumMulti;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/** concrete_slab's slab subtypes (values are the base block names, ordered like the constructor's base list) */
public class BlockSlabConcrete extends BlockMultiSlab {

	public enum Variant implements IEnumMulti {
		CONCRETE_SMOOTH,
		CONCRETE,
		CONCRETE_ASBESTOS,
		DUCRETE_SMOOTH,
		DUCRETE,
		ASPHALT
	}

	public BlockSlabConcrete(boolean isDouble, Block single, Block... materials) {
		super(isDouble, single, Material.ROCK, materials);
	}

	@Override
	protected Class<? extends Enum<?>> variantEnum() {
		return Variant.class;
	}
}
