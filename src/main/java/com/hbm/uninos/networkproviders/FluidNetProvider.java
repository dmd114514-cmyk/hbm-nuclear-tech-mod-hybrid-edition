package com.hbm.uninos.networkproviders;

import api.hbm.fluid.IFluidType;
import api.hbm.fluidmk2.FluidNetMK2;
import api.hbm.net.INetworkProvider;

/**
 * Provider for one independent network per fluid type. Attach it when registering the fluid,
 * the network layer leans on it to keep fluids apart.
 */
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public class FluidNetProvider implements INetworkProvider<FluidNetMK2> {

	private final IFluidType type;

	public FluidNetProvider(IFluidType type) {
		this.type = type;
	}

	@Override
	public FluidNetMK2 provideNetwork() {
		return new FluidNetMK2(type);
	}
}
