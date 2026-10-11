package com.hbm.handler.radiation;

import com.hbm.config.RadiationConfig;

import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

/**
 * The event front door for chunk radiation; the actual storage/spread algorithms live in the ChunkRadiationHandler implementations.
 * The system advances every 20 ticks; the world-damage step is an optional switch, up to the implementation to override.
 */
@Mod.EventBusSubscriber(modid = "hbm")
public class ChunkRadiationManager {

	public static ChunkRadiationHandler proxy = new ChunkRadiationHandlerSimple();

	private static int eggTimer = 0;

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if(!RadiationConfig.enableChunkRads) return;
		proxy.receiveWorldLoad(event);
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		if(!RadiationConfig.enableChunkRads) return;
		proxy.receiveWorldUnload(event);
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkDataEvent.Load event) {
		if(!RadiationConfig.enableChunkRads) return;
		proxy.receiveChunkLoad(event);
	}

	@SubscribeEvent
	public static void onChunkSave(ChunkDataEvent.Save event) {
		if(!RadiationConfig.enableChunkRads) return;
		proxy.receiveChunkSave(event);
	}

	@SubscribeEvent
	public static void onChunkUnload(ChunkEvent.Unload event) {
		if(!RadiationConfig.enableChunkRads) return;
		proxy.receiveChunkUnload(event);
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {

		if(!RadiationConfig.enableChunkRads) return;

		if(!event.side.isServer() || event.phase != Phase.END) return;

		// Spreading is O(chunks); once a second is plenty
		if(++eggTimer >= 20) {
			proxy.updateSystem();
			eggTimer = 0;
		}

		// World damage (grass/leaves degrading with radiation) has its own switch; off means radiation is computed but terrain untouched
		if(RadiationConfig.worldRadEffects)
			proxy.handleWorldDestruction();

		proxy.receiveWorldTick(event);
	}
}
