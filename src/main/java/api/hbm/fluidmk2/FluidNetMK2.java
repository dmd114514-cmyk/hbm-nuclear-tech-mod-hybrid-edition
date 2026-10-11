package api.hbm.fluidmk2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluid.IFluidType;
import api.hbm.net.NodeNet;
import api.hbm.net.Tuple.Pair;

/**
 * Fluid net: one extra dimension over the power net, the pressure tier.
 * Inside each pressure tier things are handed out by priority again, and tiers never mix - that is the core rule of HBM's fluid system,
 * hence the preallocated (tier x priority) bucket grid, reused and cleared every tick instead of newing over and over.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public class FluidNetMK2 extends NodeNet<IFluidReceiverMK2, IFluidProviderMK2, FluidNode> {

	public long fluidTracker = 0L;

	protected static final int timeout = 3_000;
	protected static long currentTime = 0;
	protected final IFluidType type;

	public static final int PRESSURE_SLOTS = IFluidUserMK2.HIGHEST_VALID_PRESSURE + 1;
	public static final int PRIORITY_SLOTS = ConnectionPriority.values().length;

	public long[] fluidAvailable = new long[PRESSURE_SLOTS];
	public List<Pair<IFluidProviderMK2, Long>>[] providers = new List[PRESSURE_SLOTS];
	public long[][] fluidDemand = new long[PRESSURE_SLOTS][PRIORITY_SLOTS];
	public List<Pair<IFluidReceiverMK2, Long>>[][] receivers = new List[PRESSURE_SLOTS][PRIORITY_SLOTS];
	public long[] transfered = new long[PRESSURE_SLOTS];

	public FluidNetMK2(IFluidType type) {
		this.type = type;
		for(int i = 0; i < PRESSURE_SLOTS; i++) {
			providers[i] = new ArrayList();
			for(int j = 0; j < PRIORITY_SLOTS; j++) receivers[i][j] = new ArrayList();
		}
	}

	@Override
	public void resetTrackers() {
		this.fluidTracker = 0;
	}

	@Override
	public void update() {

		if(providerEntries.isEmpty()) return;
		if(receiverEntries.isEmpty()) return;
		currentTime = System.currentTimeMillis();

		setupFluidProviders();
		setupFluidReceivers();
		transferFluid();

		cleanUp();
	}

	public void setupFluidProviders() {
		Iterator<Entry<IFluidProviderMK2, Long>> iterator = providerEntries.entrySet().iterator();

		while(iterator.hasNext()) {
			Entry<IFluidProviderMK2, Long> entry = iterator.next();
			if(currentTime - entry.getValue() > timeout || isBadLink(entry.getKey())) { iterator.remove(); continue; }

			IFluidProviderMK2 provider = entry.getKey();
			int[] pressureRange = provider.getProvidingPressureRange(type);
			for(int p = pressureRange[0]; p <= pressureRange[1]; p++) {
				long available = Math.min(provider.getFluidAvailable(type, p), provider.getProviderSpeed(type, p));
				providers[p].add(new Pair(provider, available));
				fluidAvailable[p] += available;
			}
		}
	}

	public void setupFluidReceivers() {
		Iterator<Entry<IFluidReceiverMK2, Long>> iterator = receiverEntries.entrySet().iterator();

		while(iterator.hasNext()) {
			Entry<IFluidReceiverMK2, Long> entry = iterator.next();
			if(currentTime - entry.getValue() > timeout || isBadLink(entry.getKey())) { iterator.remove(); continue; }

			IFluidReceiverMK2 receiver = entry.getKey();
			int[] pressureRange = receiver.getReceivingPressureRange(type);
			for(int p = pressureRange[0]; p <= pressureRange[1]; p++) {
				long required = Math.min(receiver.getDemand(type, p), receiver.getReceiverSpeed(type, p));
				int priority = receiver.getFluidPriority().ordinal();
				receivers[p][priority].add(new Pair(receiver, required));
				fluidDemand[p][priority] += required;
			}
		}
	}

	public void transferFluid() {

		long[] received = new long[PRESSURE_SLOTS];
		long[] notAccountedFor = new long[PRESSURE_SLOTS];

		// if the tier cap ever goes up, the loop bounds below have to go up with it
		for(int p = 0; p <= IFluidUserMK2.HIGHEST_VALID_PRESSURE; p++) {

			long totalAvailable = fluidAvailable[p];

			for(int i = PRIORITY_SLOTS - 1; i >= 0; i--) {
				long toTransfer = Math.min(fluidDemand[p][i], totalAvailable);
				if(toTransfer <= 0) continue;

				long priorityDemand = fluidDemand[p][i];

				for(Pair<IFluidReceiverMK2, Long> entry : receivers[p][i]) {
					double weight = (double) entry.getValue() / (double) priorityDemand;
					long toSend = (long) Math.max(toTransfer * weight, 0D);
					toSend -= entry.getKey().transferFluid(type, p, toSend);
					received[p] += toSend;
					fluidTracker += toSend;
				}

				totalAvailable -= received[p];
			}

			notAccountedFor[p] = received[p];
		}

		// Draw from providers proportionally
		for(int p = 0; p <= IFluidUserMK2.HIGHEST_VALID_PRESSURE; p++) {
			for(Pair<IFluidProviderMK2, Long> entry : providers[p]) {
				double weight = (double) entry.getValue() / (double) fluidAvailable[p];
				long toUse = (long) Math.max(received[p] * weight, 0D);
				entry.getKey().useUpFluid(type, p, toUse);
				notAccountedFor[p] -= toUse;
			}
		}

		// rounding compensation
		for(int p = 0; p <= IFluidUserMK2.HIGHEST_VALID_PRESSURE; p++) {
			int iterationsLeft = 100;
			while(iterationsLeft > 0 && notAccountedFor[p] > 0 && !providers[p].isEmpty()) {
				iterationsLeft--;
				Pair<IFluidProviderMK2, Long> selected = providers[p].get(rand.nextInt(providers[p].size()));
				IFluidProviderMK2 scapegoat = selected.getKey();
				long toUse = Math.min(notAccountedFor[p], scapegoat.getFluidAvailable(type, p));
				scapegoat.useUpFluid(type, p, toUse);
				notAccountedFor[p] -= toUse;
			}
		}
	}

	public void cleanUp() {
		for(int i = 0; i < PRESSURE_SLOTS; i++) {
			fluidAvailable[i] = 0;
			providers[i].clear();
			transfered[i] = 0;

			for(int j = 0; j < PRIORITY_SLOTS; j++) {
				fluidDemand[i][j] = 0;
				receivers[i][j].clear();
			}
		}
	}
}
