package com.hbm.blocks.network;

import com.hbm.inventory.fluid.FluidType;

import net.minecraft.world.World;

/**
 * The fluid duct family's "type changes spread to neighbors" port, signature matching the same-named 1.7.10 interface.
 *
 * Not a duplicate of §16's capability neighbor propagation: `CapabilityFluidProvider` answers "can fluid be pushed into the far end",
 * a point-to-point question; this port answers "which fluid the whole network carries has changed" and needs to recurse with remaining hops,
 * `loopsRemaining` being the old version's infinite-recursion gate (starts at 64, minus one per level).
 * Consumers = the duct family's blocks and TEs, the batch of this face not yet landed.
 */
public interface IBlockFluidDuct {

	public void changeTypeRecursively(World world, int x, int y, int z, FluidType prevType, FluidType type, int loopsRemaining);
}
