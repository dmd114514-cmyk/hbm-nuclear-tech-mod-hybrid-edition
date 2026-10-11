package com.hbm.blocks.generic;

import com.hbm.blocks.BlockLoot;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The depth-rock ores (three gneiss-cluster pieces, four deep ores): the minability half inherits {@link BlockDepth},
 *
 * and the old version hard-coded crystal/cinnabar/zircon drops in `getItemDropped` and `quantityDropped`, which 1.12.2's block side can't express
 * ⇒ harvestBlock overrides through the {@link BlockLoot} bridge, one `hbm:blocks/<registry name>` table each.
 * Fortune can't reach tables (no tool in the context), so the old "alexandrite capped at 2" multiplier follows §15's exception out.
 */
public class BlockDepthOre extends BlockDepth {

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		BlockLoot.harvest(world, pos, state, player, stack);
	}
}
