package com.hbm.tileentity.network;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import api.hbm.energymk2.EnergyConversion;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.tile.TileAccessCache;
import com.hbm.capability.CapabilityHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * A run of wire counts as one net: who touches it, which faces they push/pull on, generator or consumer — one sweep records it all here.
 *
 * §16 asks for "neighbors each push/pull", but per-wire neighbor hunting turns one long line into N times the cost,
 * and the old node graph is retired, so routing and caching are home-built: topology re-lays only at load/unload beats, endpoints rescan
 * at most every sixteen beats, and the usual two beats just walk "pull from generators, push to consumers" twice, linearly.
 * The net stores no power itself: what's drawn and pushed settles on the spot, with undeliverable pushes returned along the way — so power is never conjured and rarely lost.
 */
public class ConductorNetNT {

	/** Same beat as the old nodespace: settles every two beats; the grid doesn't need per-tick balancing */
	private static final int INTERVAL = 2;
	/** Endpoint rescan interval (beats): a machine attaching or detaching is noticed within a second at the latest */
	private static final int SCAN_INTERVAL = 16;

	private final World world;
	private final List<TileEntityCableBaseNT> conductors = new ArrayList<>();
	private final List<Endpoint> sources = new ArrayList<>();
	private final List<List<Endpoint>> buckets = new ArrayList<>();
	/** Anti-backtrack set while laying */
	private final IdentityHashMap<TileEntity, Boolean> visited = new IdentityHashMap<>();
	/** Dedup during endpoint rescans: a machine touching several wires records once */
	private final IdentityHashMap<TileEntity, Boolean> claimed = new IdentityHashMap<>();
	private final List<TileEntityCableBaseNT> queue = new ArrayList<>();
	private final List<TileEntityCableBaseNT> links = new ArrayList<>();

	private boolean stale = true;
	private boolean dissolved;
	/** Mid-settlement: the far side may query us back right now, so the rescan waits a beat — otherwise the endpoint table churns itself */
	private boolean balancing;
	private long lastRun = -1;
	private long lastScan = -1;
	private NetPort port;

	private ConductorNetNT(World world) {
		this.world = world;
		for(int i = 0; i < ConnectionPriority.values().length; i++) {
			buckets.add(new ArrayList<Endpoint>());
		}
	}

	/**
	 * An unaffiliated wire finds its organization: if a neighboring edge already has a net, join it; if none does, seed a new one from itself.
	 * When a neighbor carries two nets, the first wins and the other gets discovered and dissolved at the next endpoint rescan.
	 */
	public static ConductorNetNT attach(TileEntityCableBaseNT cable) {
		World world = cable.getWorld();
		List<TileEntityCableBaseNT> near = new ArrayList<>();
		collectLinks(cable, world, near);
		for(TileEntityCableBaseNT link : near) {
			ConductorNetNT net = link.getNet();
			if(net != null && !net.dissolved) {
				net.takeIn(cable);
				return net;
			}
		}

		ConductorNetNT net = new ConductorNetNT(world);
		net.flood(cable);
		return net;
	}

	/** A freshly placed wire joins an existing net: both connections and endpoints rescan */
	private void takeIn(TileEntityCableBaseNT cable) {
		cable.setNet(this);
		conductors.add(cable);
		stale = true;
	}

	/** Spreads from the seed along connections, dissolving any foreign net it meets (members re-find organizations on their own) */
	private void flood(TileEntityCableBaseNT start) {
		visited.clear();
		queue.clear();
		adopt(start);
		queue.add(start);

		for(int i = 0; i < queue.size(); i++) {
			links.clear();
			collectLinks(queue.get(i), world, links);
			for(int j = 0; j < links.size(); j++) {
				if(adopt(links.get(j))) queue.add(links.get(j));
			}
		}

		visited.clear();
		queue.clear();
		links.clear();
		scan();
	}

	/** Adopts a wire into this net; returns false if it's already in */
	private boolean adopt(TileEntityCableBaseNT cable) {
		if(cable == null || visited.containsKey(cable)) return false;
		visited.put(cable, Boolean.TRUE);
		ConductorNetNT other = cable.getNet();
		if(other != null && other != this && !other.dissolved) other.dissolve();
		cable.setNet(this);
		conductors.add(cable);
		return true;
	}

	/** A wire breaks or a switch opens: the whole net dissolves, members re-lay per new connections next beat */
	void leave(TileEntityCableBaseNT cable) {
		conductors.remove(cable);
		cable.setNet(null);
		if(!dissolved) dissolve();
	}

	/** The connection table changed (tower added/removed): dissolve and re-lay likewise */
	void rejoinSoon() {
		if(!dissolved) dissolve();
	}

	private void dissolve() {
		dissolved = true;
		for(TileEntityCableBaseNT c : conductors) {
			if(c.getNet() == this) c.setNet(null);
		}
		conductors.clear();
	}

	public boolean isDissolved() {
		return dissolved;
	}

	/**
	 * Driven by the wire's own ITickable: one net settles once every two beats, whoever ticks first runs it for the whole net.
	 * The old version leaned on the global nodespace tick; that driver is empty here (retired), so self-driving per tickable owes nothing to external hooks.
	 */
	public void tick() {
		if(dissolved || conductors.isEmpty()) return;

		long time = world.getTotalWorldTime();
		if(time == lastRun || time % INTERVAL != 0) return;
		lastRun = time;

		if(stale || time - lastScan >= SCAN_INTERVAL) {
			lastScan = time;
			stale = false;
			scan();
			if(dissolved) return;
		}

		distribute();
	}

	/** Six face neighbors + long-distance direct edges, collected into out; open switches and unloaded chunks stay out */
	private static void collectLinks(TileEntityCableBaseNT cable, World world, List<TileEntityCableBaseNT> out) {
		BlockPos pos = cable.getPos();
		for(EnumFacing dir : EnumFacing.VALUES) {
			addLink(world, pos.getX() + dir.getFrontOffsetX(), pos.getY() + dir.getFrontOffsetY(), pos.getZ() + dir.getFrontOffsetZ(), out);
		}
		for(int[] link : cable.getExtraNetLinks()) {
			addLink(world, link[0], link[1], link[2], out);
		}
	}

	private static void addLink(World world, int x, int y, int z, List<TileEntityCableBaseNT> out) {
		TileEntity te = TileAccessCache.getTileOrCache(world, x, y, z);
		if(!(te instanceof TileEntityCableBaseNT)) return;
		TileEntityCableBaseNT cable = (TileEntityCableBaseNT) te;
		if(cable.isInvalid() || !cable.isLoaded() || !cable.shouldJoinNet()) return;
		out.add(cable);
	}

	/** Rescans endpoints and, on finding "a foreign net next door", dissolves it */
	private void scan() {

		// pulled into a re-lay mid-settlement by an outside call: noted for later; the endpoint table doesn't move this beat
		if(balancing) {
			stale = true;
			return;
		}

		sources.clear();
		for(List<Endpoint> bucket : buckets) bucket.clear();
		claimed.clear();

		for(TileEntityCableBaseNT cable : conductors) {
			if(cable.isInvalid()) continue;
			BlockPos pos = cable.getPos();

			for(EnumFacing dir : EnumFacing.VALUES) {
				TileEntity te = TileAccessCache.getTileOrCache(world,
						pos.getX() + dir.getFrontOffsetX(),
						pos.getY() + dir.getFrontOffsetY(),
						pos.getZ() + dir.getFrontOffsetZ());
				if(te == null) continue;

				if(te instanceof TileEntityCableBaseNT) {
					if(te != cable) {
						ConductorNetNT other = ((TileEntityCableBaseNT) te).getNet();
						if(other != null && other != this && !other.dissolved) other.dissolve();
					}
					continue;
				}

				if(!isAlive(te)) continue;
				if(claimed.put(te, Boolean.TRUE) != null) continue;

				IEnergyStorage storage = storageOf(te, dir);
				if(storage == null) continue;

				Endpoint endpoint = new Endpoint(te, storage);
				if(storage.canExtract()) sources.add(endpoint);
				if(storage.canReceive()) buckets.get(endpoint.priority()).add(endpoint);
			}
		}

		claimed.clear();
	}

	/** Trust the machine's own capability answer first, then fall back to wrapping its own MK2 interface, and only whatever remains is third-party */
	private static IEnergyStorage storageOf(TileEntity te, EnumFacing dir) {
		IEnergyStorage storage = te.getCapability(CapabilityEnergy.ENERGY, dir.getOpposite());
		if(storage != null) return storage;
		return CapabilityHandler.energyStorageOf(te);
	}

	/** Put up a sign during settlement: the far side's machine may immediately query our capability, and the endpoint table must not move then */
	private void distribute() {
		balancing = true;
		try {
			balance();
		} finally {
			balancing = false;
		}
	}

	/** One beat's settlement: draw proportionally from generators first, then push to consumers by priority, returning whatever won't fit */
	private void balance() {

		long available = 0;
		for(Endpoint src : sources) {
			src.amount = 0;
			if(!isAlive(src.tile)) continue;
			int offer = src.storage.extractEnergy(fitInt(src.offerFe()), true);
			if(offer > 0) {
				src.amount = offer;
				available += offer;
			}
		}
		if(available <= 0) return;

		long demand = 0;
		for(List<Endpoint> bucket : buckets) {
			for(Endpoint sink : bucket) {
				sink.amount = 0;
				if(!isAlive(sink.tile)) continue;
				int want = sink.storage.receiveEnergy(fitInt(sink.demandFe()), true);
				if(want > 0) {
					sink.amount = want;
					demand += want;
				}
			}
		}
		if(demand <= 0) return;

		long budget = Math.min(available, demand);

		// take before giving: only spend power already drawn, so we never give out more than we could pull back and conjure power from nothing
		long collected = 0;
		for(Endpoint src : sources) {
			if(src.amount <= 0) continue;
			collected += src.storage.extractEnergy(fitInt(budget * src.amount / available), false);
		}
		if(collected <= 0) return;

		long left = collected;
		for(int p = buckets.size() - 1; p >= 0 && left > 0; p--) {
			List<Endpoint> bucket = buckets.get(p);

			long bucketDemand = 0;
			for(Endpoint sink : bucket) bucketDemand += sink.amount;
			if(bucketDemand <= 0) continue;

			// within a bucket, split by each consumer's demand, high-priority buckets fed first (the old node net used this same allocation)
			long give = Math.min(left, bucketDemand);
			for(Endpoint sink : bucket) {
				if(give <= 0) break;
				long share = give * sink.amount / bucketDemand;
				if(share <= 0) continue;
				int accepted = sink.storage.receiveEnergy(fitInt(share), false);
				if(accepted > 0) {
					give -= accepted;
					left -= accepted;
				}
			}
		}

		// the other side lied in its simulation, or another net fed it full at the same time: the overdraw goes back to the generator
		if(left > 0) refund(left);
	}

	private void refund(long leftover) {
		long left = leftover;
		for(Endpoint src : sources) {
			if(left <= 0) break;
			if(!src.storage.canReceive()) continue;
			left -= src.storage.receiveEnergy(fitInt(left), false);
		}
	}

	/** The net's outward-facing port: what's pushed in is handed to consumers on the spot, what's drawn is gathered from generators on the spot */
	public IEnergyStorage port() {
		if(port == null) port = new NetPort();
		return port;
	}

	private static int fitInt(long value) {
		if(value <= 0) return 0;
		return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
	}

	/** Only devices that aren't broken and whose chunk is genuinely loaded count */
	private static boolean isAlive(TileEntity te) {
		if(te == null || te.isInvalid()) return false;
		if(te instanceof api.hbm.tile.ILoadedTile && !((api.hbm.tile.ILoadedTile) te).isLoaded()) return false;
		return true;
	}

	/**
	 * A device attached to the grid from outside. Its push/pull face was fixed when endpoints were built; rate and priority only mean something for our own machines:
	 * third-party devices are throttled by their own IEnergyStorage; no cap is set for them here.
	 */
	private static final class Endpoint {

		final TileEntity tile;
		final IEnergyStorage storage;
		final IEnergyProviderMK2 provider;
		final IEnergyReceiverMK2 receiver;
		/** The give/take amounts simulated this round; both drawing and pushing split by them */
		long amount;

		Endpoint(TileEntity tile, IEnergyStorage storage) {
			this.tile = tile;
			this.storage = storage;
			this.provider = tile instanceof IEnergyProviderMK2 ? (IEnergyProviderMK2) tile : null;
			this.receiver = tile instanceof IEnergyReceiverMK2 ? (IEnergyReceiverMK2) tile : null;
		}

		/** Our own generators capped by their supply rate (the old node net did the same), converted into FE units */
		long offerFe() {
			if(provider == null) return Integer.MAX_VALUE;
			return EnergyConversion.heToFe(Math.min(provider.getPower(), provider.getProviderSpeed()));
		}

		/** Our own machines capped by their accepted rate; third-party devices throttle themselves */
		long demandFe() {
			if(receiver == null) return Integer.MAX_VALUE;
			return EnergyConversion.heToFe(Math.min(receiver.getMaxPower() - receiver.getPower(), receiver.getReceiverSpeed()));
		}

		int priority() {
			return receiver != null ? receiver.getPriority().ordinal() : ConnectionPriority.NORMAL.ordinal();
		}
	}

	/** The net's outward energy face used as "the port": it stores nothing itself, so capacity reports the generators' current holdings in the net */
	private final class NetPort implements IEnergyStorage {

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			if(maxReceive <= 0) return 0;
			long left = maxReceive;
			long used = 0;

			for(int p = buckets.size() - 1; p >= 0 && left > 0; p--) {
				for(Endpoint sink : buckets.get(p)) {
					if(left <= 0) break;
					int accepted = sink.storage.receiveEnergy(fitInt(left), simulate);
					if(accepted > 0) {
						left -= accepted;
						used += accepted;
					}
				}
			}
			return fitInt(used);
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			if(maxExtract <= 0) return 0;
			long left = maxExtract;
			long got = 0;

			for(Endpoint src : sources) {
				if(left <= 0) break;
				int extracted = src.storage.extractEnergy(fitInt(left), simulate);
				if(extracted > 0) {
					left -= extracted;
					got += extracted;
				}
			}
			return fitInt(got);
		}

		@Override
		public int getEnergyStored() {
			long stored = 0;
			for(Endpoint src : sources) {
				if(!isAlive(src.tile)) continue;
				stored += src.storage.getEnergyStored();
				if(stored > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			}
			return (int) stored;
		}

		@Override
		public int getMaxEnergyStored() {
			long max = 0;
			for(Endpoint src : sources) {
				if(!isAlive(src.tile)) continue;
				max += src.storage.getMaxEnergyStored();
				if(max > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			}
			return (int) max;
		}

		@Override
		public boolean canExtract() {
			return !sources.isEmpty();
		}

		@Override
		public boolean canReceive() {
			for(List<Endpoint> bucket : buckets) {
				if(!bucket.isEmpty()) return true;
			}
			return false;
		}
	}
}
