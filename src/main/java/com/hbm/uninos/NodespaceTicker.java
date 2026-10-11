package com.hbm.uninos;

import com.hbm.lib.RefStrings;

import api.hbm.net.UniNodespace;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * The nodespace metronome. Updates once every 2 ticks — machine networks don't need rebuilding every single tick,
 * running half as often saves real CPU (the jitter in connection state is invisible to the eye anyway).
 */
@Mod.EventBusSubscriber(modid = RefStrings.MODID)
// Deprecated: node graphs were replaced by the §16 capability neighbor propagation; new code should not build nodes or networks
@Deprecated
public final class NodespaceTicker {

	private static final int INTERVAL = 2;
	private static int tickCounter = 0;

	private NodespaceTicker() { }

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.phase != TickEvent.Phase.END) return;

		// ServerTickEvent in this mapping doesn't carry the server reference; grab it from FML
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		if(server == null) return;

		if(++tickCounter < INTERVAL) return;
		tickCounter = 0;

		UniNodespace.updateNodespace(server);
	}
}
