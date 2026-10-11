package com.hbm.world.gen.nbt.selector;

import java.util.Random;

/** Structure surface layers use the target biome's top block, so a buried ruin's roof meets the surrounding terrain */
public class BiomeTopSelector extends BiomeBlockSelector {

	@Override
	public void selectBlocks(Random rand, int x, int y, int z, boolean notInterior) {
		this.blockstate = this.nextBiome.topBlock;
	}

}
