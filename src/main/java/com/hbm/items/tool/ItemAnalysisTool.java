package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IAnalyzable;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.util.text.Style;

/**
 * The analyzer: right-click a block and it prints to chat whatever debug lines that block is willing to hand over.
 *
 * Finding the core first is because the old multiblocks (the Dummyable family) store all their data on the core tile,
 * and asking anything from the shell gets you empties — so the found core coords replace the asked coords.
 */
public class ItemAnalysisTool extends Item {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float fX, float fY, float fZ) {

		Block block = world.getBlockState(pos).getBlock();

		BlockPos target = pos;

		if(block instanceof BlockDummyable) {

			BlockPos core = ((BlockDummyable) block).findCore(world, pos);

			if(core != null)
				target = core;
		}

		if(block instanceof IAnalyzable) {

			List<String> debug = ((IAnalyzable) block).getDebugInfo(world, target);

			if(debug != null && !world.isRemote) {

				for(String line : debug) {
					TextComponentString text = new TextComponentString(line);
					text.setStyle(new Style().setColor(TextFormatting.YELLOW));
					player.sendMessage(text);
				}
			}

			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.PASS;
	}
}
