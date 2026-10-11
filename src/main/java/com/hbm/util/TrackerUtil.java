package com.hbm.util;

import com.hbm.interfaces.NotableComments;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.util.IntHashMap;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * The whole point of this file: vanilla hard-codes "tracking distance" inside its tracker with zero leeway.
 * The moment an absurdly fast projectile leaves that circle the client stops receiving its position, so missiles vanish mid-flight,
 * and ricocheting bullets cross a wall then fold back. Changing the circle means reaching into its private ledger.
 */
@NotableComments
public class TrackerUtil {

	/** Fetches an entity's server-side tracking entry; null when untracked (say, riding another entity) */
	public static EntityTrackerEntry getTrackerEntry(WorldServer world, int entityId) {
		EntityTracker tracker = world.getEntityTracker();
		IntHashMap<EntityTrackerEntry> map = ReflectionHelper.getPrivateValue(EntityTracker.class, tracker, "trackedEntityHashTable", "field_72794_c");
		return map.lookup(entityId);
	}

	/**
	 * Hard-sends a teleport packet to snap the client back, and zeroes the tracking entry's motion counter,
	 * so the same tick doesn't append an interpolation that smooths the just-placed position away again.
	 *
	 * The old ×32 rounding and ×256/360 byte packing were needed because 1.7's packet stored shorts;
	 * this version's packet takes the whole entity and carries doubles internally, so no hand-packing.
	 */
	public static void sendTeleport(World world, Entity e) {
		if(!(world instanceof WorldServer)) return;

		EntityTrackerEntry entry = getTrackerEntry((WorldServer) world, e.getEntityId());
		if(entry == null) return;

		entry.sendPacketToTrackedPlayers(new SPacketEntityTeleport(e));
		entry.updateCounter = 0;
	}

	/**
	 * Raises an entity's tracking distance (blocks). Freshly spawned entities may have no tracking entry yet; leave those alone.
	 *
	 * The counterpart here is EntityTrackerEntry's private final range, writable only via reflection.
	 * The public setMaxRange(int) is not the same thing: maxRange is the server view-distance cap (refreshed wholesale by
	 * EntityTracker.setViewDistance), while isVisibleTo takes min(range, maxRange),
	 * so using it as tracking distance neither raises the range nor keeps the cap intact.
	 */
	public static void setTrackingRange(World world, Entity e, int range) {
		if(!(world instanceof WorldServer)) return;

		EntityTrackerEntry entry = getTrackerEntry((WorldServer) world, e.getEntityId());
		if(entry == null) return;

		ReflectionHelper.setPrivateValue(EntityTrackerEntry.class, entry, range, "range", "field_73130_b");
	}
}
