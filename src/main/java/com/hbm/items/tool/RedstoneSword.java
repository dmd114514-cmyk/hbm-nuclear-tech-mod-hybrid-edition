package com.hbm.items.tool;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The redstone sword. Swing it at air and it lays down a stretch of redstone wire, at the price of fourteen durability — a sword that doubles as a wiring tool.
 */
public class RedstoneSword extends ItemSword {

	//Pridenauer you damn bastard.

	public RedstoneSword(ToolMaterial mat) {
		super(mat);
	}

	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getHeldItem(hand);

		if(!player.canPlayerEdit(pos, facing, stack)) return EnumActionResult.PASS;

		if(world.isAirBlock(pos)) {
			world.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS, 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
			world.setBlockState(pos, Blocks.REDSTONE_WIRE.getDefaultState(), 3);
		}

		stack.damageItem(14, player);
		return EnumActionResult.SUCCESS;
	}
}
