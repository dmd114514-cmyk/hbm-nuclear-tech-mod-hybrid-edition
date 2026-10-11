package com.hbm.handler;

import com.hbm.util.LoggingUtil;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Block save migrations: each chunk gets a "migration number"; on load, mismatched numbers walk the rules upward.
 *
 * The number is a **standalone constant**, no longer squeezed out of the version string — the old trick needed RefStrings.VERSION shaped
 * "1.0.27 BETA (5778)" to grab the number in parens; this version reads "1.0.0", no parens, and copying it would parse
 * -1 all day, the whole mechanism idling. The number **only ever goes up**: adding a rule means +1,
 * and removing a rule doesn't take the number back, or old saves would be mistaken for new ones and skipped.
 */
public class BlockMigrations {

	private static final String NBT_KEY_BUILD_NUMBER = "hfr_migrations_version";

	/** Current migration number; +1 for every new rule */
	public static final int MIGRATION_VERSION = 1;

	private boolean reported = false;

	@SubscribeEvent
	public void onChunkLoad(ChunkDataEvent.Load event) {
		NBTTagCompound data = event.getData();

		// Chunks with no number are treated as "new blocks": same as the old version; the cost is saves predating this mechanism never migrate
		if(!data.hasKey(NBT_KEY_BUILD_NUMBER))
			return;

		int previous = data.getInteger(NBT_KEY_BUILD_NUMBER);
		if(previous == MIGRATION_VERSION)
			return;

		try {
			migrate(event.getChunk(), previous);
		} catch(Exception ex) {
			// One chunk failing shouldn't log a few thousand times; only the first failure reports
			if(!reported) {
				reported = true;
				LoggingUtil.warn("[hbm] chunk migration failing from #{} onward; later ones of this kind report once: {}", previous, ex);
			}
		}
	}

	@SubscribeEvent
	public void onChunkSave(ChunkDataEvent.Save event) {
		event.getData().setInteger(NBT_KEY_BUILD_NUMBER, MIGRATION_VERSION);
	}

	/**
	 * The migration rules table, currently empty (this version's saves start from scratch, nothing to migrate yet).
	 *
	 * When adding a rule: bump {@link #MIGRATION_VERSION} by one and walk forward level by level from previous here,
	 * handling one tier's delta at a time — never jump straight from previous to the current number, or the rules in between get skipped.
	 */
	private void migrate(Chunk chunk, int previous) {

		if(previous >= MIGRATION_VERSION)
			return;
	}
}
