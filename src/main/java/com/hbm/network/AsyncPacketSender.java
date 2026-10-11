package com.hbm.network;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.LoggingUtil;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Async packet dispatch facade, replacing the old PacketThreading + ThreadedPacket homegrown netty stack.
 *
 * The division of labor is deliberate; don't trip on it:
 *   main thread —— only **queues already-filled packets**;
 *   sender thread —— only hands queued packets to SimpleNetworkWrapper for delivery (netty's write is thread-safe by itself).
 * Meaning: **read world/tile entities freely while constructing the packet**, but once queued treat it as a read-only snapshot,
 * the sender thread never goes back to touch the world. The old implementation's "serialize the whole packet on a worker thread" was dangerous
 *
 * The queue is bounded: if particle-grade floods ever hit, better to drop and count than blow the heap.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class AsyncPacketSender {

	/** Queue capacity. A buffer against send floods — bigger isn't better; it only postpones the pressure to OOM */
	private static final int QUEUE_CAPACITY = 16384;

	private static final ArrayBlockingQueue<Job> QUEUE = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
	private static final AtomicLong dropped = new AtomicLong();
	private static volatile boolean running = false;
	private static Thread worker;

	private AsyncPacketSender() { }

	/** Call once in preInit */
	public static void init() {
		if(running) return;
		running = true;
		worker = new Thread(AsyncPacketSender::pump, "hbm-packet-sender");
		worker.setDaemon(true);
		worker.setPriority(Thread.MIN_PRIORITY); // the sender thread has no business stealing time from the main thread
		worker.start();
	}

	private static void pump() {
		while(running) {
			try {
				Job job = QUEUE.poll(50, TimeUnit.MILLISECONDS);
				if(job == null) continue;
				job.dispatch();
			} catch(InterruptedException ex) {
				if(running) continue;
				return;
			} catch(Throwable ex) {
				// one undeliverable packet shouldn't take the sender thread down, or everything after it jams in the queue
				MainRegistry.logger.error("Failed to dispatch packet", ex);
			}
		}
	}

	/*
	 * The two external entries, named after the old interface so the particle/helper call sites don't change
	 */

	public static void createAllAroundThreadedPacket(IMessage message, TargetPoint target) {
		submit(new Job(message, target, null));
	}

	public static void createSendToThreadedPacket(IMessage message, EntityPlayerMP player) {
		submit(new Job(message, null, player));
	}

	private static void submit(Job job) {
		if(!QUEUE.offer(job)) {
			long total = dropped.incrementAndGet();
			// log on the first drop and every thousandth after; logging every drop would flood the log by itself
			if(total == 1 || total % 1000 == 0) {
				LoggingUtil.warn("Send queue full; {} packets dropped in total", total);
			}
		}
	}

	/** Inline send on the main thread: an escape hatch for "must land this frame"; don't make it the default path */
	public static void sendAllAroundNow(IMessage message, TargetPoint target) {
		NetworkHandler.sendToAllAround(message, target);
	}

	/** Try to drain at server tick end, so no packets stay queued when the world stops */
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase != TickEvent.Phase.END) return;
		if(QUEUE.size() > QUEUE_CAPACITY / 2) waitUntilThreadFinished(2000);
	}

	/** Waits for the queue to drain, returns on timeout; call before world unload / server stop */
	public static void waitUntilThreadFinished(long timeoutMs) {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while(!QUEUE.isEmpty() && System.currentTimeMillis() < deadline) {
			try {
				Thread.sleep(5L);
			} catch(InterruptedException ex) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	/** Stops the thread and clears the queue; server shutdown only */
	public static void shutdown() {
		waitUntilThreadFinished(1000);
		running = false;
		if(worker != null) worker.interrupt();
		QUEUE.clear();
	}

	private static class Job {
		private final IMessage message;
		private final TargetPoint target;
		private final EntityPlayerMP player;

		Job(IMessage message, TargetPoint target, EntityPlayerMP player) {
			this.message = message;
			this.target = target;
			this.player = player;
		}

		void dispatch() {
			if(player != null) NetworkHandler.sendTo(message, player);
			else if(target != null) NetworkHandler.sendToAllAround(message, target);
		}
	}
}
