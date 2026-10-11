package com.hbm.items.tool;

import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.main.NTMSounds;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** The match. Strike it against a block's side and that cell catches fire. */
public class ItemMatch extends ItemBase {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float cx, float cy, float cz) {

		if(world.isRemote)
			return EnumActionResult.SUCCESS;

		ItemStack stack = player.getHeldItem(hand);
		// The old version hand-computed ±1 by side, equivalent to "one cell out along the clicked face"
		BlockPos target = pos.offset(face);

		if(!player.canPlayerEdit(target, face, stack))
			return EnumActionResult.FAIL;

		if(world.getBlockState(target).getMaterial() == Material.AIR) {
			world.playSound(null, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, ItemSounds.resolveVanilla(NTMSounds.VANILLA_IGNITE), SoundCategory.BLOCKS, 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
			world.setBlockState(target, Blocks.FIRE.getDefaultState());
		}

		if(!player.capabilities.isCreativeMode)
			stack.shrink(1);

		return EnumActionResult.SUCCESS;
	}
}
