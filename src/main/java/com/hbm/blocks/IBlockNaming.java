package com.hbm.blocks;

import com.hbm.lib.RefStrings;

import net.minecraft.block.Block;

/**
 * Adds chained naming to block classes.
 * 1.12.2 has no setBlockName, and not every block derives from BlockBase — some extend vanilla
 * BlockStone/BlockLadder directly, where single inheritance blocks a common base; hence naming as an interface default method.
 *
 * **The registry name is not set here**: HbmRegistry.block(block, name) owns that.
 * Setting it here too would trip 1.12.2's setRegistryName second-call exception (it's set-once).
 * Translation keys also get no modid prefix, so the old tile.xxx.name lang reuses as-is.
 */
public interface IBlockNaming {

	default Block setBlockName(String name) {
		Block self = (Block) this;
		self.setUnlocalizedName(name);
		return self;
	}
}
