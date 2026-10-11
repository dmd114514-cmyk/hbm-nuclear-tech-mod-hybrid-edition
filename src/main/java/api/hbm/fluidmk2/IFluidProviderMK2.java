package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidType;

/** Machines that give fluid out */
public interface IFluidProviderMK2 extends IFluidUserMK2 {

	public void useUpFluid(IFluidType type, int pressure, long amount);

	public default long getProviderSpeed(IFluidType type, int pressure) { return 1_000_000_000; }

	public long getFluidAvailable(IFluidType type, int pressure);

	public default int[] getProvidingPressureRange(IFluidType type) { return DEFAULT_PRESSURE_RANGE; }
}
