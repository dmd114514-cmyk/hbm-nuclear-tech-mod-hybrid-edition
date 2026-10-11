package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.inventory.RecipesCommon.MetaBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** If a destroyed cell still touches same-type blocks, patch one back in place so the terrain doesn't get a hole */
public class BlockMutatorDebris implements IBlockMutator {

	protected MetaBlock metaBlock;

	public BlockMutatorDebris(Block block) {
		this(block, 0);
	}

	public BlockMutatorDebris(Block block, int meta) {
		this.metaBlock = new MetaBlock(block, meta);
	}

	@Override public void mutatePre(ExplosionVNT explosion, Block block, int meta, int x, int y, int z) { }

	@Override public void mutatePost(ExplosionVNT explosion, int x, int y, int z) {

		World world = explosion.world;
		for(EnumFacing dir : EnumFacing.VALUES) {
			BlockPos neighbor = new BlockPos(x + dir.getFrontOffsetX(), y + dir.getFrontOffsetY(), z + dir.getFrontOffsetZ());
			IBlockState state = world.getBlockState(neighbor);
			Block b = state.getBlock();
			if(b.isNormalCube(state) && (b != metaBlock.block || b.getMetaFromState(state) != metaBlock.meta)) {
				world.setBlockState(new BlockPos(x, y, z), metaBlock.block.getStateFromMeta(metaBlock.meta), 3);
				return;
			}
		}
	}
}
