package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidTank;
import api.hbm.fluid.IFluidType;
import api.hbm.net.DirPos;
import api.hbm.tile.TileAccessCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Generic IFluidProviderMK2: hand over the output tanks and this does the drawing and the proportional subtraction.
 */
public interface IFluidStandardSenderMK2 extends IFluidProviderMK2 {

	public IFluidTank[] getSendingTanks();

	public default void tryProvide(IFluidTank tank, World world, DirPos pos) {
		tryProvide(tank.getTankType(), tank.getPressure(), world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
	}

	public default void tryProvide(IFluidType type, int pressure, World world, DirPos pos) {
		tryProvide(type, pressure, world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
	}

	public default void tryProvide(IFluidType type, World world, DirPos pos) {
		tryProvide(type, 0, world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
	}

	public default void tryProvide(IFluidType type, int pressure, World world, int x, int y, int z, EnumFacing dir) {

		TileEntity te = TileAccessCache.getTileOrCache(world, x, y, z);

		//Neighbor propagation: hand it straight to our own receiver next door, third-party devices go over Forge's fluid stack, and pipes in between move their own
		if(te != this && te instanceof IFluidReceiverMK2) {
			IFluidReceiverMK2 rec = (IFluidReceiverMK2) te;
			if(rec.canConnect(type, dir.getOpposite())) {
				long provides = Math.min(this.getFluidAvailable(type, pressure), this.getProviderSpeed(type, pressure));
				long receives = Math.min(rec.getDemand(type, pressure), rec.getReceiverSpeed(type, pressure));
				long toTransfer = Math.min(provides, receives);
				toTransfer -= rec.transferFluid(type, pressure, toTransfer);
				this.useUpFluid(type, pressure, toTransfer);
			}
		}

		if(te != this) {
			long available = Math.min(this.getFluidAvailable(type, pressure), this.getProviderSpeed(type, pressure));
			long sent = FluidNeighbors.pushToForeign(type, world, x, y, z, dir, available);
			if(sent > 0) this.useUpFluid(type, pressure, sent);
		}
	}

	@Override
	public default long getFluidAvailable(IFluidType type, int pressure) {
		long amount = 0;
		for(IFluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) amount += tank.getFill();
		}
		return amount;
	}

	@Override
	public default void useUpFluid(IFluidType type, int pressure, long amount) {
		int tanks = 0;
		for(IFluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) tanks++;
		}

		// Spread one even round across the tanks first, so it does not just drain the first one
		if(tanks > 1) {
			int firstRound = (int) Math.floor((double) amount / (double) tanks);
			for(IFluidTank tank : getSendingTanks()) {
				if(tank.getTankType() == type && tank.getPressure() == pressure) {
					int toRem = Math.min(firstRound, tank.getFill());
					tank.setFill(tank.getFill() - toRem);
					amount -= toRem;
				}
			}
		}

		if(amount > 0) for(IFluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type && tank.getPressure() == pressure) {
				int toRem = (int) Math.min(amount, tank.getFill());
				tank.setFill(tank.getFill() - toRem);
				amount -= toRem;
			}
		}
	}

	@Override
	public default int[] getProvidingPressureRange(IFluidType type) {
		int lowest = HIGHEST_VALID_PRESSURE;
		int highest = 0;

		for(IFluidTank tank : getSendingTanks()) {
			if(tank.getTankType() == type) {
				if(tank.getPressure() < lowest) lowest = tank.getPressure();
				if(tank.getPressure() > highest) highest = tank.getPressure();
			}
		}

		return lowest <= highest ? new int[] { lowest, highest } : DEFAULT_PRESSURE_RANGE;
	}

	@Override
	public default long getProviderSpeed(IFluidType type, int pressure) {
		return 1_000_000_000;
	}
}
