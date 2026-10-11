package com.hbm.world.gen.nbt.selector;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

/** Brick. Red brick, everywhere. */
public class BrickSelector extends BlockSelector {

	@Override
	public void selectBlocks(Random rand, int x, int y, int z, boolean notInterior) {
		this.blockstate = Blocks.BRICK_BLOCK.getDefaultState();
	}

}
