package com.hbm.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IGUIProvider {

	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos);
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos);
}
