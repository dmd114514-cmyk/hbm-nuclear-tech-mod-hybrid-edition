package api.hbm.tile;

import java.util.HashMap;
import java.util.Map;

import api.hbm.net.Tuple.Quartet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Neighbor lookup cache. Energy/fluid nets ask what sits at the coord next door over and over every tick,
 * and world.getTileEntity is far too expensive for that, so results are cached for a few dozen ticks.
 *
 * Short cache for misses, long for hits: after a machine is broken you want to notice it is gone quickly, while a machine that exists is not worth re-querying.
 */
public final class TileAccessCache {

	public static final Map<Quartet<Integer, Integer, Integer, Integer>, TileAccessCache> cache = new HashMap<>();

	/** Misses cached for 1 second, so a broken machine does not leave a ghost connection behind */
	public static final int NULL_CACHE = 20;
	/** Hits cached for 3 seconds */
	public static final int NONNULL_CACHE = 60;

	public final TileEntity tile;
	public final long expiresOn;

	public TileAccessCache(TileEntity tile, long expiresOn) {
		this.tile = tile;
		this.expiresOn = expiresOn;
	}

	public boolean hasExpired(long worldTime) {
		if(tile != null && tile.isInvalid()) return true;
		if(worldTime >= expiresOn) return true;
		if(tile instanceof ILoadedTile && !((ILoadedTile) tile).isLoaded()) return true;
		return false;
	}

	/** One mutable key instance is reused - lookups mutate rather than clone, which saves a pile of temporary Quartets */
	public static final Quartet<Integer, Integer, Integer, Integer> publicCumRag = new Quartet<>(0, 0, 0, 0);

	public static TileEntity getTileOrCache(World world, int x, int y, int z) {
		publicCumRag.mangle(x, y, z, world.provider.getDimension());
		TileAccessCache entry = cache.get(publicCumRag);

		if(entry == null || entry.hasExpired(world.getTotalWorldTime())) {
			TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			entry = new TileAccessCache(tile, world.getTotalWorldTime() + (tile == null ? NULL_CACHE : NONNULL_CACHE));
			cache.put(publicCumRag.clone(), entry);
			return tile;
		} else {
			return entry.tile;
		}
	}

	/** Cleared on chunk/dim unload so the cache does not hold dead TEs from other worlds */
	public static void clear() {
		cache.clear();
	}
}
