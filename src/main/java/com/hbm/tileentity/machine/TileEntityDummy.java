package com.hbm.tileentity.machine;

import com.hbm.interfaces.IMultiblock;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

/**
 * The tile entity of a multiblock's "placeholder cells": all logic lives in the core's TE; this one just remembers where the core is.
 *
 * Once the core breaks, placeholders become orphans, so each tick it checks whether the target is still part of the multiblock,
 * and knocks itself out when it isn't — no unremovable air walls left behind.
 */
public class TileEntityDummy extends TileEntity implements ITickable {

	public int targetX;
	public int targetY;
	public int targetZ;

	@Override
	public void update() {
		if(!this.world.isRemote) {
			if(!(this.world.getBlockState(new BlockPos(targetX, targetY, targetZ)).getBlock() instanceof IMultiblock)) {
				world.destroyBlock(pos, false);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.targetX = nbt.getInteger("tx");
		this.targetY = nbt.getInteger("ty");
		this.targetZ = nbt.getInteger("tz");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("tx", this.targetX);
		nbt.setInteger("ty", this.targetY);
		nbt.setInteger("tz", this.targetZ);
		return nbt;
	}
}
