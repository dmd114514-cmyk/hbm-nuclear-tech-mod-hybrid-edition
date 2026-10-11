package com.hbm.inventory;

import com.hbm.inventory.fluid.FluidType;

/**
 * HBM's own fluid stack, not the same thing as Forge's same-named class (spell out the full name when touching Forge).
 * One pressure field more than Forge's, because pressurized tanks exist here.
 */
public class FluidStack {

	public FluidType type;
	public int fill;
	public int pressure;

	@Deprecated public FluidStack(int fill, FluidType type) { //this parameter order is cursed
		this.fill = fill;
		this.type = type;
	}

	public FluidStack(FluidType type, int fill) {
		this(type, fill, 0);
	}

	public FluidStack(FluidType type, int fill, int pressure) {
		this.fill = fill;
		this.type = type;
		this.pressure = pressure;
	}
}
