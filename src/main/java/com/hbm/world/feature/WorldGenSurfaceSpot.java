package com.hbm.world.feature;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

/**
 * A surface blotch: swaps dirt/grass within a radius for a given block by probability — the "ring of discoloration" for pollution spread.
 */
public class WorldGenSurfaceSpot extends WorldGenerator {

	private Block block;
	private int radius;
	private float chance;

	public WorldGenSurfaceSpot(Block block, int radius, float chance) {
		this.block = block;
		this.radius = radius;
		this.chance = chance;
	}

	// WorldGenerator only has the BlockPos form in this version; the old (x,y,z) overload is gone, so callers hand in the point directly
	@Override
	public boolean generate(World world, Random rand, BlockPos anchor) {

		int x = anchor.getX();
		int y = anchor.getY();
		int z = anchor.getZ();

		int r = rand.nextInt(this.radius - 2) + 2;
		byte depth = 2;

		IBlockState replacement = this.block.getDefaultState();

		for(int iX = x - r; iX <= x + r; ++iX) {
			for(int iZ = z - r; iZ <= z + r; ++iZ) {
				int k1 = iX - x;
				int l1 = iZ - z;

				if(k1 * k1 + l1 * l1 <= r * r) {
					for(int iY = y - depth; iY <= y + depth; ++iY) {
						BlockPos pos = new BlockPos(iX, iY, iZ);
						IBlockState state = world.getBlockState(pos);

						if(state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS) {
							if(rand.nextFloat() < this.chance) world.setBlockState(pos, replacement, 2);
						}
					}
				}
			}
		}

		return true;
	}
}
