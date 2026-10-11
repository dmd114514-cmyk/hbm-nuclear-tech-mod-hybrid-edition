package com.hbm.world.gen.nbt.selector;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.StructureComponent.BlockSelector;

/** Shared base for the biome-swapped surface/filler selectors: feed the target biome in before building */
public abstract class BiomeBlockSelector extends BlockSelector {

	public Biome nextBiome;

}
