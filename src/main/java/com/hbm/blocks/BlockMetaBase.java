package com.hbm.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

/**
 * The common layer for blocks carrying subtype/facing meta: in 1.12.2 meta must become a block property to survive,
 * without one `getStateFromMeta(anything)` always returns the same state and 16 tiers silently collapse to 1.
 * The three boilerplate pieces (property, state container, round-trip) collect here; subclasses write business only.
 *
 * By the way: the `normal` entry in a blockstate JSON only covers blocks **without properties**;
 * blocks extending this class must list `meta=0..15` value by value; the generator already recognizes this class down the inheritance chain.
 */
public abstract class BlockMetaBase extends BlockContainerBase {

	/*Where the old 4-bit meta lives now */
	public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);

	protected BlockMetaBase(Material material) {
		super(material);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(META, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}
}
