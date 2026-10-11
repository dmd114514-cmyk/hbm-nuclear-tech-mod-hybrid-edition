package api.hbm.energymk2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.energymk2.Nodespace.PowerNode;
import api.hbm.net.NodeNet;
import api.hbm.net.Tuple.Pair;

/**
 * Power net: hands providers' spare power to receivers by priority.
 * Technically this counts as MK3 (folded into UNINOS), but UNINOS grew out of this code almost entirely, so the name stays.
 */
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public class PowerNetMK2 extends NodeNet<IEnergyReceiverMK2, IEnergyProviderMK2, PowerNode> {

	public long energyTracker = 0L;

	protected static final int timeout = 3_000;

	@Override
	public void resetTrackers() {
		this.energyTracker = 0;
	}

	@Override
	public void update() {

		if(providerEntries.isEmpty()) return;
		if(receiverEntries.isEmpty()) return;

		long timestamp = System.currentTimeMillis();

		List<Pair<IEnergyProviderMK2, Long>> providers = new ArrayList<>();
		long powerAvailable = 0;

		// First total up the available power
		Iterator<Entry<IEnergyProviderMK2, Long>> provIt = providerEntries.entrySet().iterator();
		while(provIt.hasNext()) {
			Entry<IEnergyProviderMK2, Long> entry = provIt.next();
			if(timestamp - entry.getValue() > timeout || isBadLink(entry.getKey())) { provIt.remove(); continue; }
			long src = Math.min(entry.getKey().getPower(), entry.getKey().getProviderSpeed());
			if(src > 0) {
				providers.add(new Pair<>(entry.getKey(), src));
				powerAvailable += src;
			}
		}

		// Then total up demand by priority
		List<Pair<IEnergyReceiverMK2, Long>>[] receivers = newReceiverBuckets();
		long[] demand = new long[ConnectionPriority.values().length];
		long totalDemand = 0;

		Iterator<Entry<IEnergyReceiverMK2, Long>> recIt = receiverEntries.entrySet().iterator();
		while(recIt.hasNext()) {
			Entry<IEnergyReceiverMK2, Long> entry = recIt.next();
			if(timestamp - entry.getValue() > timeout || isBadLink(entry.getKey())) { recIt.remove(); continue; }
			long rec = Math.min(entry.getKey().getMaxPower() - entry.getKey().getPower(), entry.getKey().getReceiverSpeed());
			if(rec > 0) {
				int p = entry.getKey().getPriority().ordinal();
				receivers[p].add(new Pair<>(entry.getKey(), rec));
				demand[p] += rec;
				totalDemand += rec;
			}
		}

		long toTransfer = Math.min(powerAvailable, totalDemand);
		long energyUsed = 0;

		// Feed power starting from the highest-priority bucket
		for(int i = ConnectionPriority.values().length - 1; i >= 0; i--) {
			List<Pair<IEnergyReceiverMK2, Long>> list = receivers[i];
			long priorityDemand = demand[i];

			for(Pair<IEnergyReceiverMK2, Long> entry : list) {
				double weight = (double) entry.getValue() / (double) priorityDemand;
				long toSend = (long) Math.min(Math.max(toTransfer * weight, 0D), entry.getValue());
				energyUsed += (toSend - entry.getKey().transferPower(toSend)); // Whatever did not fit has to go back
			}

			toTransfer -= energyUsed;
		}

		this.energyTracker += energyUsed;
		long leftover = energyUsed;

		// Draw from providers proportionally
		for(Pair<IEnergyProviderMK2, Long> entry : providers) {
			double weight = (double) entry.getValue() / (double) powerAvailable;
			long toUse = (long) Math.max(energyUsed * weight, 0D);
			entry.getKey().usePower(toUse);
			leftover -= toUse;
		}

		// Rounding compensation: if there is spare, take it from a random provider, so no power appears out of nowhere
		int iterationsLeft = 100; // a while loop without a brake eventually bites you
		while(iterationsLeft > 0 && leftover > 0 && !providers.isEmpty()) {
			iterationsLeft--;
			Pair<IEnergyProviderMK2, Long> selected = providers.get(rand.nextInt(providers.size()));
			IEnergyProviderMK2 scapegoat = selected.getKey();
			long toUse = Math.min(leftover, scapegoat.getPower());
			scapegoat.usePower(toUse);
			leftover -= toUse;
		}
	}

	/** Diode-style one-way push: push only, never pull; returns what was not delivered */
	public long sendPowerDiode(long power) {

		if(receiverEntries.isEmpty()) return power;

		long timestamp = System.currentTimeMillis();

		List<Pair<IEnergyReceiverMK2, Long>>[] receivers = newReceiverBuckets();
		long[] demand = new long[ConnectionPriority.values().length];
		long totalDemand = 0;

		Iterator<Entry<IEnergyReceiverMK2, Long>> recIt = receiverEntries.entrySet().iterator();
		while(recIt.hasNext()) {
			Entry<IEnergyReceiverMK2, Long> entry = recIt.next();
			if(timestamp - entry.getValue() > timeout) { recIt.remove(); continue; }
			long rec = Math.min(entry.getKey().getMaxPower() - entry.getKey().getPower(), entry.getKey().getReceiverSpeed());
			int p = entry.getKey().getPriority().ordinal();
			receivers[p].add(new Pair<>(entry.getKey(), rec));
			demand[p] += rec;
			totalDemand += rec;
		}

		long toTransfer = Math.min(power, totalDemand);
		long energyUsed = 0;

		for(int i = ConnectionPriority.values().length - 1; i >= 0; i--) {
			List<Pair<IEnergyReceiverMK2, Long>> list = receivers[i];
			long priorityDemand = demand[i];

			for(Pair<IEnergyReceiverMK2, Long> entry : list) {
				double weight = (double) entry.getValue() / (double) priorityDemand;
				long toSend = (long) Math.max(toTransfer * weight, 0D);
				energyUsed += (toSend - entry.getKey().transferPower(toSend));
			}

			toTransfer -= energyUsed;
		}

		this.energyTracker += energyUsed;
		return power - energyUsed;
	}

	@SuppressWarnings("unchecked")
	private static List<Pair<IEnergyReceiverMK2, Long>>[] newReceiverBuckets() {
		int n = ConnectionPriority.values().length;
		List<Pair<IEnergyReceiverMK2, Long>>[] buckets = new List[n];
		for(int i = 0; i < n; i++) buckets[i] = new ArrayList<>();
		return buckets;
	}
}
