package com.hbm.tileentity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IOverpressurable {

	public void explode(World world, BlockPos pos);
}
