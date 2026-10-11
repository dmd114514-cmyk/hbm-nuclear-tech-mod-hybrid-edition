package com.hbm.render.anim;

import java.util.HashMap;
import java.util.Map.Entry;

// An animation system that "calls itself simple": back then Collada wasn't taken, just for the fun of building it
public class BusAnimation {

	// One bus handles one thing: a part's displacement, or a whole-body rotation; several parts with identical animations may share one
	private final HashMap<String, BusAnimationSequence> animationBuses = new HashMap<String, BusAnimationSequence>();
	// Always equal to the longest sequence's duration
	private int totalTime = 0;

	/**
	 * Hangs a bus onto the animation
	 * @param name the bus name
	 * @param bus the bus itself
	 */
	public BusAnimation addBus(String name, BusAnimationSequence bus) {

		animationBuses.put(name, bus);

		int duration = bus.getTotalTime();

		if(duration > totalTime)
			totalTime = duration;

		return this;
	}

	/** Used to recompute total duration when keyframes are appended to a sequence after the fact */
	public void updateTime() {

		for(Entry<String, BusAnimationSequence> sequence : animationBuses.entrySet()) {

			int time = sequence.getValue().getTotalTime();

			if(time > totalTime)
				totalTime = time;
		}
	}

	/** Fetches a bus by name; you don't usually call this directly */
	public BusAnimationSequence getBus(String name) {
		return animationBuses.get(name);
	}

	/** Scales all keyframe durations by one factor; below 1 means faster playback */
	public void setTimeMult(double mult) {
		for(Entry<String, BusAnimationSequence> sequence : animationBuses.entrySet()) {
			sequence.getValue().multiplyTime(mult);
		}
	}

	/**
	 * The state of a given bus at a given millisecond
	 * @param name the bus name
	 * @param millis milliseconds elapsed since the animation started
	 */
	public double[] getTimedTransformation(String name, int millis) {

		if(this.animationBuses.containsKey(name))
			return animationBuses.get(name).getTransformation(millis);

		return null;
	}

	public int getDuration() {
		return totalTime;
	}

}
