package com.hbm.tileentity;

import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.util.BobMathUtil;

import api.hbm.fluid.IFluidTank;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.fluidmk2.IFluidUserMK2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * The wrench/copy-wand tier for fluid machines: treats "which fluid is in the tank" as a copyable setting.
 * Coordinates all use 1.12.2's BlockPos (ICopiable already switched over).
 */
public interface IFluidCopiable extends ICopiable {

	/**
	 * The network layer only hands out IFluidTank / IFluidType, but copying needs the content layer's fluid id in the NBT,
	 * so it narrows once here: tanks without a FluidType (forwarding-only, no real fluid) count as non-copyable.
	 */
	static FluidType copyableType(IFluidTank tank) {
		if(tank == null)
			return null;

		api.hbm.fluid.IFluidType type = tank.getTankType();
		return type instanceof FluidType ? (FluidType) type : null;
	}

	/**
	 * @return First type for the normal paste, second type for the alt paste,
	 *         none if there is no alt paste support
	 */
	default int[] getFluidIDToCopy() {
		IFluidUserMK2 tile = (IFluidUserMK2) this;
		ArrayList<Integer> types = new ArrayList<>();

		for(IFluidTank tank : tile.getAllTanks()) {
			FluidType type = copyableType(tank);
			if(type != null && !type.hasNoID())
				types.add(type.getID());
		}

		return BobMathUtil.intCollectionToArray(types);
	}

	/**
	 * Where a plain paste lands: the receiver's first tank. Returns the content-layer FluidTank because changing fluid type
	 * is something only it offers via setTankType; the network layer's IFluidTank contract has no such door.
	 */
	default FluidTank getTankToPaste() {
		TileEntity te = (TileEntity) this;
		if(te instanceof IFluidStandardTransceiverMK2) { // why are we using the transceiver here?
			IFluidTank[] tanks = ((IFluidStandardTransceiverMK2) te).getReceivingTanks();
			if(tanks == null || tanks.length == 0)
				return null;
			return copyableTank(tanks[0]);
		}
		return null;
	}

	static FluidTank copyableTank(IFluidTank tank) {
		return tank instanceof FluidTank ? (FluidTank) tank : null;
	}

	@Override
	default NBTTagCompound getSettings(World world, BlockPos pos) {
		NBTTagCompound tag = new NBTTagCompound();
		if(getFluidIDToCopy().length > 0) tag.setIntArray("fluidID", getFluidIDToCopy());
		return tag;
	}

	@Override
	default void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, BlockPos pos) {
		if(getTankToPaste() != null) {
			int[] ids = nbt.getIntArray("fluidID");
			if(ids.length > 0 && index < ids.length) {
				int id = ids[index];
				getTankToPaste().setTankType(Fluids.fromID(id));
			}
		}
	}

	@Override
	default String[] infoForDisplay(World world, BlockPos pos) {
		int[] ids = getFluidIDToCopy();
		String[] names = new String[ids.length];
		for(int i = 0; i < ids.length; i++) {
			names[i] = Fluids.fromID(ids[i]).getUnlocalizedName();
		}
		return names;
	}
}
