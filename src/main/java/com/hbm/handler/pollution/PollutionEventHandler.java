package com.hbm.handler.pollution;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@Mod.EventBusSubscriber(modid = "hbm")
public class PollutionEventHandler {

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		PollutionHandler.onWorldLoad(event.getWorld());
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		PollutionHandler.onWorldUnload(event.getWorld());
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if(event.side.isServer() && event.phase == Phase.END) PollutionHandler.serverTick();
	}
}
