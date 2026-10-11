package com.hbm.util;

/** Cached millisecond timestamp. Refreshed once per tick by the main loop, sparing the hot path endless System.currentTimeMillis(). */
public final class Clock {

	private static long time_ms;

	private Clock() { }

	public static void update() {
		time_ms = System.currentTimeMillis();
	}

	public static long get_ms() {
		return time_ms;
	}
}
