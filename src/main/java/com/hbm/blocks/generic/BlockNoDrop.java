package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.BlockLoot;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Mines into nothing.
 *
 * "Nothing" is table-driven now too: no entries in the hbm:blocks/<registry name> table means nothing drops,
 * and pack authors add a pool to the table for drops — no code changes.
 */
public class BlockNoDrop extends BlockBase {

	public BlockNoDrop(Material material) {
		super(material);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		BlockLoot.harvest(world, pos, state, player, stack);
	}
}
