package com.hbm.explosion.vanillant.standard;

import java.util.HashSet;
import java.util.Iterator;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.explosion.vanillant.interfaces.IBlockProcessor;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Reshapes without drops and clears the list at the end, so the standard presentation doesn't add another round of block debris */
public class BlockProcessorNoDamage implements IBlockProcessor {

	protected IBlockMutator convert;

	public BlockProcessorNoDamage() { }

	public BlockProcessorNoDamage withBlockEffect(IBlockMutator convert) {
		this.convert = convert;
		return this;
	}

	@Override
	public void process(ExplosionVNT explosion, World world, double x, double y, double z, HashSet<BlockPos> affectedBlocks) {

		Iterator<BlockPos> iterator = affectedBlocks.iterator();

		while(iterator.hasNext()) {
			BlockPos pos = iterator.next();
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(state.getMaterial() != Material.AIR) {
				if(this.convert != null) this.convert.mutatePre(explosion, block, block.getMetaFromState(state), pos.getX(), pos.getY(), pos.getZ());
			}
		}


		if(this.convert != null) {
			iterator = affectedBlocks.iterator();

			while(iterator.hasNext()) {
				BlockPos pos = iterator.next();
				IBlockState state = world.getBlockState(pos);
				Block block = state.getBlock();

				if(state.getMaterial() == Material.AIR) {
					this.convert.mutatePost(explosion, pos.getX(), pos.getY(), pos.getZ());
				}
			}
		}

		affectedBlocks.clear();
	}
}
