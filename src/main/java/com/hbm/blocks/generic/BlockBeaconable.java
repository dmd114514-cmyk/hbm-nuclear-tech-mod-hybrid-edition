package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;

import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockBeaconable extends BlockBase {

	public BlockBeaconable(Material mat) {
		super(mat);
	}

	@Override
	public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beaconPos) {
		return true;
	}
}
