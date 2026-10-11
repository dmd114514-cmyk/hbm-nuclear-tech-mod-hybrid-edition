package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerCrateDesh;
import com.hbm.inventory.gui.GUICrateDesh;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The desh crate (104 slots) */
public class TileEntityCrateDesh extends TileEntityCrateBase {

	public TileEntityCrateDesh() {
		super(104);
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.crateDesh";
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerCrateDesh(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUICrateDesh(player.inventory, this);
	}
}
