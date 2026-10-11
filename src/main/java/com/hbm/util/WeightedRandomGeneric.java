package com.hbm.util;

import net.minecraft.util.WeightedRandom;

/**
 * Typed weighted entries. Vanilla's WeightedRandom only hands back Items, leaving the cast to callers,
 * and binding the generic saves one cast per call site and one chance to cast wrong.
 */
public class WeightedRandomGeneric<T> extends WeightedRandom.Item {

	private T item;

	public WeightedRandomGeneric(T o, int weight) {
		super(weight);
		item = o;
	}

	public T get() {
		return item;
	}
}
