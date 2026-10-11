package com.hbm.util;

import com.hbm.interfaces.HalfLifeType;

/**
 * RTG pellet lifespan and heat-value settlement.
 *
 * Of the old four members this version lands <code>getLifespan</code> first: pure arithmetic, reading only {@link HalfLifeType},
 * which is exactly the number the fuel side (the <code>pellet_rtg_*</code> declarations) wants.
 *
 * The remaining three — <code>getPower</code> / <code>hasHeat</code> / <code>updateRTGs</code> — didn't come this round,
 * not overlooked: their signatures and bodies all sit on <code>com.hbm.items.machine.ItemRTGPellet</code>
 * (<code>getScaledPower</code>, <code>getHeat</code>, <code>handleDecay</code> must be read from it),
 * and that class doesn't exist in any branch of this version. They land with the machines that eat them;
 * writing them now would just be shells that always return 0.
 *
 * The old callers were: {@code TileEntityDiFurnaceRTG:159}, {@code TileEntityMachineIGenerator:163-164}, and friends
 * {@code TileEntityMachineRadiolysis:108}、{@code TileEntityRtgFurnace:112/272/282}。
 * By the way: {@code VersatileConfig.rtgDecay()} and {@code scaleRTGPower()} already exist in this version,
 * so adding <code>getPower</code> later won't need config changes.
 */
public class RTGUtil {

	/**
	 * Computes how long this RTG lasts from its half-life, in Minecraft ticks.
	 *
	 * Three conversion tiers: {@code SHORT} by days (48000 ticks a day), {@code MEDIUM} by years, {@code LONG} by centuries.
	 * The "year" in the latter two is set by <code>realYears</code> — true means 365 days, false means 100 days,
	 * i.e. with it off a year is a hundred days and lifespans shrink by more than half.
	 *
	 * Intermediate values stay in float, matching the old shape: at century scale times long half-lives, float already can't reach the ones digit,
	 * and switching to long would desync these long-lived RTGs' seconds against old saves.
	 *
	 * @param halfLife   the half-life value
	 * @param type       the half-life's unit magnitude
	 * @param realYears  whether year/century tiers fold by 365 days or 100
	 * @return lifespan, in ticks
	 */
	public static long getLifespan(float halfLife, HalfLifeType type, boolean realYears) {
		float life = 0;
		switch (type)
		{
		case LONG:
			life = (48000 * (realYears ? 365 : 100) * 100) * halfLife;
			break;
		case MEDIUM:
			life = (48000 * (realYears ? 365 : 100)) * halfLife;
			break;
		case SHORT:
			life = 48000 * halfLife;
			break;
		}
		return (long) life;
	}
}
