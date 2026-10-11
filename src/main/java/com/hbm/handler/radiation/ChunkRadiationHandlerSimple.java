package com.hbm.handler.radiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.hbm.config.RadiationConfig;
import com.hbm.util.ParticleUtil;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

/**
 * The plainest radiation implementation: one radiation value per chunk, spreading outward one ring, slowly decaying.
 * Data lives in memory for as long as the moon takes to orbit, saved and loaded with the chunk's NBT (key hfr_simple_radiation).
 *
 * Keys switched from ChunkCoordIntPair to ChunkPos.asLong, so the hot path stops allocating an object per lookup.
 */
public class ChunkRadiationHandlerSimple extends ChunkRadiationHandler {

	private final Map<World, SimpleRadiationPerWorld> perWorld = new HashMap<World, SimpleRadiationPerWorld>();
	private static final float MAX_RAD = 100_000F;

	// 3x3 neighborhood offsets, reused all over the spread step
	private static final int[][] NEIGHBOURS = new int[9][];

	static {
		int index = 0;
		for(int i = -1; i <= 1; i++) {
			for(int j = -1; j <= 1; j++) {
				NEIGHBOURS[index++] = new int[] {i, j};
			}
		}
	}

	@Override
	public float getRadiation(World world, int x, int y, int z) {

		SimpleRadiationPerWorld radWorld = perWorld.get(world);
		if(radWorld == null) return 0F;

		Float rad = radWorld.radiation.get(ChunkPos.asLong(x >> 4, z >> 4));
		return rad == null ? 0F : MathHelper.clamp(rad, 0F, MAX_RAD);
	}

	@Override
	public void setRadiation(World world, int x, int y, int z, float rad) {

		SimpleRadiationPerWorld radWorld = perWorld.get(world);
		if(radWorld == null) return;

		BlockPos pos = new BlockPos(x, 0, z);
		if(!world.isBlockLoaded(pos)) return;

		radWorld.radiation.put(ChunkPos.asLong(x >> 4, z >> 4), MathHelper.clamp(rad, 0F, MAX_RAD));
		world.getChunkFromBlockCoords(pos).markDirty();
	}

	@Override
	public void incrementRad(World world, int x, int y, int z, float rad) {
		setRadiation(world, x, y, z, getRadiation(world, x, y, z) + rad);
	}

	@Override
	public void decrementRad(World world, int x, int y, int z, float rad) {
		setRadiation(world, x, y, z, Math.max(getRadiation(world, x, y, z) - rad, 0F));
	}

	@Override
	public void updateSystem() {

		for(Entry<World, SimpleRadiationPerWorld> entry : perWorld.entrySet()) {

			World world = entry.getKey();
			ChunkProviderServer provider = (ChunkProviderServer) ((WorldServer) world).getChunkProvider();
			Map<Long, Float> radiation = entry.getValue().radiation;
			Map<Long, Float> buff = new HashMap<Long, Float>(radiation);
			radiation.clear();

			for(Entry<Long, Float> chunk : buff.entrySet()) {

				if(chunk.getValue() == 0F) continue;

				long key = chunk.getKey();
				int chunkX = (int) (key >> 32);
				int chunkZ = (int) key;

				for(int n = 0; n < NEIGHBOURS.length; n++) {

					int i = NEIGHBOURS[n][0];
					int j = NEIGHBOURS[n][1];
					int type = Math.abs(i) + Math.abs(j);

					// self keeps the most, orthogonal neighbors next, diagonals the least
					float percent = type == 0 ? 0.6F : type == 1 ? 0.075F : 0.025F;
					int newChunkX = chunkX + i;
					int newChunkZ = chunkZ + j;
					long newKey = ChunkPos.asLong(newChunkX, newChunkZ);

					Float val = radiation.get(newKey);
					float existing = val == null ? 0F : val;
					// cells that already had a value stack then decay one tier; freshly reached cells just take the value
					float value = buff.containsKey(newKey) ? (existing + chunk.getValue() * percent) * 0.99F - 0.05F : chunk.getValue() * percent;

					float stored = MathHelper.clamp(value, 0F, MAX_RAD);
					radiation.put(newKey, stored);

					// a cell past the threshold puffs fog at 1/fogCh; fogCh is user-typed, and 0 would blow nextInt on the spot, so guard it first
					if(stored > RadiationConfig.fogRad && RadiationConfig.fogCh > 0
							&& world.rand.nextInt(RadiationConfig.fogCh) == 0
							&& provider.chunkExists(newChunkX, newChunkZ)) {

						int fogX = newChunkX * 16 + world.rand.nextInt(16);
						int fogZ = newChunkZ * 16 + world.rand.nextInt(16);
						int fogY = world.getHeight(fogX, fogZ) + world.rand.nextInt(5);

						ParticleUtil.spawnRadFog(world, fogX, fogY, fogZ);
					}
				}
			}
		}
	}

	@Override
	public void clearSystem(World world) {
		SimpleRadiationPerWorld radWorld = perWorld.get(world);
		if(radWorld != null) radWorld.radiation.clear();
	}

	@Override
	public void receiveWorldLoad(WorldEvent.Load event) {
		if(!event.getWorld().isRemote) perWorld.put(event.getWorld(), new SimpleRadiationPerWorld());
	}

	@Override
	public void receiveWorldUnload(WorldEvent.Unload event) {
		if(!event.getWorld().isRemote) perWorld.remove(event.getWorld());
	}

	private static final String NBT_KEY = "hfr_simple_radiation";

	@Override
	public void receiveChunkLoad(ChunkDataEvent.Load event) {

		if(event.getWorld().isRemote) return;

		SimpleRadiationPerWorld radWorld = perWorld.get(event.getWorld());
		if(radWorld == null) return;

		ChunkPos pos = event.getChunk().getPos();
		radWorld.radiation.put(ChunkPos.asLong(pos.x, pos.z), event.getData().getFloat(NBT_KEY));
	}

	@Override
	public void receiveChunkSave(ChunkDataEvent.Save event) {

		if(event.getWorld().isRemote) return;

		SimpleRadiationPerWorld radWorld = perWorld.get(event.getWorld());
		if(radWorld == null) return;

		ChunkPos pos = event.getChunk().getPos();
		Float val = radWorld.radiation.get(ChunkPos.asLong(pos.x, pos.z));
		event.getData().setFloat(NBT_KEY, val == null ? 0F : val);
	}

	@Override
	public void receiveChunkUnload(ChunkEvent.Unload event) {

		if(event.getWorld().isRemote) return;

		SimpleRadiationPerWorld radWorld = perWorld.get(event.getWorld());
		if(radWorld == null) return;

		ChunkPos pos = event.getChunk().getPos();
		radWorld.radiation.remove(ChunkPos.asLong(pos.x, pos.z));
	}

	public static class SimpleRadiationPerWorld {
		public final Map<Long, Float> radiation = new HashMap<Long, Float>();
	}
}
