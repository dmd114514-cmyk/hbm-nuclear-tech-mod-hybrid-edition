package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

/**
 * Blocks where mobs may not spawn.
 *
 * The old version overrode `canCreatureSpawn(EnumCreatureType, IBlockAccess, int, int, int)` to return false outright;
 * that road is consolidated on `BlockBase.noMobSpawn()` here (1.12.2's signature became
 * `canCreatureSpawn(IBlockState, IBlockAccess, BlockPos, EntityLiving.SpawnPlacementType)`, the fourth argument no longer an EnumCreatureType),
 * so flipping that switch in the constructor suffices; no second override.
 * What this class adds is the red-text tooltip: players seeing no spawns on the ground deserve to know it's the block's doing.
 */
public class BlockNoSpawn extends BlockBase implements ITooltipProvider {

	public BlockNoSpawn(Material mat) {
		super(mat);
		noMobSpawn();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean ext) {
		// Only the client's ItemBlockBase.addInformation reaches this; the translation facade never resolves on the server
		list.add(TextFormatting.RED + I18nUtil.resolveKey("tile.nospawn"));
	}
}
