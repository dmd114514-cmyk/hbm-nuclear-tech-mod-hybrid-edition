package com.hbm.world.feature;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * A 3D ore layer: three noise fields multiplied together decide which stone becomes the target block — reads as a slanted vein, not scattered dots.
 *
 * It registers itself on the event bus in its constructor, so whoever news it up activates it (same as the old version).
 * Two relocation notes:
 *   · `ChunkCoordIntPair` became `ChunkPos` here; the decoration event's position also merged into one BlockPos (used as "the chunk's corner").
 *   · The old version filled `cacheZ` but never read it — both vertical lookups used `cacheX`. Copied as-is; don't "fix" it in passing:
 *     change it and the vein shapes change, and that's not a porting concern.
 */
public class OreLayer3D {

	public static int counter = 0;
	public int id;

	long lastSeed;
	NoiseGeneratorPerlin noiseX;
	NoiseGeneratorPerlin noiseY;
	NoiseGeneratorPerlin noiseZ;
	double[][] cacheX;
	double[][] cacheZ;

	double scaleH;
	double scaleV;
	double threshold;

	Block block;
	int meta;
	int dim = 0;

	Map<Integer, Set<ChunkPos>> alreadyDecorated = new HashMap<Integer, Set<ChunkPos>>();

	public OreLayer3D(Block block, int meta) {
		this.block = block;
		this.meta = meta;
		MinecraftForge.EVENT_BUS.register(this);
		this.id = counter;
		counter++;
	}

	public OreLayer3D setDimension(int dim) {
		this.dim = dim;
		return this;
	}

	public OreLayer3D setScaleH(double scale) {
		this.scaleH = scale;
		return this;
	}

	public OreLayer3D setScaleV(double scale) {
		this.scaleV = scale;
		return this;
	}

	public OreLayer3D setThreshold(double threshold) {
		this.threshold = threshold;
		return this;
	}

	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {

		World world = event.getWorld();
		BlockPos anchor = event.getPos();
		int cx = anchor.getX();
		int cz = anchor.getZ();

		if(world.provider == null || world.provider.getDimension() != this.dim) return;

		ChunkPos chunkPos = new ChunkPos(cx >> 4, cz >> 4);
		Set<ChunkPos> decoratedChunks = alreadyDecorated.computeIfAbsent(world.provider.getDimension(), n -> new HashSet<ChunkPos>());

		// Stop early if we've already generated this chunk in this dimension
		if(decoratedChunks.contains(chunkPos)) return;
		decoratedChunks.add(chunkPos);

		// refresh caches on first run and if the world seed changes
		if(this.noiseX == null || world.getSeed() != lastSeed) {
			this.noiseX = new NoiseGeneratorPerlin(new Random(world.getSeed() + 101 + id), 4);
			this.noiseY = new NoiseGeneratorPerlin(new Random(world.getSeed() + 102 + id), 4);
			this.noiseZ = new NoiseGeneratorPerlin(new Random(world.getSeed() + 103 + id), 4);
			cacheX = new double[16][65];
			cacheZ = new double[16][65];
			lastSeed = world.getSeed();
		}

		for(int o = 0; o < 16; o++) {
			for(int y = 64; y > 5; y--) {
				cacheX[o][y] = this.noiseX.getValue(y * scaleV, (cz + 8 + o) * scaleH);
				cacheZ[o][y] = this.noiseZ.getValue((cx + 8 + o) * scaleH, y * scaleV);
			}
		}

		IBlockState placement = this.block.getStateFromMeta(this.meta);

		for(int ox = 0; ox < 16; ox++) {
			int x = cx + 8 + ox;

			for(int oz = 0; oz < 16; oz++) {
				int z = cz + 8 + oz;

				double ny = this.noiseY.getValue(x * scaleH, z * scaleH);

				for(int y = 64; y > 5; y--) {
					double nx = cacheX[oz][y];
					double nz = cacheX[ox][y];

					if(nx * ny * nz > threshold) {
						BlockPos pos = new BlockPos(x, y, z);
						IBlockState target = world.getBlockState(pos);

						if(target.getBlock().isNormalCube(target)
								&& target.getBlock().isReplaceableOreGen(target, world, pos, Blocks.STONE.getDefaultState()::equals)) {
							world.setBlockState(pos, placement, 2);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onWorldLoad(WorldEvent.Load event) {
		alreadyDecorated.put(event.getWorld().provider.getDimension(), new HashSet<ChunkPos>());
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		alreadyDecorated.remove(event.getWorld().provider.getDimension());
	}

}
