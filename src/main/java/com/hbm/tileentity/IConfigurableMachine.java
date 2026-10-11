package com.hbm.tileentity;

import java.io.IOException;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

public interface IConfigurableMachine {

	/* This machine's name inside the JSON */
	public String getConfigName();
	/* Reads the JSON and applies parameters; missing values fall back to defaults, no errors */
	public void readIfPresent(JsonObject obj);
	/* Writes the current parameters out whole */
	public void writeConfig(JsonWriter writer) throws IOException;

	public static boolean grab(JsonObject obj, String name, boolean def) {
		return obj.has(name) ? obj.get(name).getAsBoolean() : def;
	}

	public static int grab(JsonObject obj, String name, int def) {
		return obj.has(name) ? obj.get(name).getAsInt() : def;
	}

	public static long grab(JsonObject obj, String name, long def) {
		return obj.has(name) ? obj.get(name).getAsLong() : def;
	}

	public static double grab(JsonObject obj, String name, double def) {
		return obj.has(name) ? obj.get(name).getAsDouble() : def;
	}
}
