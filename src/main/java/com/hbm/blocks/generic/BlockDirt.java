package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.BlockLoot;
import com.hbm.saveddata.TomSaveData;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

/**
 * The meteor-scorched earth: the old family registered only impact_dirt.
 *
 * Two old behaviors copied:
 *   1) drops are plain dirt, not itself — via the {@code BlockLoot} bridge per §15 here, with the
 *      {@code hbm:blocks/impact_dirt} table; change drops by changing the table;
 *   2) on random tick, "headroom bright enough (ash dimming included) and no rain from the sky" grows it into grass,
 *      scorching the plain dirt beneath as well — this is what the wasteland family ({@link BlockWasteEarth}) turns into after burning.
 *      what the wasteland family turns into after burning.
 *
 * The old fully commented-out {@code onNeighborBlockChange} (reverting to dirt when surrounding grass can't hold) was dead code there too; not ported.
 *
 * One changed face: the old {@code getSavedLightValue(EnumSkyBlock.Block, x, y+1, z)} is this version's
 * {@code getLightFor(EnumSkyBlock.BLOCK, cell above)}, and {@code getBlockLightValue} maps to {@code getLightFromNeighbors}.
 */
public class BlockDirt extends BlockBase {

	public BlockDirt(Material mat, boolean tick) {
		super(mat);
		this.setTickRandomly(tick);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		BlockLoot.harvest(world, pos, state, player, stack);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(world.isRemote)
			return;

		TomSaveData data = TomSaveData.forWorld(world);
		BlockPos up = pos.up();

		int light = Math.max(world.getLightFor(EnumSkyBlock.BLOCK, up), (int) (world.getLightFromNeighbors(up) * (1 - data.dust)));

		if(light >= 9 && data.fire == 0) {

			world.setBlockState(pos, Blocks.GRASS.getDefaultState());

			if(world.getBlockState(pos.down()).getBlock() == Blocks.DIRT)
				world.setBlockState(pos.down(), ModBlocksGeneric.impact_dirt.getDefaultState());
		}
	}
}
