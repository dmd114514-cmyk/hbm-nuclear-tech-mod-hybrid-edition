package com.hbm.handler.radiation;

import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * The abstraction layer of the chunk radiation system. Implementations decide where data lives (memory table / chunk NBT / 3D voxels);
 * events always forward in through ChunkRadiationManager; an implementation only cares about its own data.
 */
public abstract class ChunkRadiationHandler {

	/** Advances the whole system once (all worlds) */
	public abstract void updateSystem();

	public abstract float getRadiation(World world, int x, int y, int z);
	public abstract void setRadiation(World world, int x, int y, int z, float rad);
	public abstract void incrementRad(World world, int x, int y, int z, float rad);
	public abstract void decrementRad(World world, int x, int y, int z, float rad);
	public abstract void clearSystem(World world);

	/* Event forwarding hooks; don't override the ones you don't use */
	public void receiveWorldLoad(WorldEvent.Load event) { }
	public void receiveWorldUnload(WorldEvent.Unload event) { }
	public void receiveWorldTick(TickEvent.ServerTickEvent event) { }

	public void receiveChunkLoad(ChunkDataEvent.Load event) { }
	public void receiveChunkSave(ChunkDataEvent.Save event) { }
	public void receiveChunkUnload(ChunkEvent.Unload event) { }

	public void handleWorldDestruction() { }
}
