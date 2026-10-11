package com.hbm.tweak;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import api.hbm.tweak.TweakDomains;

/**
 * Runtime value tweak table, keyed by domain/target/field, tracking current value, default and source.
 */
public final class HbmTweaks {

	/** The source reported externally when nothing overrides: keeps "default" and "modified" apart */
	public static final String SOURCE_DEFAULT = "default";
	public static final String SOURCE_SET = "set";
	public static final String SOURCE_MULTIPLY = "multiply";

	/** Which channel this override came in from: a script or a config file (§14.4 has both writing the same table, so the source must stay separable) */
	public static final String ORIGIN_SCRIPT = "script";
	public static final String ORIGIN_JSON = "json";

	private static final float DEFAULT_VALUE = 1.0F;

	/** Carries its own log sink, so it doesn't depend on a global logger that may not be initialized yet */
	private static final Logger LOGGER = LogManager.getLogger("hbm-tweaks");

	private static final Map<String, Entry> ENTRIES = new LinkedHashMap<String, Entry>();

	private HbmTweaks() {
	}

	private static final class Entry {
		float value = DEFAULT_VALUE;
		float defaultValue = DEFAULT_VALUE;
		String source = SOURCE_DEFAULT;
		String origin = ORIGIN_SCRIPT;
	}

	private static String key(String domain, String target, String field) {
		return (domain == null ? "" : domain) + "\u0001" + (target == null ? "" : target) + "\u0001" + (field == null ? "" : field);
	}

	/** Illegal combos get one log line and are dropped, not written into the override table */
	private static boolean rejectIfIllegal(String domain, String target, String field) {
		if (TweakDomains.isLegalField(domain, field)) {
			return false;
		}
		LOGGER.warn("[tweaks] illegal field combination ignored: domain={} target={} field={}", domain, target, field);
		return true;
	}

	private static Entry entry(String domain, String target, String field) {
		String k = key(domain, target, field);
		Entry e = ENTRIES.get(k);
		if (e == null) {
			e = new Entry();
			ENTRIES.put(k, e);
		}
		return e;
	}

	public static void set(String domain, String target, String field, float v) {
		set(domain, target, field, v, ORIGIN_SCRIPT);
	}

	public static void set(String domain, String target, String field, float v, String origin) {
		if (rejectIfIllegal(domain, target, field)) return;
		Entry e = entry(domain, target, field);
		e.value = v;
		e.source = SOURCE_SET;
		e.origin = origin;
	}

	/** Multiplies on top of the default, so repeat calls don't stack */
	public static void multiply(String domain, String target, String field, float f) {
		multiply(domain, target, field, f, ORIGIN_SCRIPT);
	}

	/** Multiplies on top of the default, so repeat calls don't stack */
	public static void multiply(String domain, String target, String field, float f, String origin) {
		if (rejectIfIllegal(domain, target, field)) return;
		Entry e = entry(domain, target, field);
		e.value = e.defaultValue * f;
		e.source = SOURCE_MULTIPLY;
		e.origin = origin;
	}

	public static void global(String field, float v) {
		set(TweakDomains.GLOBAL, GLOBAL_TARGET, field, v);
	}

	public static float get(String domain, String target, String field) {
		Entry e = ENTRIES.get(key(domain, target, field));
		return e == null ? DEFAULT_VALUE : e.value;
	}

	/** The target placeholder used by the catch-all domain record; global() writes it, resolve() reads it — both sides must spell it identically */
	public static final String GLOBAL_TARGET = "*";

	/**
	 * The front door for consumers: check this target's own override first, fall back to the catch-all domain, then return the default untouched.
	 *
	 * <p>Call sites should only know this one method — it keeps the default/current/source layers in one place, and changes land immediately (computed on every read),
	 * no startup snapshot. The defaultValue passed in is the mod's hard-coded original; if no script touched it, it comes back as-is.</p>
	 */
	public static float resolve(String domain, String target, String field, float defaultValue) {
		Entry own = ENTRIES.get(key(domain, target, field));
		if (own != null) {
			return own.value;
		}
		Entry glob = ENTRIES.get(key(domain, GLOBAL_TARGET, field));
		if (glob != null) {
			return glob.value;
		}
		return defaultValue;
	}

	/** For call sites that only want a multiplier: returns 1.0 when the catch-all domain isn't overridden, i.e. no change */
	public static float factor(String domain, String target, String field) {
		return resolve(domain, target, field, DEFAULT_VALUE);
	}

	public static float getDefault(String domain, String target, String field) {
		Entry e = ENTRIES.get(key(domain, target, field));
		return e == null ? DEFAULT_VALUE : e.defaultValue;
	}

	public static String getSource(String domain, String target, String field) {
		Entry e = ENTRIES.get(key(domain, target, field));
		return e == null ? SOURCE_DEFAULT : e.source;
	}

	/** Whether the override came from a script or a config file */
	public static String getOrigin(String domain, String target, String field) {
		Entry e = ENTRIES.get(key(domain, target, field));
		return e == null ? SOURCE_DEFAULT : e.origin;
	}

	public static void reset() {
		ENTRIES.clear();
	}

	public static void reset(String domain) {
		String prefix = (domain == null ? "" : domain) + "\u0001";
		ENTRIES.keySet().removeIf(k -> k.startsWith(prefix));
	}

	/** Self-test: the set/multiply/reset/illegal-combo semantics; prints OK only when all pass */
	public static void selfTest() {
		reset();
		String d = TweakDomains.GUN;
		String t = "testGun";
		String f = "damage";

		check(get(d, t, f) == 1.0F, "initial value should be the default");
		set(d, t, f, 42.0F);
		check(get(d, t, f) == 42.0F, "after set, the current value should equal the set value");
		check(getDefault(d, t, f) == 1.0F, "set should not change the default value");
		check(SOURCE_SET.equals(getSource(d, t, f)), "after set, the source should be set");

		multiply(d, t, f, 3.0F);
		check(get(d, t, f) == 3.0F, "multiply should compute relative to the default value");
		check(SOURCE_MULTIPLY.equals(getSource(d, t, f)), "after multiply, the source should be multiply");

		System.out.println(String.format(java.util.Locale.ROOT, "default=%s current=%s source=%s",
			getDefault(d, t, f), get(d, t, f), getSource(d, t, f)));

		// resolve/factor are the entry points call sites actually use, so their semantics get verified separately: default when unoverridden, current once overridden
		reset();
		check(resolve(d, t, f, 7.5F) == 7.5F, "with no override, resolve should return the default value untouched");
		set(d, t, f, 3.0F);
		check(resolve(d, t, f, 7.5F) == 3.0F, "resolve should prefer this target's override");
		reset();
		set(d, HbmTweaks.GLOBAL_TARGET, f, 2.0F);
		check(resolve(d, t, f, 7.5F) == 2.0F, "when this target has no override, it should fall back to the same domain's * entry");
		check(factor(d, t, f) == 2.0F, "factor and resolve should share the same fallback");
		reset();
		check(resolve(d, t, f, 7.5F) == 7.5F && factor(d, t, f) == 1.0F, "after reset, both should return to factory semantics");
		global("damage", 4.0F);
		check(get(TweakDomains.GLOBAL, HbmTweaks.GLOBAL_TARGET, "damage") == 4.0F, "global should land on the global domain's * target");
		reset();

		float keep = get(d, t, f);
		set("bogus", t, f, 999.0F);
		set(d, t, "radius", 999.0F);
		check(get(d, t, f) == keep, "an illegal combination should not change the existing value");
		check(get("bogus", t, f) == 1.0F, "an illegal combination should not write into the override table");

		reset();
		check(get(d, t, f) == 1.0F && getDefault(d, t, f) == 1.0F, "after reset, should return to the default value");
		check(SOURCE_DEFAULT.equals(getSource(d, t, f)), "after reset, the source should return to default");

		System.out.println("selfTest: OK");
	}

	private static void check(boolean ok, String msg) {
		if (!ok) {
			throw new IllegalStateException("selfTest failed: " + msg);
		}
	}

	/** Dumps the override table as text; untouched combos don't appear */
	public static String dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("tweaks(").append(ENTRIES.size()).append("):");
		for (Map.Entry<String, Entry> me : ENTRIES.entrySet()) {
			String[] parts = me.getKey().split("\u0001", -1);
			Entry e = me.getValue();
			sb.append("\n  ").append(parts[0]).append('/').append(parts[1]).append('/').append(parts[2])
				.append(" = ").append(e.value)
				.append(" (default ").append(e.defaultValue)
				.append(", ").append(e.source).append(", ").append(e.origin).append(')');
		}
		return sb.toString();
	}
}
