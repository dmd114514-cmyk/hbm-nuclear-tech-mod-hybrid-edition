package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IBlockMutator;
import com.hbm.inventory.RecipesCommon.MetaBlock;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** Only leaves a ring of specified blocks at the crater rim, for that "scorched surface" look */
public class BlockMutatorBulkie implements IBlockMutator {

	protected MetaBlock metaBlock;

	public BlockMutatorBulkie(Block block) {
		this(block, 0);
	}

	public BlockMutatorBulkie(Block block, int meta) {
		this.metaBlock = new MetaBlock(block, meta);
	}

	@Override
	public void mutatePre(ExplosionVNT explosion, Block block, int meta, int x, int y, int z) {
		IBlockState state = block.getStateFromMeta(meta);
		if(!block.isNormalCube(state)) return;
		Vec3d vec = new Vec3d(x + 0.5 - explosion.posX, y + 0.5 - explosion.posY, z + 0.5 - explosion.posZ);
		if(vec.lengthVector() >= explosion.size - 0.5) {
			explosion.world.setBlockState(new BlockPos(x, y, z), metaBlock.block.getStateFromMeta(metaBlock.meta), 3);
		}
	}

	@Override public void mutatePost(ExplosionVNT explosion, int x, int y, int z) { }
}
