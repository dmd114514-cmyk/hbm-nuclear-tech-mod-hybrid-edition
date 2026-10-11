package com.hbm.interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** The contract of old-school factory multiblocks (the Assembly Machine generation) */
public interface IFactory {

	boolean isStructureValid(World world);

	public long getPowerScaled(long i);

	public int getProgressScaled(int i);

	public boolean isProcessable(ItemStack item);
}
