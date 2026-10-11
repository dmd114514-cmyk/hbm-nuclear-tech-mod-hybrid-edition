package com.hbm.tileentity.network;

import net.minecraft.util.math.BlockPos;

/**
 * A tile entity the drone linker can target (the same-named 1.7.10 interface).
 *
 * This version uses the real `BlockPos`: the old `com.hbm.util.fauxpointtwelve.BlockPos` was a 1.7.10-era fake-coordinate shim,
 * and this version's `world.getTileEntity(BlockPos)` family takes the real type; keeping the shim would just make call sites pass it around.
 */
public interface IDroneLinkable {

	public BlockPos getPoint();

	public void setNextTarget(int x, int y, int z);
}
