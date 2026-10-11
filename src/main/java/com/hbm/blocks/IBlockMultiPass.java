package com.hbm.blocks;

/**
 * The RenderBlocks pass mechanism that drew a block twice is gone in 1.12.2,
 * so the old way of grabbing a render ID via RenderingRegistry.getNextAvailableRenderId() has to go.
 * This interface now only hints at custom renderers: how many layers this block needs, and whether the item form draws too.
 */
public interface IBlockMultiPass {

	public int getPasses();

	public default boolean shouldRenderItemMulti() {
		return false;
	}
}
