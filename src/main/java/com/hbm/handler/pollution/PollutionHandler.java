package com.hbm.handler.pollution;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.hbm.config.MobConfig;
import com.hbm.config.RadiationConfig;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.storage.WorldSavedData;

public class PollutionHandler {

	// The save name, plus the per-second production baseline of the three base pollutants (carrying over the old balance numbers)
	public static final String DATA_NAME = "hbmpollution";
	public static final float SOOT_PER_SECOND = 1F / 25F;
	public static final float HEAVY_METAL_PER_SECOND = 1F / 50F;
	public static final float POISON_PER_SECOND = 1F / 50F;
	private static final float MAX_POLLUTION = 10_000F;

	// Where players have slept; the enraged swarm follows the scent there
	public static Vec3d targetCoords;

	// Only server worlds register; the client never gets data so the simulation never runs there, saving a pile of isRemote checks
	private static final Map<World, PollutionWorldData> loaded = new HashMap<>();
	private static final int[][] OFFSETS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
	// values() clones its array on every call; don't use it inside loops
	private static final PollutionType[] VALUES = PollutionType.values();
	private static final int SOOT = PollutionType.SOOT.ordinal();
	private static final int POISON = PollutionType.POISON.ordinal();
	private static final int HEAVYMETAL = PollutionType.HEAVYMETAL.ordinal();

	///////////////////////
	/// access and read/write stubs ///
	///////////////////////

	public static void onWorldLoad(World world) {

		if(!RadiationConfig.enablePollution || world.isRemote) return;

		PollutionWorldData data = (PollutionWorldData) world.getPerWorldStorage().getOrLoadData(PollutionWorldData.class, DATA_NAME);
		if(data == null) {
			data = new PollutionWorldData(DATA_NAME);
			world.getPerWorldStorage().setData(DATA_NAME, data);
		}
		loaded.put(world, data);
	}

	public static void onWorldUnload(World world) {
		loaded.remove(world);
	}

	///////////////////////
	/// static read/write surface    ///
	///////////////////////

	public static void incrementPollution(World world, int x, int y, int z, PollutionType type, float amount) {

		if(!RadiationConfig.enablePollution) return;

		PollutionWorldData data = loaded.get(world);
		if(data == null) return;

		PollutionData chunk = data.chunkData(x, z, true);
		int index = type.ordinal();
		chunk.pollution[index] = MathHelper.clamp(chunk.pollution[index] + amount * MobConfig.pollutionMult, 0F, MAX_POLLUTION);
		data.markDirty();
	}

	public static void decrementPollution(World world, int x, int y, int z, PollutionType type, float amount) {
		incrementPollution(world, x, y, z, type, -amount);
	}

	public static void setPollution(World world, int x, int y, int z, PollutionType type, float amount) {

		if(!RadiationConfig.enablePollution) return;

		PollutionWorldData data = loaded.get(world);
		if(data == null) return;

		data.chunkData(x, z, true).pollution[type.ordinal()] = amount;
		data.markDirty();
	}

	public static float getPollution(World world, int x, int y, int z, PollutionType type) {

		if(!RadiationConfig.enablePollution) return 0F;

		PollutionWorldData data = loaded.get(world);
		if(data == null) return 0F;

		PollutionData chunk = data.chunkData(x, z, false);
		return chunk == null ? 0F : chunk.pollution[type.ordinal()];
	}

	public static PollutionData getPollutionData(World world, int x, int y, int z) {

		if(!RadiationConfig.enablePollution) return null;

		PollutionWorldData data = loaded.get(world);
		if(data == null) return null;

		return data.chunkData(x, z, false);
	}

	////////////////////
	/// server main loop ///
	////////////////////

	private static int eggTimer = 0;

	public static void serverTick() {

		handleWorldDestruction();

		// Spreading is O(chunks); runs every 60 ticks
		if(++eggTimer < 60) return;
		eggTimer = 0;

		for(PollutionWorldData data : loaded.values()) {

			Map<Long, PollutionData> next = new HashMap<>();

			for(Entry<Long, PollutionData> entry : data.pollution.entrySet()) {

				long key = entry.getKey();
				int x = (int) (key >> 32);
				int z = (int) key;
				PollutionData chunk = entry.getValue();

				float[] spread = new float[VALUES.length];

				/* This cell's decay and its output toward neighbors */
				if(chunk.pollution[SOOT] > 10) {
					spread[SOOT] = chunk.pollution[SOOT] * 0.05F;
					chunk.pollution[SOOT] *= 0.8F;
				}
				chunk.pollution[SOOT] *= 0.99F;
				chunk.pollution[HEAVYMETAL] *= 0.9995F;

				if(chunk.pollution[POISON] > 10) {
					spread[POISON] = chunk.pollution[POISON] * 0.025F;
					chunk.pollution[POISON] *= 0.9F;
				} else {
					chunk.pollution[POISON] *= 0.995F;
				}

				/* write back to self */
				PollutionData self = next.get(key);
				if(self == null) self = new PollutionData();

				boolean keep = false;
				for(int i = 0; i < self.pollution.length; i++) {
					self.pollution[i] += chunk.pollution[i];
					if(self.pollution[i] > 0) keep = true;
				}
				if(keep) next.put(key, self);

				/* split among the four neighbors */
				for(int[] offset : OFFSETS) {
					long neighbourKey = ChunkPos.asLong(x + offset[0], z + offset[1]);
					PollutionData neighbour = next.get(neighbourKey);
					if(neighbour == null) neighbour = new PollutionData();

					keep = false;
					for(int i = 0; i < neighbour.pollution.length; i++) {
						neighbour.pollution[i] += spread[i];
						if(neighbour.pollution[i] > 0) keep = true;
					}
					if(keep) next.put(neighbourKey, neighbour);
				}
			}

			data.pollution.clear();
			data.pollution.putAll(next);
			data.markDirty();
		}
	}

	// Once poison gas piles up it nibbles the ground: grass turns to coarse dirt, foliage withers
	private static final float DESTRUCTION_THRESHOLD = 15F;
	private static final int DESTRUCTION_COUNT = 5;

	private static void handleWorldDestruction() {

		for(Entry<World, PollutionWorldData> entry : loaded.entrySet()) {

			World world = entry.getKey();
			ChunkProviderServer provider = (ChunkProviderServer) ((WorldServer) world).getChunkProvider();

			for(Entry<Long, PollutionData> pollution : entry.getValue().pollution.entrySet()) {

				if(pollution.getValue().pollution[POISON] < DESTRUCTION_THRESHOLD) continue;

				long key = pollution.getKey();
				int chunkX = (int) (key >> 32);
				int chunkZ = (int) key;

				for(int i = 0; i < DESTRUCTION_COUNT; i++) {

					int x = (chunkX << 6) + world.rand.nextInt(64);
					int z = (chunkZ << 6) + world.rand.nextInt(64);

					if(!provider.chunkExists(x >> 4, z >> 4)) continue;

					int y = world.getHeight(x, z) - world.rand.nextInt(3) + 1;
					BlockPos pos = new BlockPos(x, y, z);
					IBlockState state = world.getBlockState(pos);
					Block block = state.getBlock();

					if(block == Blocks.GRASS || (block == Blocks.DIRT && state.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT)) {
						world.setBlockState(pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), 3);
					} else if(block == Blocks.TALLGRASS || state.getMaterial() == Material.LEAVES || state.getMaterial() == Material.PLANTS) {
						world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
					}
				}
			}
		}
	}

	////////////////////
	/// data structures     ///
	////////////////////

	public static class PollutionWorldData extends WorldSavedData {

		// Keys are ChunkPos.asLong, sparing the hot path an allocation per chunk
		public final Map<Long, PollutionData> pollution = new HashMap<>();

		public PollutionWorldData(String name) {
			super(name);
		}

		// Pollution tallies per 4x4-chunk cell; coordinates shift right by 6
		public PollutionData chunkData(int x, int z, boolean create) {

			long key = ChunkPos.asLong(x >> 6, z >> 6);
			PollutionData data = pollution.get(key);

			if(data == null && create) {
				data = new PollutionData();
				pollution.put(key, data);
			}
			return data;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {

			NBTTagList list = nbt.getTagList("entries", 10);

			for(int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound tag = list.getCompoundTagAt(i);
				long key = ChunkPos.asLong(tag.getInteger("chunkX"), tag.getInteger("chunkZ"));
				pollution.put(key, PollutionData.fromNBT(tag));
			}
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

			NBTTagList list = new NBTTagList();

			for(Entry<Long, PollutionData> entry : pollution.entrySet()) {
				NBTTagCompound tag = new NBTTagCompound();
				// broken back into chunk coordinates for saving, so the save stays human-readable
				tag.setInteger("chunkX", (int) (entry.getKey() >> 32));
				tag.setInteger("chunkZ", entry.getKey().intValue());
				entry.getValue().toNBT(tag);
				list.appendTag(tag);
			}

			nbt.setTag("entries", list);
			return nbt;
		}
	}

	public static class PollutionData {

		public float[] pollution = new float[VALUES.length];

		public static PollutionData fromNBT(NBTTagCompound nbt) {

			PollutionData data = new PollutionData();

			for(int i = 0; i < VALUES.length; i++) {
				data.pollution[i] = nbt.getFloat(VALUES[i].name().toLowerCase(Locale.US));
			}
			return data;
		}

		public void toNBT(NBTTagCompound nbt) {

			for(int i = 0; i < VALUES.length; i++) {
				nbt.setFloat(VALUES[i].name().toLowerCase(Locale.US), pollution[i]);
			}
		}
	}

	// The order is the array index in the save; touch it and saves cross-contaminate
	public enum PollutionType {
		SOOT, POISON, HEAVYMETAL, FALLOUT;
	}
}
