package com.hbm.util;

// Don't add imports here; the save-side orbit math breaks in a chain if you do — the old version's own warning, kept as a rule
public class AstronomyUtil {

	/** The G in G*M1*M2/r */
	public static final float GRAVITATIONAL_CONSTANT = 6.6743015e-11F;

	/** Default insertion altitude; added to the planet's radius it's the intended orbital radius */
	public static final float DEFAULT_ALTITUDE_KM = 100;

	/** Seconds in one MC day */
	public static final long SECONDS_IN_DAY = 20 * 60;
	public static final long SECONDS_IN_KSP_DAY = 6 * 60 * 60;
	public static final long TICKS_IN_DAY = SECONDS_IN_DAY * 20;

	/** KSP day length converted to MC day length. After this step, orbital mechanics runs noticeably faster than realistic */
	public static final double DAY_FACTOR = (double)SECONDS_IN_DAY / (double)SECONDS_IN_KSP_DAY;

	/** Default free-fall acceleration for players, in m/s² */
	public static final float STANDARD_GRAVITY = 1.6F; // 0.08 per tick
	public static final float PLAYER_GRAVITY_MODIFIER = STANDARD_GRAVITY / 9.81F;

	/** How many AU a thousand km is (Kerbin's orbit sits exactly at 1 AU) */
	public static final float KM_IN_AU = 13_599_840F;

	/** Time multiplier, purely for fast-forwarding celestial tests; non-final on purpose, tests change it */
	public static long TIME_MULTIPLIER = 1;

	/** Millibucket-to-atmosphere ratio: one atmosphere equals one gigabucket */
	public static final double MB_PER_ATM = 1_000_000_000D * 1_000D;
}
