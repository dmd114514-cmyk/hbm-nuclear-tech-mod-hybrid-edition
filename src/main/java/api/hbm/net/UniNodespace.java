package api.hbm.net;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

/**
 * The unified nodespace: an invisible parallel dimension the nodes live in, with tile entities as their shells.
 * The upside: links do not depend on whether a chunk is loaded - unload the machine and the wires still know each other.
 *
 * The tick rhythm comes from com.hbm.uninos.NodespaceTicker (fill in links every tick, rebuild nets periodically).
 */
@SuppressWarnings({"rawtypes", "unchecked"})
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public final class UniNodespace {

	public static final Map<World, NodeWorld> worlds = new HashMap<>();
	public static final Set<NodeNet<?, ?, ?>> activeNodeNets = new HashSet<>();

	private UniNodespace() { }

	public static GenNode<?> getNode(World world, int x, int y, int z, INetworkProvider<?> type) {
		NodeWorld nodeWorld = worlds.get(world);
		if(nodeWorld != null) return nodeWorld.nodes.get(new NodeKey(new BlockPos(x, y, z), type));
		return null;
	}

	public static void createNode(World world, GenNode<?> node) {
		NodeWorld nodeWorld = worlds.get(world);
		if(nodeWorld == null) {
			nodeWorld = new NodeWorld();
			worlds.put(world, nodeWorld);
		}
		nodeWorld.pushNode(node);
	}

	public static void destroyNode(World world, int x, int y, int z, INetworkProvider<?> type) {
		GenNode<?> node = getNode(world, x, y, z, type);
		if(node != null) popAndExpire(world, node);
	}

	public static void destroyNode(World world, GenNode<?> node) {
		if(node != null) popAndExpire(world, node);
	}

	private static void popAndExpire(World world, GenNode<?> node) {
		NodeWorld nodeWorld = worlds.get(world);
		if(nodeWorld != null) nodeWorld.popNode(node);
	}

	/** Ripped out with the world when it unloads, so the map does not fill up with dead World references */
	public static void clearWorld(World world) {
		NodeWorld nodeWorld = worlds.remove(world);
		if(nodeWorld != null) {
			for(GenNode<?> node : nodeWorld.nodes.values()) {
				if(node.net != null) node.net.destroy();
				node.expired = true;
			}
		}
	}

	private static int reapTimer = 0;

	/** Driven by the server tick: fill in links first, then run every net's update() */
	public static void updateNodespace(MinecraftServer server) {

		for(WorldServer world : server.worlds) {
			NodeWorld nodeWorld = worlds.get(world);
			if(nodeWorld == null) continue;

			for(Entry<NodeKey, GenNode<?>> entry : nodeWorld.nodes.entrySet()) {
				GenNode<?> node = entry.getValue();
				INetworkProvider<?> provider = entry.getKey().provider;
				if(!node.hasValidNet() || node.recentlyChanged) {
					checkNodeConnection(world, node, provider);
					node.recentlyChanged = false;
				}
			}
		}

		updateNetworks();
		updateReapTimer();
	}

	private static void updateNetworks() {

		// reset has to run before update, or last round's accumulated stats bleed into this one
		for(NodeNet<?, ?, ?> net : activeNodeNets) net.resetTrackers();
		for(NodeNet<?, ?, ?> net : activeNodeNets) net.update();

		if(reapTimer <= 0) {
			activeNodeNets.removeIf(net -> {
				net.links.removeIf(link -> link.expired);
				return net.links.isEmpty();
			});
		}
	}

	private static void updateReapTimer() {
		if(reapTimer <= 0) reapTimer = 5 * 60 * 20; // five minutes is plenty, do not do it more often
		else reapTimer--;
	}

	/** Walk the link points, look for a neighbor node, connect when it is there */
	private static void checkNodeConnection(World world, GenNode<?> node, INetworkProvider<?> provider) {

		for(DirPos con : node.connections) {
			GenNode<?> conNode = getNode(world, con.getX(), con.getY(), con.getZ(), provider);
			if(conNode != null) {
				if(conNode.hasValidNet() && conNode.net == node.net) continue;
				if(checkConnection(conNode, con, false)) {
					connectToNode(node, conNode);
				}
			}
		}

		if(node.net == null || !node.net.isValid()) provider.provideNetwork().joinLink(node);
	}

	/** Two-way face check; skipSideCheck ignores the direction value and matches on coordinates only */
	public static boolean checkConnection(GenNode<?> connectsTo, DirPos connectFrom, boolean skipSideCheck) {
		for(DirPos revCon : connectsTo.connections) {
			boolean samePos = revCon.getX() - Directions.offsetX(revCon.getDir()) == connectFrom.getX()
					&& revCon.getY() - Directions.offsetY(revCon.getDir()) == connectFrom.getY()
					&& revCon.getZ() - Directions.offsetZ(revCon.getDir()) == connectFrom.getZ();
			if(samePos && (revCon.getDir() == connectFrom.getDir().getOpposite() || skipSideCheck)) return true;
		}
		return false;
	}

	private static void connectToNode(GenNode origin, GenNode connection) {

		if(origin.hasValidNet() && connection.hasValidNet()) {
			if(origin.net.links.size() > connection.net.links.size()) {
				origin.net.joinNetworks(connection.net);
			} else {
				connection.net.joinNetworks(origin.net);
			}
		} else if(!origin.hasValidNet() && connection.hasValidNet()) {
			connection.net.joinLink(origin);
		} else if(origin.hasValidNet()) {
			origin.net.joinLink(connection);
		}
	}

	/** A composite key of (position, node type), replacing the old new-a-Pair-everywhere style */
	public static final class NodeKey {
		public final BlockPos pos;
		public final INetworkProvider<?> provider;

		public NodeKey(BlockPos pos, INetworkProvider<?> provider) {
			this.pos = pos;
			this.provider = provider;
		}

		@Override
		public int hashCode() {
			return pos.hashCode() * 31 + System.identityHashCode(provider);
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(!(obj instanceof NodeKey)) return false;
			NodeKey other = (NodeKey) obj;
			// Node types are singletons, so comparing references is enough - equals is just slower
			return provider == other.provider && pos.equals(other.pos);
		}
	}

	public static final class NodeWorld {

		/** Insertion order iteration, so net rebuilding is deterministic */
		public final Map<NodeKey, GenNode<?>> nodes = new LinkedHashMap<>();

		public void pushNode(GenNode<?> node) {
			for(BlockPos pos : node.positions) {
				nodes.put(new NodeKey(pos, node.networkProvider), node);
			}
		}

		public void popNode(GenNode<?> node) {
			if(node.net != null) node.net.destroy();
			for(BlockPos pos : node.positions) {
				nodes.remove(new NodeKey(pos, node.networkProvider));
			}
			node.expired = true;
		}
	}
}
