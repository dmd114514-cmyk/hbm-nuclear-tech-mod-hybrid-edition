package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.BlockLadder;

public class BlockNTMLadder extends BlockLadder implements IBlockNaming {

	public BlockNTMLadder() {
		super();
	}

	/**
	 * Same as BlockBase's: 1.12.2 gives textures to JSON models; this only absorbs the old chained writing, keeping "which texture" in code
	 * Ladders ride vanilla's BlockLadder line and can't reach BlockBase's version, so a local one fills in;
	 * the return type must stay this class, or the next setBlockName (an interface default method) is unreachable.
	 */
	public BlockNTMLadder setBlockTextureName(String name) {
		return this;
	}
}
