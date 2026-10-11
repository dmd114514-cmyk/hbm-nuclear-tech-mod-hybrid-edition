package com.hbm.tileentity;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public interface IRadarCommandReceiver {

	public boolean sendCommandPosition(BlockPos pos);
	public boolean sendCommandEntity(Entity target);
}
