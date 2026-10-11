package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class BlockMutatorFire implements IBlockMutator {

	@Override public void mutatePre(ExplosionVNT explosion, Block block, int meta, int x, int y, int z) { }

	@Override
	public void mutatePost(ExplosionVNT explosion, int x, int y, int z) {

		BlockPos pos = new BlockPos(x, y, z);
		IBlockState state = explosion.world.getBlockState(pos);
		IBlockState below = explosion.world.getBlockState(pos.down());
		if(state.getMaterial() == Material.AIR && below.getBlock().isOpaqueCube(below) && explosion.world.rand.nextInt(3) == 0) {
			explosion.world.setBlockState(pos, Blocks.FIRE.getDefaultState());
		}
	}
}
