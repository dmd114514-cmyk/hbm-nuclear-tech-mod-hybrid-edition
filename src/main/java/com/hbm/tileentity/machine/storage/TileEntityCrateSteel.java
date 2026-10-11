package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerCrateSteel;
import com.hbm.inventory.gui.GUICrateSteel;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The steel crate (54 slots) */
public class TileEntityCrateSteel extends TileEntityCrateBase {

	public TileEntityCrateSteel() {
		super(54);
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.crateSteel";
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerCrateSteel(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUICrateSteel(player.inventory, this);
	}
}
