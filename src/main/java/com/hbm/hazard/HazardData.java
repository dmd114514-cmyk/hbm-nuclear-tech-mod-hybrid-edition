package com.hbm.hazard;

import java.util.ArrayList;
import java.util.List;

import com.hbm.hazard.type.HazardTypeBase;

/**
 * One registration unit: the package of hazards behind an ore dict name / item / item stack.
 */
public class HazardData {

	/*
	 * Reading this wipes everything accumulated so far, which is how one item completely overrides the ore-dict level config.
	 */
	boolean doesOverride = false;
	/*
	 * Mutex bit. Finer than an override: only make specific entries yield to each other, the way the ore dict aliases plutonium238 and pu238 do.
	 * The opposite of an override - if this entry collides with one already registered, this is the one that yields.
	 *
	 * Bits in use (keep this in sync when you change them)
	 * -1: ore dict (ingotX)
	 */
	int mutexBits = 0b0000_0000_0000_0000_0000_0000_0000_0000;

	List<HazardEntry> entries = new ArrayList();

	public HazardData addEntry(HazardTypeBase hazard) {
		return this.addEntry(hazard, 1F, false);
	}

	public HazardData addEntry(HazardTypeBase hazard, float level) {
		return this.addEntry(hazard, level, false);
	}

	public HazardData addEntry(HazardTypeBase hazard, float level, boolean override) {
		this.entries.add(new HazardEntry(hazard, level));
		this.doesOverride = override;
		return this;
	}

	public HazardData addEntry(HazardEntry entry) {
		this.entries.add(entry);
		return this;
	}

	public HazardData setMutex(int mutex) {
		this.mutexBits = mutex;
		return this;
	}

	public int getMutex() {
		return mutexBits;
	}
}
