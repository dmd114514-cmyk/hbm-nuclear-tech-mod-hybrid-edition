package com.hbm.main;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.hbm.interfaces.IHbmTickReceiver;
import com.hbm.lib.RefStrings;
import com.hbm.util.Clock;
import com.hbm.util.LoggingUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * The global heartbeat driver — counts the beats, then wakes the systems on the roster.
 * <br><br>
 * The old version wrote thirty-odd event handlers and the "advance once per N ticks" system calls straight into this class,
 * making it the project's most conflicted file and leaving every unlisted system homeless. Now event needs are each subscriber's own
 * {@code MinecraftForge.EVENT_BUS} subscription (see {@link IHbmTickReceiver}); only tick dispatch stays here:
 * others implement the interface and call {@link #addTickReceiver}, never touching this file.
 * <br><br>
 * Mounting uses {@code @Mod.EventBusSubscriber} auto-registration (same road as {@code RegistryHandler}),
 * so everything below is static — on that road instance methods get silently skipped, not one fires.
 * Since "being mounted" itself can also fail silently, each side logs one self-proof line on its first real callback,
 * and inspection just checks for that line to know the whole drive chain is actually running.
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
public final class ModEventHandler {

	/**
	 * Copy-on-write read snapshot: mounts are scattered across each init's tail and module init,
	 * while dispatch runs every tick — a plain List would need manual locking or risk concurrent modification.
	 */
	private static final List<IHbmTickReceiver> receivers = new CopyOnWriteArrayList<>();

	/** The global clock's modulo baseline; increments only on server ticks, one per tick */
	private static long serverTicks = 0;

	/** The self-proof logs fire once per side, then stop */
	private static boolean provenClient = false;
	private static boolean provenServer = false;

	private ModEventHandler() { }

	/**
	 * Hangs a heartbeat receiver.
	 * <br><br>
	 * Same name or same instance both count as duplicates and get rejected: once the roster doubles up, no system can be called out by name
	 * as to who's actually running, and nobody remembers to dedup inside their own tick callback, so it's blocked once here.
	 */
	public static void addTickReceiver(IHbmTickReceiver receiver) {

		for(IHbmTickReceiver existing : receivers) {
			if(existing == receiver || existing.tickName().equals(receiver.tickName())) {
				LoggingUtil.warn("[hbm] tick receiver {} already on the roster; mount skipped", receiver.tickName());
				return;
			}
		}

		receivers.add(receiver);
		LoggingUtil.info("[hbm] tick receiver mounted: {} (once every {} ticks)", receiver.tickName(), Math.max(1, receiver.tickInterval()));
	}

	/** Unloading only matters at server shutdown and module toggles; nobody calls it otherwise */
	public static boolean removeTickReceiver(IHbmTickReceiver receiver) {
		return receivers.remove(receiver);
	}

	public static int tickReceiverCount() {
		return receivers.size();
	}

	/**
	 * Due-time check. An interval below 1 is treated as 1, so a toggle typoed to 0 doesn't freeze a system forever.
	 */
	private static boolean due(long clock, int interval) {
		return interval <= 1 || clock % interval == 0;
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {

		if(event.phase != TickEvent.Phase.END) return;

		// The timestamp cache must refresh on the client too: client animations and blinking read it; server-only refresh means it starves the moment you join a server
		Clock.update();

		if(!provenClient) {
			provenClient = true;
			LoggingUtil.info("[hbm] tick drive live (client), {} receivers on the roster", receivers.size());
		}
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {

		if(event.phase != TickEvent.Phase.END) return;

		Clock.update();
		serverTicks++;

		// This line prints even with an empty roster: it proves "the bus really does reach us"; receiver count is a separate matter
		if(!provenServer) {
			provenServer = true;
			LoggingUtil.info("[hbm] tick drive live (server), {} receivers on the roster", receivers.size());
		}

		if(receivers.isEmpty()) return;

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		for(IHbmTickReceiver receiver : receivers) {
			if(server != null && due(serverTicks, receiver.tickInterval()))
				receiver.onServerTick(server);
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {

		if(event.phase != TickEvent.Phase.END) return;
		// The world beat only counts the server's copy: if the same world also fired on the client, bookkeeping systems would advance twice
		if(event.world == null || event.world.isRemote || receivers.isEmpty()) return;

		long worldTime = event.world.getTotalWorldTime();

		for(IHbmTickReceiver receiver : receivers) {
			if(due(worldTime, receiver.tickInterval()))
				receiver.onWorldTick(event.world);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {

		if(event.phase != TickEvent.Phase.END) return;

		EntityPlayer player = event.player;
		if(player == null || receivers.isEmpty()) return;

		World world = player.getEntityWorld();
		if(world == null || world.isRemote) return;

		for(IHbmTickReceiver receiver : receivers) {
			if(due(player.ticksExisted, receiver.tickInterval()))
				receiver.onPlayerTick(player);
		}
	}
}
