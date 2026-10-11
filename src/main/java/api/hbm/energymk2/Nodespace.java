package api.hbm.energymk2;

import api.hbm.net.DirPos;
import api.hbm.net.GenNode;
import api.hbm.net.INetworkProvider;
import api.hbm.net.UniNodespace;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Remains of MK1 nodespace. It worked well as a proof of concept, but was not general enough and UNINOS replaced it;
 * this class now only exists as a shim so old call sites glide into UNINOS.
 */
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public final class Nodespace {

	public static final PowerNetProvider THE_POWER_PROVIDER = new PowerNetProvider();

	private Nodespace() { }

	public static PowerNode getNode(World world, int x, int y, int z) {
		return (PowerNode) UniNodespace.getNode(world, x, y, z, THE_POWER_PROVIDER);
	}

	public static void createNode(World world, PowerNode node) {
		UniNodespace.createNode(world, node);
	}

	public static void destroyNode(World world, int x, int y, int z) {
		UniNodespace.destroyNode(world, x, y, z, THE_POWER_PROVIDER);
	}

	public static final class PowerNetProvider implements INetworkProvider<PowerNetMK2> {
		@Override
		public PowerNetMK2 provideNetwork() {
			return new PowerNetMK2();
		}
	}

	public static class PowerNode extends GenNode<PowerNetMK2> {

		public PowerNode(BlockPos... positions) {
			super(THE_POWER_PROVIDER, positions);
		}

		@Override
		public PowerNode setConnections(DirPos... connections) {
			super.setConnections(connections);
			return this;
		}
	}
}
