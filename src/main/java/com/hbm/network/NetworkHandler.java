package com.hbm.network;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The network facade. One channel "hbm", over Forge's SimpleNetworkWrapper (the old homegrown Netty codec
 * is thoroughly replaced by the official implementation; no point keeping it alive).
 *
 * Keeps HBM's usual send* method names, so migrated old code barely touches its call sites.
 *
 * Reminder: since 1.8 packets are received on the network thread; anything that touches world/blocks/entities
 * must schedule back to the main thread first, or enjoy the random crashes.
 */
public final class NetworkHandler {

	public static final String CHANNEL = "hbm";

	private static SimpleNetworkWrapper wrapper;
	private static int discriminator = 0;

	private NetworkHandler() { }

	/** Initialized once in preInit */
	public static void init() {
		wrapper = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL);
	}

	public static SimpleNetworkWrapper get() {
		return wrapper;
	}

	/**
	 * Registers a packet; discriminator auto-assigned, no hand-written duplicates possible.
	 *
	 * Content-layer machine TE sync packets **may self-register right in their own registrar's preInit()**,
	 * no need to wait for the core to edit its roster — otherwise every new machine means touching a core file, and the core becomes the content layer's bottleneck.
	 * This leans on the standing order in MainRegistry.preInit: channel init() runs before the assembly point and the registrars' preInit(),
	 * so by the time a content registrar runs, the wrapper already exists. Don't move that order earlier.
	 */
	public static <REQ extends IMessage, REPLY extends IMessage> void register(
			Class<? extends IMessageHandler<REQ, REPLY>> handler, Class<REQ> message, Side receiveOn) {
		wrapper.registerMessage(handler, message, discriminator++, receiveOn);
	}

	/*
	 * HBM-style send methods
	 */

	public static void sendToServer(IMessage message) {
		wrapper.sendToServer(message);
	}

	public static void sendToAll(IMessage message) {
		wrapper.sendToAll(message);
	}

	public static void sendTo(IMessage message, EntityPlayerMP player) {
		wrapper.sendTo(message, player);
	}

	public static void sendToDimension(IMessage message, int dimensionId) {
		wrapper.sendToDimension(message, dimensionId);
	}

	public static void sendToAllAround(IMessage message, TargetPoint point) {
		wrapper.sendToAllAround(message, point);
	}

	/** To every player who "can see this coordinate"; Range is ignored */
	public static void sendToAllTracking(IMessage message, TargetPoint point) {
		wrapper.sendToAllTracking(message, point);
	}

	/** To every player tracking the entity; note players don't track themselves — sendTo separately when self-sync is needed */
	public static void sendToAllTracking(IMessage message, Entity entity) {
		wrapper.sendToAllTracking(message, entity);
	}

	/*
	 * Main-thread scheduling
	 */

	/** Tosses the task back to the world's main thread; passing a non-server world just runs it inline */
	public static void scheduleServer(World world, Runnable task) {
		if(world instanceof WorldServer) {
			((IThreadListener) world).addScheduledTask(task);
		} else {
			task.run();
		}
	}

	/** Client-only: tosses the task back to the client main thread */
	public static void scheduleClient(Runnable task) {
		net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(task);
	}
}
