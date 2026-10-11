package api.hbm.fluidmk2;

import api.hbm.net.GenNode;
import api.hbm.net.INetworkProvider;

/**
 * Fluid node. There is one net per fluid, so a node has to remember whose net it is.
 */
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public class FluidNode extends GenNode<FluidNetMK2> {

	@SuppressWarnings("unchecked")
	public FluidNode(INetworkProvider<?> provider, net.minecraft.util.math.BlockPos... positions) {
		super(provider, positions);
	}

	@Override
	public FluidNode setConnections(api.hbm.net.DirPos... connections) {
		super.setConnections(connections);
		return this;
	}
}
