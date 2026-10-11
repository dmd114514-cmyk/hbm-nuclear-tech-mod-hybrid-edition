package com.hbm.world.gen.terrain;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;

/**
 * Meteorite craters: scoops a bowl out of the target biome group, float dirt on the floor, bedrock on the walls.
 *
 * The old version indexed straight into the `Block[]` handed down by `MapGenBase` via (bx*16+bz)*256+y; that side-array is gone here,
 * replaced by ChunkPrimer's per-coordinate read/write. The coordinates mean the same: the base loop takes "the chunk being generated" as origin
 * and walks every candidate crater center within ±range (both decompiled generates have this shape),
 * so the offset is (target chunk - candidate center chunk) and the crater radius doesn't care about chunk borders.
 */
public class MapGenCrater extends MapGenBase {

	private int frequency = 100;
	private int minSize = 8;
	private int maxSize = 64;

	public IBlockState regolith;
	public IBlockState rock;

	public Biome targetBiome;

	public MapGenCrater(int frequency) {
		this.frequency = frequency;
	}

	public void setSize(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;

		this.range = (maxSize / 8) + 1;
	}

	private double depthFunc(double x, double rad, double depth) {
		return -Math.pow(x, 2) / Math.pow(rad, 2) * depth + depth;
	}

	// The base calls this once per candidate center within ±range; the primer is always the chunk currently being generated
	@Override
	protected void recursiveGenerate(World world, int craterX, int craterZ, int chunkX, int chunkZ, ChunkPrimer primer) {

		if(rand.nextInt(frequency) != 0) return;
		if(targetBiome != null && targetBiome != world.getBiome(new BlockPos(craterX * 16, 64, craterZ * 16))) return;

		int xCoord = chunkX - craterX;
		int zCoord = chunkZ - craterZ;

		double radius = rand.nextInt(maxSize - minSize) + minSize;
		double depth = radius * 0.35D;

		for(int bx = 15; bx >= 0; bx--) { // bx, bz are column coordinates inside this primer
			for(int bz = 15; bz >= 0; bz--) {
				for(int y = 127; y >= 0; y--) {

					IBlockState state = primer.getBlockState(bx, y, bz);
					if(state.getBlock() == Blocks.AIR) continue;

					if(state.getBlock().isOpaqueCube(state) || state.getMaterial().isLiquid()) {
						// re-base to crater-center coordinates, or the radius won't be a circle
						int x = xCoord * 16 + bx;
						int z = zCoord * 16 + bz;

						double r = Math.sqrt(x * x + z * z);

						if(r - rand.nextInt(3) <= radius) {
							// dig down by the bowl curve to the right depth
							int dep = (int) MathHelper.clamp(depthFunc(r, radius, depth), 0, y - 1);
							for(int i = 0; i < dep; i++) {
								primer.setBlockState(bx, y - i, bz, Blocks.AIR.getDefaultState());
							}

							y -= dep;

							dep = Math.min(3, y - 1);

							// float dirt on the crater floor, bedrock one ring out
							IBlockState fill = (r + rand.nextInt(3) <= radius / 3D) ? regolith : rock;
							for(int i = 0; i < dep; i++) {
								primer.setBlockState(bx, y - i, bz, fill);
							}

							break;
						}
					}
				}
			}
		}
	}

}
