package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.gas.ModBlocksGas;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The nuclear waste blocks drifting dense radon (the block_waste trio: plain / painted / vitrified).
 *
 * The radiation half eats the {@link BlockHazard} base entirely (one hazard-table read on entry, an add every 20 ticks with rescheduling);
 * this only layers the "gas drifting" half: each tick rolls first, and on a half chance with an empty target neighbor the dense radon goes in,
 * then the base runs — order as the old version; radiation never precedes the venting.
 *
 * One reminder: the base only schedules the next update when rad exceeds 0, so whether these three drift — like the twenty-seven
 * hazardous pieces — depends on the hazard table having their radiation entries; before the table lands, placing one only earns you a visible block.
 */
public class BlockNuclearWaste extends BlockHazard {

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		EnumFacing dir = EnumFacing.getFront(rand.nextInt(6));
		BlockPos target = pos.offset(dir);

		if(rand.nextInt(2) == 0 && world.isAirBlock(target)) {
			world.setBlockState(target, ModBlocksGas.gas_radon_dense.getDefaultState());
		}

		super.updateTick(world, pos, state, rand);
	}
}
