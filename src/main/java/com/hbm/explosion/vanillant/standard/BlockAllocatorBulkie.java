package com.hbm.explosion.vanillant.standard;

import java.util.HashSet;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockAllocator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockAllocatorBulkie implements IBlockAllocator {

	protected double maximum;
	protected int resolution;

	public BlockAllocatorBulkie(double maximum) {
		this(maximum, 16);
	}

	public BlockAllocatorBulkie(double maximum, int resolution) {
		this.resolution = resolution;
		this.maximum = maximum;
	}

	@Override
	public HashSet<BlockPos> allocate(ExplosionVNT explosion, World world, double x, double y, double z, float size) {

		HashSet<BlockPos> affectedBlocks = new HashSet<BlockPos>();

		for(int i = 0; i < this.resolution; ++i) {
			for(int j = 0; j < this.resolution; ++j) {
				for(int k = 0; k < this.resolution; ++k) {

					if(i == 0 || i == this.resolution - 1 || j == 0 || j == this.resolution - 1 || k == 0 || k == this.resolution - 1) {

						double d0 = (double) ((float) i / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
						double d1 = (double) ((float) j / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
						double d2 = (double) ((float) k / ((float) this.resolution - 1.0F) * 2.0F - 1.0F);
						double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

						d0 /= d3;
						d1 /= d3;
						d2 /= d3;

						double currentX = x;
						double currentY = y;
						double currentZ = z;

						double dist = 0;

						//doesn't decay by remaining power — punches straight to max radius, hard blocks just cut it short early
						for(float stepSize = 0.3F; dist <= explosion.size;) {

							double deltaX = currentX - x;
							double deltaY = currentY - y;
							double deltaZ = currentZ - z;
							dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

							BlockPos pos = new BlockPos(MathHelper.floor(currentX), MathHelper.floor(currentY), MathHelper.floor(currentZ));
							IBlockState state = world.getBlockState(pos);
							Block block = state.getBlock();

							if(state.getMaterial() != Material.AIR) {
								float blockResistance = explosion.exploder != null ? explosion.exploder.getExplosionResistance(explosion.compat, world, pos, state) : block.getExplosionResistance(world, pos, explosion.exploder, explosion.compat);
								if(this.maximum < blockResistance) {
									break;
								}
							}

							if(explosion.exploder == null || explosion.exploder.canExplosionDestroyBlock(explosion.compat, world, pos, state, explosion.size)) {
								affectedBlocks.add(pos);
							}

							currentX += d0 * (double) stepSize;
							currentY += d1 * (double) stepSize;
							currentZ += d2 * (double) stepSize;
						}
					}
				}
			}
		}

		return affectedBlocks;
	}
}
