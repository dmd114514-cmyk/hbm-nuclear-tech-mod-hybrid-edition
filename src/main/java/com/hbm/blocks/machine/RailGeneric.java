package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.IBlockNaming;
import com.hbm.blocks.ITooltipProvider;

import net.minecraft.block.BlockRail;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The vanilla-shaped rail family: wooden, narrow and high-speed rails, differing only in speed caps and whether they curve.
 * Shape is a block state in 1.12.2, and vanilla's BlockRail already manages it — extending it beats maintaining a SHAPE field.
 */
public class RailGeneric extends BlockRail implements IBlockNaming, ITooltipProvider {

	/*Vanilla wooden rail's speed; the percentages in the tooltips take it as the baseline */
	protected static final float baseSpeed = 0.4F;
	protected float maxSpeed = 0.4F;
	protected boolean slopable = true;
	protected boolean flexible = true;

	public RailGeneric(String tool, int harvestLevel) {
		setHarvestLevel(tool, harvestLevel);
	}

	public RailGeneric setMaxSpeed(float speed) {
		this.maxSpeed = speed;
		return this;
	}

	public RailGeneric setFlexible(boolean flexible) {
		this.flexible = flexible;
		return this;
	}

	public RailGeneric setSlopable(boolean slopable) {
		this.slopable = slopable;
		return this;
	}

	/*Textures belong to JSON models now; this only absorbs the old chained writing, keeping "which texture" in code */
	public RailGeneric setBlockTextureName(String name) {
		return this;
	}

	@Override
	public float getRailMaxSpeed(World world, EntityMinecart cart, BlockPos pos) {
		return maxSpeed;
	}

	@Override
	public boolean isFlexibleRail(IBlockAccess world, BlockPos pos) {
		return !this.isPowered;
	}

	@Override
	public boolean canMakeSlopes(IBlockAccess world, BlockPos pos) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean ext) {

		float speed = this.maxSpeed / baseSpeed;

		if(speed != 1F)
			list.add((speed > 1 ? TextFormatting.BLUE : TextFormatting.RED) + "Speed: " + ((int) (speed * 100)) + "%");

		if(!flexible)
			list.add(TextFormatting.RED + "Cannot be used for turns!");

		if(!slopable)
			list.add(TextFormatting.RED + "Cannot be used for slopes!");
	}
}
