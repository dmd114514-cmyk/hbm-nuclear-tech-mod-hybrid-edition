package com.hbm.util.i18n;

import net.minecraft.util.text.translation.I18n;

/**
 * The translation facade. Sits directly on vanilla's (deprecated but alive in 1.12.2) I18n,
 * safe to call server-side too — with no lang file it returns the key verbatim, a natural fallback.
 *
 * Note: autoBreak's pixel-width wrapping belongs to the GUI layer; it arrives with the GUI migration.
 */
public final class I18nUtil {

	private I18nUtil() { }

	public static String resolveKey(String key, Object... args) {
		return I18n.translateToLocalFormatted(key, args);
	}

	/** An alias for resolveKey; old code used both spellings */
	public static String format(String key, Object... args) {
		return I18n.translateToLocalFormatted(key, args);
	}

	/**
	 * NTM's line break is '$', split after translating,
	 * letting one long lang line control its own wrap points.
	 */
	public static String[] resolveKeyArray(String key, Object... args) {
		return resolveKey(key, args).split("\\$");
	}
}
