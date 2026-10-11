package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidTank;
import api.hbm.tile.ILoadedTile;

/** The participation agreement for fluid nets (parent interface of providers) */
public interface IFluidUserMK2 extends IFluidConnectorMK2, ILoadedTile {

	/** Pressure tier cap; nothing above it gets an array, and changing it changes the memory layout of every fluid net */
	public static final int HIGHEST_VALID_PRESSURE = 5;

	/** Implementations that ignore tiers just use tier 0 */
	public static final int[] DEFAULT_PRESSURE_RANGE = new int[] { 0, 0 };

	public IFluidTank[] getAllTanks();
}
