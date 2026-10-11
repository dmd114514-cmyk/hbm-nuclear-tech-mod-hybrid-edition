package com.hbm.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

/**
 * Mounting interface for the global heartbeat.
 * <br><br>
 * The old version inlined every "advance once per N ticks" system into the one entry event class, and anyone adding logic stuffed it into that file,
 * making it the most-churned file in the project. Now that file only counts beats; what actually happens lives in each receiver's
 * own class, hung into {@code ModEventHandler}'s list — the two sides never share a file again.
 * <br><br>
 * All three callbacks fire server-side only, at the tail end of the tick (world and entity state has settled,
 * nothing half-updated gets read). Systems that want the start-of-tick can subscribe to vanilla tick events themselves;
 * this won't dispatch both phases for them — most systems need one phase, and running half for nothing is pure loss.
 * <br><br>
 * Damage, drops, block interaction and the like don't belong here; use your own {@code @SubscribeEvent}
 * straight onto {@code MinecraftForge.EVENT_BUS}. The bus multicasts anyway; another layer of roster is just a detour.
 */
public interface IHbmTickReceiver {

	/** Name used for roster logging and self-checks; unique across the roster */
	String tickName();

	/**
	 * How many ticks between callbacks; 1 means every tick.
	 * <br><br>
	 * The modulo baseline grabs the fastest ready-made clock per context: global uses the server tick counter, world uses
	 * {@code getTotalWorldTime()}, player uses {@code ticksExisted} — so no receiver needs its own counter.
	 * The cost: a given world/player's time must increase monotonically,
	 * which is exactly what vanilla guarantees.
	 */
	default int tickInterval() { return 1; }

	/** Global beat: cross-world ledgers, network bookkeeping — things done once go here */
	default void onServerTick(MinecraftServer server) { }

	/** World beat: one callback per loaded server world; pollution/atmosphere/radiation bookkeeping goes here */
	default void onWorldTick(World world) { }

	/** Player beat: one callback per online server player; per-item settlement goes here */
	default void onPlayerTick(EntityPlayer player) { }
}
