package com.hbm.interfaces;

import com.hbm.util.Either;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Object whose settings a "wrench/copy wand" can read and write. Coordinates all moved to 1.12.2's BlockPos. */
public interface ICopiable {

	NBTTagCompound getSettings(World world, BlockPos pos);

	void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, BlockPos pos);

	/** A TE reports its own block type; a Block answers with itself */
	default String getSettingsSourceID(Either<TileEntity, Block> self) {
		Block block = self.isLeft() ? self.left().getBlockType() : self.right();
		return block.getUnlocalizedName();
	}

	default String getSettingsSourceDisplay(Either<TileEntity, Block> self) {
		Block block = self.isLeft() ? self.left().getBlockType() : self.right();
		return block.getLocalizedName();
	}

	default String[] infoForDisplay(World world, BlockPos pos) {
		return null;
	}
}
