package com.hbm.blocks.generic;

import com.hbm.blocks.BlockLoot;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Blocks that mine into something else (the disguised blocks reverting when broken).
 *
 * The old version took a Block object in its constructor — drops hard-coded in Java;
 * now the hbm:blocks/<registry name> table speaks, and changing drops means changing the table.
 */
public class BlockGenericDrop extends BlockGeneric {

	public BlockGenericDrop(Material material) {
		super(material);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		BlockLoot.harvest(world, pos, state, player, stack);
	}
}
