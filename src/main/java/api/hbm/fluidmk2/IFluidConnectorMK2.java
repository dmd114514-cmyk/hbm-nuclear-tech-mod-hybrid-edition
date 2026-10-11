package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidType;
import net.minecraft.util.EnumFacing;

public interface IFluidConnectorMK2 {

	/** dir is THIS block's face, not the side doing the checking */
	public default boolean canConnect(IFluidType type, EnumFacing dir) {
		return dir != null;
	}
}
