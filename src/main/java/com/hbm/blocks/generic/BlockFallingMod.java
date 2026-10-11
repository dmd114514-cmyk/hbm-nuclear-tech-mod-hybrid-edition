package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * The fallible blocks (sand, gravel, scrap and friends).
 *
 * All logic lives in vanilla's `BlockFalling`; this only adds the two chained doors this version can't reach:
 * step sounds became the protected `setSoundType` in 1.12.2 (reachable only from subclasses),
 * while texture names belong to JSON models, so `setBlockTextureName` only absorbs the writing, keeping "which texture" in code;
 * naming rides `IBlockNaming`'s default method — single inheritance is taken by BlockFalling here.
 */
public class BlockFallingMod extends BlockFalling implements IBlockNaming {

	public BlockFallingMod(Material material) {
		super(material);
	}

	/** Textures belong to models; this only absorbs the old chained writing */
	public BlockFallingMod setBlockTextureName(String name) {
		return this;
	}

	public BlockFallingMod setStepSound(SoundType type) {
		setSoundType(type);
		return this;
	}
}
