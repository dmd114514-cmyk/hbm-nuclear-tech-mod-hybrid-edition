package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.item.IDepthRockTool;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The depth-rock tier: hands can't mine it, only explosions or tools honoring `IDepthRockTool`.
 *
 * 1.12.2 reshaped the mining-speed door: `getPlayerRelativeBlockHardness(IBlockState, EntityPlayer, World, BlockPos)`,
 * The third argument is a World rather than the old IBlockAccess, so the old version's pass-world-straight-down still works.
 * The tool side (who can mine it) lives in `api.hbm.item.IDepthRockTool`; that's the item line's business — this side only asks the question.
 */
public class BlockDepth extends BlockBase implements ITooltipProvider {

	public BlockDepth() {
		super(Material.ROCK);
		setBlockUnbreakable();
		setResistance(10.0F);
	}

	@Override
	public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {

		ItemStack held = player.getHeldItemMainhand();

		if(held.getItem() instanceof IDepthRockTool) {
			if(((IDepthRockTool) held.getItem()).canBreakRock(world, player, held, this, pos.getX(), pos.getY(), pos.getZ()))
				return 1F / 50F;
		}

		return super.getPlayerRelativeBlockHardness(state, player, world, pos);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean ext) {
		// Only the client's ItemBlockBase.addInformation reaches this
		list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.tile.depth"));
	}
}
