package com.hbm.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Collection;
import java.math.RoundingMode;

import net.minecraft.util.math.MathHelper;

/** Math odds and ends. All pure functions; no state lives here. */
public final class BobMathUtil {

	private BobMathUtil() { }

	/*Maps one range linearly onto another */
	public static float remap(float num, float min1, float max1, float min2, float max2) {
		return ((num - min1) / (max1 - min1)) * (max2 - min2) + min2;
	}

	/** Normalizes to 0..1 without clamping */
	public static float remap01(float num, float min1, float max1) {
		return (num - min1) / (max1 - min1);
	}

	/** Normalizes to 0..1 and clamps; UI progress bars live on this */
	public static float remap01_clamp(float num, float min1, float max1) {
		return MathHelper.clamp((num - min1) / (max1 - min1), 0F, 1F);
	}

	/** A half-second blink toggle, for warning lamps and flashing text */
	public static boolean getBlink() {
		return System.currentTimeMillis() % 1000 < 500;
	}

	/** Big-number shorthand: 1.5M / 2.3G style, for panel displays */
	public static String getShortNumber(long l) {

		double res;
		String suffix = "";
		long abs = Math.abs(l);

		if(abs >= Math.pow(10, 18)) {
			res = l / Math.pow(10, 18);
			suffix = "E";
		} else if(abs >= Math.pow(10, 15)) {
			res = l / Math.pow(10, 15);
			suffix = "P";
		} else if(abs >= Math.pow(10, 12)) {
			res = l / Math.pow(10, 12);
			suffix = "T";
		} else if(abs >= Math.pow(10, 9)) {
			res = l / Math.pow(10, 9);
			suffix = "G";
		} else if(abs >= Math.pow(10, 6)) {
			res = l / Math.pow(10, 6);
			suffix = "M";
		} else if(abs >= Math.pow(10, 3)) {
			res = l / Math.pow(10, 3);
			suffix = "k";
		} else {
			return Long.toString(l);
		}

		return String.format("%.1f%s", res, suffix);
	}

	/** Rounds by digit count. BigDecimal instead of multiplying by 10^n, dodging the 0.1+0.2 float tail */
	public static double roundDecimal(double num, int digits) {
		if(digits < 0)
			throw new IllegalArgumentException("Attempted negative number in non-negative field! Attempted value: " + digits);

		return new BigDecimal(num).setScale(digits, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * The radiation decay curve: flattens the high-dose end while keeping the low end from hitting zero outright.
	 * Dose conversion leans on it everywhere; changing it is changing global balance. The neutron hazard now calls this too, so the formula isn't written twice.
	 */
	public static double squirt(double x) {
		return Math.sqrt(x + 1D / ((x + 2D) * (x + 2D))) - 1D / (x + 2D);
	}

	/** Formats a 0~1 ratio as a percentage string; decimal places follow the platform locale, as before */
	public static String toPercentage(float amount, float total) {
		return NumberFormat.getPercentInstance().format(amount / total);
	}

	/**
	 * Splits ticks into years/days/tenths-of-a-day; radioactive fuel lifespans live on this tooltip.
	 *
	 * Conversions as before: 48000 ticks a day, 100 days a year. Returns a **ready-formatted string**,
	 * the third segment being the 0~10 notch, not raw ticks — append it directly, don't multiply back.
	 */
	public static String[] ticksToDate(long ticks) {

		final int tickDay = 48000;
		final int tickYear = tickDay * 100;

		String[] dateOut = new String[3];

		long year = Math.floorDiv(ticks, tickYear);
		byte day = (byte) Math.floorDiv(ticks - tickYear * year, tickDay);
		float time = ticks - (tickYear * year + tickDay * day);
		time = (float) convertScale(time, 0, tickDay, 0, 10F);

		dateOut[0] = String.valueOf(year);
		dateOut[1] = String.valueOf(day);
		dateOut[2] = String.valueOf(time);

		return dateOut;
	}

	/*Moves a number from one range to another linearly; both edges are doubles, not an integer ratio */
	public static double convertScale(double toScale, double oldMin, double oldMax, double newMin, double newMax) {
		double prevRange = oldMax - oldMin;
		double newRange = newMax - newMin;
		return (((toScale - oldMin) * newRange) / prevRange) + newMin;
	}

	/*The minimal angle between two facings, landing in -180~180; turret tracking's "how many degrees left" uses it */
	public static double angularDifference(double alpha, double beta) {
		double delta = (beta - alpha + 180) % 360 - 180;
		return delta < -180 ? delta + 360 : delta;
	}

	/*Flattens an Integer collection into int[]; the copy wand's settings use it to write fluid ids into an NBT int array */
	public static int[] intCollectionToArray(Collection<Integer> in) {
		int[] out = new int[in.size()];
		int i = 0;
		for(Integer value : in) {
			out[i++] = value.intValue();
		}
		return out;
	}
}
