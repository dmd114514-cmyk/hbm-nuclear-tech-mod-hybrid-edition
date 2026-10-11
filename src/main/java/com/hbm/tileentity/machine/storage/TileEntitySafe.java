package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerSafe;
import com.hbm.inventory.gui.GUISafe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The safe (15 slots) */
public class TileEntitySafe extends TileEntityCrateBase {

	public TileEntitySafe() {
		super(15);
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.safe";
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerSafe(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUISafe(player.inventory, this);
	}
}
