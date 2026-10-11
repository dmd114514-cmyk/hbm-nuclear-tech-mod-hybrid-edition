package api.hbm.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import api.hbm.tile.ILoadedTile;
import net.minecraft.tileentity.TileEntity;

/**
 * Base class for a node net: it holds some links, some receivers and some providers.
 * How energy/fluid/items actually move between them is the subclass's business in update().
 *
 * The data structures use LinkedHashSet / HashMap iteration order on purpose so behaviour is reproducible - do not swap in a HashSet.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public abstract class NodeNet<R, P, L extends GenNode> {

	/** Global random source, for things like splitting a remainder randomly where exact attribution does not matter */
	public static Random rand = new Random();

	public boolean valid = true;
	public Set<L> links = new LinkedHashSet<>();

	public Map<R, Long> receiverEntries = new HashMap<>();
	public Map<P, Long> providerEntries = new HashMap<>();

	public NodeNet() {
		UniNodespace.activeNodeNets.add(this);
	}

	///// receiver management
	public boolean isSubscribed(R receiver) { return this.receiverEntries.containsKey(receiver); }
	public void addReceiver(R receiver) { this.receiverEntries.put(receiver, System.currentTimeMillis()); }
	public void removeReceiver(R receiver) { this.receiverEntries.remove(receiver); }

	///// provider management
	public boolean isProvider(P provider) { return this.providerEntries.containsKey(provider); }
	public void addProvider(P provider) { this.providerEntries.put(provider, System.currentTimeMillis()); }
	public void removeProvider(P provider) { this.providerEntries.remove(provider); }

	/** Merge two nets; keep the bigger one, destroy the other in place */
	public void joinNetworks(NodeNet network) {
		if(network == this) return;

		List<L> oldNodes = new ArrayList<>(network.links.size());
		oldNodes.addAll(network.links);

		for(L conductor : oldNodes) forceJoinLink(conductor);
		network.links.clear();

		for(Object connector : network.receiverEntries.keySet()) this.addReceiver((R) connector);
		for(Object connector : network.providerEntries.keySet()) this.addProvider((P) connector);
		network.destroy();
	}

	/** Attach a node to this net, detaching it from its old net first */
	public NodeNet joinLink(L node) {
		if(node.net != null) node.net.leaveLink(node);
		return forceJoinLink(node);
	}

	/** Attach directly, skipping the detach step - only when the internal state is already known */
	public NodeNet forceJoinLink(L node) {
		this.links.add(node);
		node.setNet(this);
		return this;
	}

	public void leaveLink(L node) {
		node.setNet(null);
		this.links.remove(node);
	}

	///// net housekeeping
	public void invalidate() { this.valid = false; UniNodespace.activeNodeNets.remove(this); }
	public boolean isValid() { return this.valid; }
	public void resetTrackers() { }
	public abstract void update();

	public void destroy() {
		this.invalidate();
		for(L link : this.links) if(link.net == this) link.setNet(null);
		this.links.clear();
		this.receiverEntries.clear();
		this.providerEntries.clear();
	}

	/** Dead link test: block broken, chunk unloaded, TE invalidated - none of the three should still count */
	public static boolean isBadLink(Object o) {
		if(o instanceof ILoadedTile && !((ILoadedTile) o).isLoaded()) return true;
		if(o instanceof TileEntity && ((TileEntity) o).isInvalid()) return true;
		return false;
	}
}
