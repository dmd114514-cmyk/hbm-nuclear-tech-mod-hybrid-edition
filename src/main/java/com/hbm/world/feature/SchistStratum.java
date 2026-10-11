package com.hbm.world.feature;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Gneiss strata: terrain noise draws a few horizontal bands around y=30, turning stone into gneiss.
 *
 * The old version got its chunk position from `event.chunkX/chunkZ`; here the event carries one `BlockPos` (used as "the chunk's corner";
 * this reading can't be pinned down statically — one decorated-world run confirms it; off by half a chunk at worst, the bands just sit slightly aside).
 *
 * Not wired yet: the old version hung it with `MinecraftForge.EVENT_BUS.register(new SchistStratum())` inside `MainRegistry`,
 * which lives in the other side's init — until that lands, this class's listener never fires.
 */
public class SchistStratum {

	NoiseGeneratorPerlin noise;

	@SubscribeEvent
	public void onDecorate(DecorateBiomeEvent.Pre event) {

		World world = event.getWorld();

		if(this.noise == null) {
			this.noise = new NoiseGeneratorPerlin(new Random(world.getSeed()), 4);
		}

		if(world.provider == null || world.provider.getDimension() != 0)
			return;

		BlockPos anchor = event.getPos();
		int cX = anchor.getX();
		int cZ = anchor.getZ();

		double scale = 0.01D;
		int threshold = 5;

		for(int x = cX + 8; x < cX + 24; x++) {
			for(int z = cZ + 8; z < cZ + 24; z++) {

				double n = this.noise.getValue(x * scale, z * scale);

				if(n > threshold) {
					int range = (int)((n - threshold) * 3);

					if(range > 4)
						range = 8 - range;

					if(range < 0)
						continue;

					for(int y = 30 - range; y <= 30 + range; y++) {

						BlockPos pos = new BlockPos(x, y, z);
						IBlockState state = world.getBlockState(pos);

						if(state.getBlock().isNormalCube(state) && state.getMaterial() == Material.ROCK
								&& state.getBlock().isReplaceableOreGen(state, world, pos, Blocks.STONE.getDefaultState()::equals)) {
							world.setBlockState(pos, ModBlocks.stone_gneiss.getDefaultState(), 2);
						}
					}
				}
			}
		}
	}
}
