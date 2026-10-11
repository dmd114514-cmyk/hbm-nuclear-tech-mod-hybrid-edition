package com.hbm.blocks.gas;

import java.util.HashSet;
import java.util.Random;

import com.hbm.interfaces.Untested;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Flammable gas: a drifting fuel sheet; touch an open flame and it becomes a fire cluster on the spot; itself flammable, so fire climbs it
 *
 * The ignition table keeps the old four (fire / lava / torch / lit pumpkin). The old "add T-construction's stone torch when loaded" line
 * has no per-modid block door in this version's `com.hbm.util.Compat`, and building a shell on someone else's face for one torch isn't worth it —
 * one fewer torch doesn't change the main "touch flame, catch fire" behavior.
 *
 * Drift: 1/3 of moves are vertical only (lighter than most gases), otherwise it spreads sideways; it only disperses downward at 1/20.
 */
public class BlockGasFlammable extends BlockGasBase {

	public static final HashSet<Block> fireSources = new HashSet<Block>();

	public BlockGasFlammable() {
		super(0.8F, 0.8F, 0.2F);

		if(fireSources.isEmpty()) {
			fireSources.add(Blocks.FIRE);
			fireSources.add(Blocks.LAVA);
			fireSources.add(Blocks.TORCH);
			fireSources.add(Blocks.LIT_PUMPKIN);
		}
	}

	@Override
	public EnumFacing getFirstDirection(World world, BlockPos pos) {

		if(world.rand.nextInt(3) == 0)
			return EnumFacing.getFront(world.rand.nextInt(2));

		return this.randomHorizontal(world);
	}

	@Override
	public EnumFacing getSecondDirection(World world, BlockPos pos) {
		return this.randomHorizontal(world);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote) {

			for(EnumFacing dir : EnumFacing.VALUES) {
				Block block = world.getBlockState(pos.offset(dir)).getBlock();

				if(isFireSource(block)) {
					combust(world, pos);
					return;
				}
			}

			if(rand.nextInt(20) == 0 && world.isAirBlock(pos.down())) {
				world.setBlockToAir(pos);
				return;
			}
		}

		super.updateTick(world, pos, state, rand);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		/* an entity already on fire lights this gas by itself */
		if(!world.isRemote && entity.isBurning())
			combust(world, pos);
	}

	@Untested
	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {

		if(world.isRemote)
			return;

		for(EnumFacing dir : EnumFacing.VALUES) {
			Block neighbor = world.getBlockState(pos.offset(dir)).getBlock();

			if(isFireSource(neighbor)) {
				world.scheduleUpdate(pos, this, 2);
				return;
			}
		}
	}

	protected void combust(World world, BlockPos pos) {
		world.setBlockState(pos, Blocks.FIRE.getDefaultState());
	}

	public boolean isFireSource(Block block) {
		return fireSources.contains(block);
	}

	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return true;
	}

	@Override
	public int getDelay(World world) {
		return world.rand.nextInt(5) + 16;
	}
}
