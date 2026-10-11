package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;

/**
 * Generic stairs: shape, facing, half height and connection shape (inner/outer corners) all live in vanilla's `BlockStairs`,
 * and no icon registration either — the bottom/top/side textures come from `models/block/<name>{,_inner,_outer}.json`,
 * parented to vanilla's `block/stairs` / `block/inner_stairs` / `block/outer_stairs` respectively,
 * with blockstates listing all forty facing×half×shape tiers per the vanilla stairs template.
 *
 * This thin layer keeps three chained doors: naming via `IBlockNaming`, plus the texture and step-sound absorbers (textures belong to models here and
 * `setSoundType` is protected).
 *
 * ⚠ The old constructor's `recipeGen.add(...)` (the auto stairs-recipe list) didn't come over: that tier belongs to the recipe side;
 * this batch lands blocks and assets only — don't book them as "recipes ready".
 */
public class BlockGenericStairs extends BlockStairs implements IBlockNaming {

	public BlockGenericStairs(Block base, int meta) {
		super(base.getStateFromMeta(meta));
		this.useNeighborBrightness = true;
	}

	/** Textures belong to models; this only absorbs the old chained writing */
	public BlockGenericStairs setBlockTextureName(String name) {
		return this;
	}

	public BlockGenericStairs setStepSound(SoundType type) {
		setSoundType(type);
		return this;
	}
}
