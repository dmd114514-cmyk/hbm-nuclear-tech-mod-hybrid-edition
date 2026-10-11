package api.hbm.net;

import net.minecraft.util.math.BlockPos;

/**
 * A node's soul: it can exist in a net without a tile entity, which is why links survive a chunk unload.
 * A single node can span several coordinates (multiblock machines) and stick link points out on several faces.
 *
 * N is bound to a raw NodeNet; see INetworkProvider for why.
 */
@SuppressWarnings("rawtypes")
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public class GenNode<N extends NodeNet> {

	public BlockPos[] positions;
	public DirPos[] connections;
	/** Between node creation and the next net update this is**always** null; ask hasValidNet() before using it */
	public N net;
	public boolean expired = false;
	public boolean recentlyChanged = true;
	/** Distinguishes node types when saving/looking up */
	public INetworkProvider networkProvider;

	public GenNode(INetworkProvider networkProvider, BlockPos... positions) {
		this.networkProvider = networkProvider;
		this.positions = positions;
	}

	public GenNode<N> setConnections(DirPos... connections) {
		this.connections = connections;
		return this;
	}

	/** The usual all-six-faces connection; the vast majority of machine nodes look like this */
	public GenNode<N> setStandardConnections(int x, int y, int z) {
		return this.setConnections(
				new DirPos(x + 1, y, z, Directions.POS_X),
				new DirPos(x - 1, y, z, Directions.NEG_X),
				new DirPos(x, y + 1, z, Directions.POS_Y),
				new DirPos(x, y - 1, z, Directions.NEG_Y),
				new DirPos(x, y, z + 1, Directions.POS_Z),
				new DirPos(x, y, z - 1, Directions.NEG_Z));
	}

	public GenNode<N> addConnection(DirPos connection) {
		DirPos[] old = this.connections;
		DirPos[] next = new DirPos[old.length + 1];
		System.arraycopy(old, 0, next, 0, old.length);
		next[old.length] = connection;
		this.connections = next;
		return this;
	}

	public boolean hasValidNet() {
		return this.net != null && this.net.isValid();
	}

	public void setNet(N net) {
		this.net = net;
		this.recentlyChanged = true;
	}
}
