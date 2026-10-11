package com.hbm.handler.neutron;

import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NeutronNodeWorld {

	// List of all stream worlds.
	public static HashMap<World, StreamWorld> streamWorlds = new HashMap<>();

	public static NeutronNode getNode(World world, BlockPos pos) {
		StreamWorld streamWorld = streamWorlds.get(world);
		return streamWorld != null ? streamWorld.nodeCache.get(pos) : null;
	}

	public static void removeNode(World world, BlockPos pos) {
		StreamWorld streamWorld = streamWorlds.get(world);
		if(streamWorld == null) return;
		streamWorld.removeNode(pos);
	}

	public static StreamWorld getOrAddWorld(World world) {
		StreamWorld streamWorld = streamWorlds.get(world);
		if(streamWorld == null) {
			streamWorld = new StreamWorld();
			streamWorlds.put(world, streamWorld);
		}
		return streamWorld;
	}

	public static void removeAllWorlds() {
		streamWorlds.clear();
	}

	public static void removeEmptyWorlds() {
		streamWorlds.values().removeIf((streamWorld) -> {
			return streamWorld.streams.isEmpty();
		});
	}

	public static class StreamWorld {

		private List<NeutronStream> streams;
		private HashMap<BlockPos, NeutronNode> nodeCache = new HashMap<>();

		public StreamWorld() {
			streams = new ArrayList<>();
		}

		public void runStreamInteractions(World world) {
			for(NeutronStream stream : streams) {
				stream.runStreamInteraction(world, this);
			}
		}

		public void addStream(NeutronStream stream) {
			streams.add(stream);
		}

		public void removeAllStreams() {
			streams.clear();
		}

		/*
		 * Skipped (piece not landed: RBMKNeutronHandler — restore once the RBMK block/TE vertical slice lands):
		 * the old version walked the cached nodes here calling RBMKNeutronHandler.RBMKNeutronNode.checkNode to prune stale ones.
		public void cleanNodes() {
			List<BlockPos> toRemove = new ArrayList<>();
			for(NeutronNode cachedNode : nodeCache.values()) {
				if(cachedNode.type == NeutronStream.NeutronType.RBMK) {
					RBMKNeutronHandler.RBMKNeutronNode node = (RBMKNeutronHandler.RBMKNeutronNode) cachedNode;
					toRemove.addAll(node.checkNode(this));
				}
			}
			for(BlockPos pos : toRemove) {
				nodeCache.remove(pos);
			}
		}
		*/

		public NeutronNode getNode(BlockPos pos) {
			return nodeCache.get(pos);
		}

		public void addNode(NeutronNode node) {
			nodeCache.put(node.pos, node);
		}

		public void removeNode(BlockPos pos) {
			nodeCache.remove(pos);
		}

		public void removeAllStreamsOfType(NeutronStream.NeutronType type) {
			streams.removeIf(stream -> stream.type == type);
		}
	}
}
