package com.hbm.world.gen.nbt.selector;

import java.util.Random;

/** Structure filler uses the target biome's filler block (the old version filled fillerBlock here) */
public class BiomeFillerSelector extends BiomeBlockSelector {

	@Override
	public void selectBlocks(Random rand, int x, int y, int z, boolean notInterior) {
		this.blockstate = this.nextBiome.fillerBlock;
	}

}
