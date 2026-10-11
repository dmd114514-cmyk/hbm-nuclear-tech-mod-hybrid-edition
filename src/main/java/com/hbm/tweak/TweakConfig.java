package com.hbm.tweak;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import api.hbm.tweak.TweakDomains;

/**
 * JSON source for the tweak table: lets packs without CraftTweaker adjust values from a config file.
 *
 * Reads `config/hbm/hbm-tweaks.json`, shaped like
 * <pre>{ "gun": { "g12": { "damage": 4.0 } } }</pre>
 * A bare number means an **absolute value**; to express a multiplier write
 * <pre>{ "gun": { "g12": { "damage": { "multiply": 1.5 } } } }</pre>
 *
 * Domains/fields still go through {@link TweakDomains}'s whitelist; the table refuses illegal combos itself.
 * A parse failure logs one line and treats the whole table as absent — one broken JSON shouldn't keep the game from starting.
 */
public final class TweakConfig {

	/** Same directory as the main config, fixed name; no template gets generated, no point spawning a file for people who never use it */
	public static final String FILE_NAME = "hbm-tweaks.json";

	private TweakConfig() { }

	/**
	 * Call once in preInit (scripts run later in the registration phase, so when both write the same table scripts naturally win; see §14.4).
	 *
	 * @return how many overrides actually took effect; 0 when the file is missing
	 */
	public static int loadJson() {

		File file = new File(com.hbm.config.ConfigManager.configDir, FILE_NAME);
		if(!file.isFile())
			return 0;

		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "UTF-8")) {

			JsonElement root = new JsonParser().parse(reader);
			if(root == null || !root.isJsonObject()) {
				com.hbm.util.LoggingUtil.warn("[hbm] {} is not a JSON object; ignored whole", FILE_NAME);
				return 0;
			}

			int applied = 0;

			for(Map.Entry<String, JsonElement> domain : root.getAsJsonObject().entrySet()) {
				if(!domain.getValue().isJsonObject()) continue;

				for(Map.Entry<String, JsonElement> target : domain.getValue().getAsJsonObject().entrySet()) {
					if(!target.getValue().isJsonObject()) continue;

					for(Map.Entry<String, JsonElement> field : target.getValue().getAsJsonObject().entrySet()) {
						if(apply(domain.getKey(), target.getKey(), field.getKey(), field.getValue()))
							applied++;
					}
				}
			}

			com.hbm.util.LoggingUtil.info("[hbm] numeric table loaded from {} with {} override entries", FILE_NAME, applied);
			return applied;

		} catch(Exception ex) {
			com.hbm.util.LoggingUtil.warn("[hbm] {} failed to parse; ignored whole: {}", FILE_NAME, ex);
			return 0;
		}
	}

	/** Bare number = absolute value; an object with set/multiply = pick one semantic per field; malformed entries are skipped */
	private static boolean apply(String domain, String target, String field, JsonElement value) {

		// The whitelist runs here first: the table's rejection path only logs and doesn't report back, so the counting is on us to get right
		if(!TweakDomains.isLegalField(domain, field))
			return false;

		if(value.isJsonPrimitive()) {
			HbmTweaks.set(domain, target, field, value.getAsFloat(), HbmTweaks.ORIGIN_JSON);
			return true;
		}

		if(!value.isJsonObject())
			return false;

		JsonObject object = value.getAsJsonObject();

		if(object.has("multiply")) {
			HbmTweaks.multiply(domain, target, field, object.get("multiply").getAsFloat(), HbmTweaks.ORIGIN_JSON);
			return true;
		}
		if(object.has("set")) {
			HbmTweaks.set(domain, target, field, object.get("set").getAsFloat(), HbmTweaks.ORIGIN_JSON);
			return true;
		}

		return false;
	}
}
