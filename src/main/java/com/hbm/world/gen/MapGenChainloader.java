package com.hbm.world.gen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Chains HBM's own surface generators into the vanilla cave generator.
 *
 * Vanilla has no hook for "append a MapGenBase to an existing dimension", but InitMapGenEvent does: on the CAVE / NETHER_CAVE
 * fires, hook other people's generators underneath ours and then run our own chain in order — caves other mods modified stay intact,
 * while our oil seeps, bedrock oil and sellafield craters all take effect in the same pass. Registered at LOWEST precisely to run after the other mods.
 *
 * The old version carried two patches just to "get the current chunk's meta array": a static blockMetas and a per-chunk ledger,
 * plus a post-generation sweep of blockMetaMap + repairBadGeneration(). This version needs none of that: ChunkPrimer holds
 * IBlockState outright, meta lives in the block state and goes in with one setBlockState — no side-array at all,
 * hence none of the "some mods only supplied 256-length old-style meta" traps. Both patches dropped; the `MapGenBaseMeta`
 * "mapgen base with a meta table" retired with them (its only reason to exist was that side-array; zero references left once the patches went),
 * the terrain generators below just extend MapGenBase and hand meta along inside the IBlockState.
 *
 * No callers yet: the four register()/addOverworldGenerator() sites are HbmWorld:44/67/74/92 (that family of
 * terrain generators and the other side's registration aren't queued yet), and HbmWorldGen:50's repairBadGeneration stays unported for the reasons above.
 */
public class MapGenChainloader extends MapGenBase {

	private MapGenBase parent;

	private List<MapGenBase> generators = new ArrayList<MapGenBase>();

	// These may be added to before OR after mapgen registration safely
	private static final List<MapGenBase> overworldGenerators = new ArrayList<MapGenBase>();
	private static final List<MapGenBase> netherGenerators = new ArrayList<MapGenBase>();

	// Executes our chainloaded parent, and all our child generators
	@Override
	public void generate(World world, int chunkX, int chunkZ, ChunkPrimer primer) {

		this.parent.generate(world, chunkX, chunkZ, primer);

		// Some mods may use vanilla gen events for added dimensions, so we guard against that here
		int dimension = world.provider.getDimension();
		if(dimension != 0 && dimension != -1) return;

		for(MapGenBase generator : this.generators) {
			generator.generate(world, chunkX, chunkZ, primer);
		}
	}

	public static void register() {
		MapGenEventHandler handler = new MapGenEventHandler();
		MinecraftForge.TERRAIN_GEN_BUS.register(handler);
		MinecraftForge.EVENT_BUS.register(handler);
	}

	public static void addOverworldGenerator(MapGenBase generator) {
		if(overworldGenerators.contains(generator)) return;
		overworldGenerators.add(generator);
	}

	public static void addNetherGenerator(MapGenBase generator) {
		if(netherGenerators.contains(generator)) return;
		netherGenerators.add(generator);
	}

	public static class MapGenEventHandler {

		// Register as late as possible to pick up any modded cave generators
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public void addMapGenChainloader(InitMapGenEvent event) {

			if(!(event.getNewGen() instanceof MapGenChainloader)) {

				if(event.getType() == EventType.CAVE) {
					MapGenChainloader loader = new MapGenChainloader();
					loader.parent = event.getNewGen();
					loader.generators = overworldGenerators;
					event.setNewGen(loader);
				} else if(event.getType() == EventType.NETHER_CAVE) {
					MapGenChainloader loader = new MapGenChainloader();
					loader.parent = event.getNewGen();
					loader.generators = netherGenerators;
					event.setNewGen(loader);
				}
			}
		}
	}
}
