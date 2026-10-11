package com.hbm.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * For proxy blocks: tells TileEntityProxyBase where its core is.
 * BlockDummyable doesn't implement this — its own core-finding logic already covers it.
 */
public interface IProxyController {

	public TileEntity getCore(World world, BlockPos pos);
}
