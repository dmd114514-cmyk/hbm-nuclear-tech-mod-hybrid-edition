package api.hbm.fluidmk2;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluid.IFluidType;
import api.hbm.net.DirPos;
import api.hbm.tile.TileAccessCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Machines that take fluid in */
public interface IFluidReceiverMK2 extends IFluidUserMK2 {

	/** Push fluid into a receiver, returns what did not fit */
	public long transferFluid(IFluidType type, int pressure, long amount);

	public default long getReceiverSpeed(IFluidType type, int pressure) { return 1_000_000_000; }

	public long getDemand(IFluidType type, int pressure);

	public default int[] getReceivingPressureRange(IFluidType type) { return DEFAULT_PRESSURE_RANGE; }

	public default void trySubscribe(IFluidType type, World world, DirPos pos) {
		trySubscribe(type, 0, world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir());
	}

	/**
	 * Called once per direction each tick: treat that side's neighbor as a fluid source and take a sip.
	 * Fluid between our own machines comes in through the provider's tryProvide; this only handles third-party devices.
	 */
	public default long trySubscribe(IFluidType type, int pressure, World world, int x, int y, int z, EnumFacing dir) {
		long demand = Math.min(this.getDemand(type, pressure), this.getReceiverSpeed(type, pressure));
		long pulled = FluidNeighbors.pullFromForeign(type, world, x, y, z, dir, demand);
		if(pulled > 0) this.transferFluid(type, pressure, pulled);
		return pulled;
	}

	public default ConnectionPriority getFluidPriority() {
		return ConnectionPriority.NORMAL;
	}
}
