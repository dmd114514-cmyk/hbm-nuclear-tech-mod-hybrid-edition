package com.hbm.render.block.ct;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * The door connective-texture blocks must implement.
 *
 * The old door carried three things: getFragments (handing out 20 IIcons), canConnect, and the block's own primeReceiver line inside
 * registerBlockIcons. This version, the latter two have nowhere to land — textures are no longer registered by blocks,
 * they're named by the model in IModel#getTextures and fragment rects are computed live by CTFragment, so the block side is left with just "should I connect to my neighbor"
 * — one judgment — and the door narrows down to canConnect alone.
 *
 * Wiring a block up to connected textures takes three steps (all three in the block class; the model side never touched):
 * 1. implements IBlockCT; override canConnect when you need non-default connectivity;
 * 2. override createBlockState() → return CTContext.container(this, super.createBlockState());
 * 3. override getExtendedState(state, world, pos) → return CTContext.attach(world, pos, state);
 * The latter two exist to pass world coords to the renderer; skipping them doesn't crash, it just falls back to the plain "nothing connects" look.
 * The look is enabled from the blockstate JSON: that tier's model written as hbm:ct/blocks/&lt;base texture name&gt;.
 */
public interface IBlockCT {

	/**
	 * Whether the neighbor cell should join me in one piece.
	 *
	 * @param world       the render-time world read door
	 * @param selfState   the state of the cell being drawn (with meta; families split by tier should look at this)
	 * @param selfPos     the cell being drawn
	 * @param neighborPos the neighbor being asked; the implementation fetches neighbor block and state from world itself
	 */
	public default boolean canConnect(IBlockAccess world, IBlockState selfState, BlockPos selfPos, BlockPos neighborPos) {
		return selfState.getBlock() == world.getBlockState(neighborPos).getBlock();
	}
}
