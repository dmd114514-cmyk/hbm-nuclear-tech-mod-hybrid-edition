package com.hbm.explosion.vanillant.standard;

import java.util.HashSet;
import java.util.Iterator;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.explosion.vanillant.interfaces.IBlockProcessor;
import com.hbm.explosion.vanillant.interfaces.IDropChanceMutator;
import com.hbm.explosion.vanillant.interfaces.IFortuneMutator;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockProcessorStandard implements IBlockProcessor {

	protected IDropChanceMutator chance;
	protected IFortuneMutator fortune;
	protected IBlockMutator convert;

	public BlockProcessorStandard() { }

	public BlockProcessorStandard withChance(IDropChanceMutator chance) {
		this.chance = chance;
		return this;
	}

	public BlockProcessorStandard withFortune(IFortuneMutator fortune) {
		this.fortune = fortune;
		return this;
	}

	public BlockProcessorStandard withBlockEffect(IBlockMutator convert) {
		this.convert = convert;
		return this;
	}

	@Override
	public void process(ExplosionVNT explosion, World world, double x, double y, double z, HashSet<BlockPos> affectedBlocks) {

		Iterator<BlockPos> iterator = affectedBlocks.iterator();
		float dropChance = 1.0F / explosion.size;

		while(iterator.hasNext()) {
			BlockPos pos = iterator.next();
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(state.getMaterial() != Material.AIR) {
				if(block.canDropFromExplosion(null)) {

					if(chance != null) {
						dropChance = chance.mutateDropChance(explosion, block, pos.getX(), pos.getY(), pos.getZ(), dropChance);
					}

					int dropFortune = fortune == null ? 0 : fortune.mutateFortune(explosion, block, pos.getX(), pos.getY(), pos.getZ());

					block.dropBlockAsItemWithChance(world, pos, state, dropChance, dropFortune);
				}

				block.onBlockExploded(world, pos, explosion.compat);
				if(this.convert != null) this.convert.mutatePre(explosion, block, block.getMetaFromState(state), pos.getX(), pos.getY(), pos.getZ());
			} else {
				iterator.remove();
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
	}

	public BlockProcessorStandard setNoDrop() {
		this.chance = new DropChanceMutatorStandard(0F);
		return this;
	}
	public BlockProcessorStandard setAllDrop() {
		this.chance = new DropChanceMutatorStandard(1F);
		return this;
	}
	public BlockProcessorStandard setFortune(int fortune) {
		this.fortune = new IFortuneMutator() { //only one use site; not worth a dedicated standard class
			@Override
			public int mutateFortune(ExplosionVNT explosion, Block block, int x, int y, int z) {
				return fortune;
			}
		};
		return this;
	}
}
