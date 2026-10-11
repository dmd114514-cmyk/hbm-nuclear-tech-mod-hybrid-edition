package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerCrateIron;
import com.hbm.inventory.gui.GUICrateIron;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The iron crate (36 slots) */
public class TileEntityCrateIron extends TileEntityCrateBase {

	public TileEntityCrateIron() {
		super(36);
	}

	@Override
	public String getName() {
		return this.hasCustomName() ? this.customName : "container.crateIron";
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerCrateIron(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUICrateIron(player.inventory, this);
	}
}
