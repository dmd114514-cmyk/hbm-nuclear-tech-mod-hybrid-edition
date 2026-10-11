package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.gas.ModBlocksGas;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The gas-leaking block family (charred uranium ores, asbestos, lab bricks, ancient waste...).
 *
 * Four switches as before: `randomTick` (whether random ticks run), `rate` (the schedule interval), `onBreak` (leaking when mined),
 * `onNeighbour` (a one-in-three chance on neighbor changes to fill all six air directions with gas). What leaks is {@link #getGas}'s call;
 * the four gas blocks live in {@link ModBlocksGas}.
 *
 * Three rewrites for 1.12.2:
 *   1) coordinates to BlockPos / IBlockState, neighbor changes via `neighborChanged`;
 *   2) the old "leak when mined" lived in `dropBlockAsItemWithChance`; here it's `breakBlock` (vanilla calls it after the block is already air,
 *      so writing one gas cell back is safe — the same timing as before);
 *   3) the old `onEntityWalking` mixed server-side venting with client particles; split by side here — 1.12.2's walking hook
 *      also fires on the server, and unsplit it would run a wasted pass on dedicated servers.
 *
 * Drops remain the block itself (vanilla fallback) with no extras, so this family needs no §15 table.
 * ⚠ the old `getGas()` also had an `ore_nether_coal → gas_monoxide` branch; that block isn't in the block registry yet — add it when it lands.
 */
public class BlockOutgas extends BlockBase {

	boolean randomTick;
	int rate;
	boolean onBreak;
	boolean onNeighbour;

	public BlockOutgas(Material mat, boolean randomTick, int rate, boolean onBreak) {
		super(mat);
		this.setTickRandomly(randomTick);
		this.randomTick = randomTick;
		this.rate = rate;
		this.onBreak = onBreak;
		this.onNeighbour = false;
	}

	public BlockOutgas(Material mat, boolean randomTick, int rate, boolean onBreak, boolean onNeighbour) {
		this(mat, randomTick, rate, onBreak);
		this.onNeighbour = onNeighbour;
	}

	@Override
	public int tickRate(World world) {
		return rate;
	}

	protected Block getGas() {

		if(this == ModBlocksGeneric.ore_uranium_scorched || this == ModBlocksGeneric.ore_gneiss_uranium
				|| this == ModBlocksGeneric.ore_gneiss_uranium_scorched || this == ModBlocksGeneric.ore_nether_uranium
				|| this == ModBlocksGeneric.ore_nether_uranium_scorched) {
			return ModBlocksGas.gas_radon;
		}

		if(this == ModBlocksGeneric.block_corium_cobble)
			return ModBlocksGas.gas_radon;

		if(this == ModBlocksGeneric.ancient_scrap)
			return ModBlocksGas.gas_radon_tomb;

		if(this == ModBlocksGeneric.ore_gneiss_asbestos || this == ModBlocksGeneric.block_asbestos
				|| this == ModBlocksGeneric.deco_asbestos || this == ModBlocksGeneric.brick_asbestos
				|| this == ModBlocksGeneric.tile_lab || this == ModBlocksGeneric.tile_lab_cracked
				|| this == ModBlocksGeneric.tile_lab_broken) {
			return ModBlocksGas.gas_asbestos;
		}

		return Blocks.AIR;
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {

		if(this.randomTick && getGas() == ModBlocksGas.gas_asbestos) {

			BlockPos up = pos.up();

			if(world.isAirBlock(up)) {

				if(!world.isRemote && world.rand.nextInt(10) == 0)
					world.setBlockState(up, ModBlocksGas.gas_asbestos.getDefaultState());

				if(world.isRemote)
					for(int i = 0; i < 5; i++)
						world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + world.rand.nextFloat(), pos.getY() + 1.1, pos.getZ() + world.rand.nextFloat(), 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		EnumFacing dir = EnumFacing.getFront(rand.nextInt(6));
		BlockPos next = pos.offset(dir);

		if(world.isAirBlock(next))
			world.setBlockState(next, getGas().getDefaultState());
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {

		if(onNeighbour && world.rand.nextInt(3) == 0) {

			for(EnumFacing dir : EnumFacing.VALUES) {
				BlockPos next = pos.offset(dir);

				if(world.isAirBlock(next))
					world.setBlockState(next, getGas().getDefaultState());
			}
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		if(onBreak)
			world.setBlockState(pos, getGas().getDefaultState());

		if(this == ModBlocksGeneric.ancient_scrap) {
			for(int ix = -2; ix <= 2; ix++) {
				for(int iy = -2; iy <= 2; iy++) {
					for(int iz = -2; iz <= 2; iz++) {

						int sum = Math.abs(ix + iy + iz);
						BlockPos target = pos.add(ix, iy, iz);

						if(sum < 5 && sum > 0 && world.isAirBlock(target))
							world.setBlockState(target, this.getGas().getDefaultState());
					}
				}
			}
		}

		super.breakBlock(world, pos, state);
	}
}
