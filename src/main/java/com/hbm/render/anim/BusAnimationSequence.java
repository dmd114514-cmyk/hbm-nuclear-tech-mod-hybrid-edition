package com.hbm.render.anim;

import java.util.ArrayList;
import java.util.List;

import com.hbm.render.anim.BusAnimationKeyframe.IType;

// The actual "film strip": a run of keyframes, each carrying its own behavior
public class BusAnimationSequence {

	public static enum Dimension {
		TX,
		TY,
		TZ,
		RX,
		RY,
		RZ,
		SX,
		SY,
		SZ
	}


	// Each keyframe carries one dimension only, and the dimensions can lay their frames at different times
	private List<List<BusAnimationKeyframe>> transformKeyframes = new ArrayList<List<BusAnimationKeyframe>>(9);

	public double[] offset = new double[3];

	// Rotation axis order
	public double[] rotMode = new double[] { 0, 1, 2 };

	public BusAnimationSequence() {
		// Nine dimensions, each holding a column of keyframes
		for(int i = 0; i < 9; i++) {
			transformKeyframes.add(new ArrayList<BusAnimationKeyframe>());
		}
	}

	// Appends a keyframe to the given dimension
	public BusAnimationSequence addKeyframe(Dimension dimension, BusAnimationKeyframe keyframe) {
		transformKeyframes.get(dimension.ordinal()).add(keyframe);
		return this;
	}

	public BusAnimationSequence addKeyframe(Dimension dimension, double value, int duration) {
		return addKeyframe(dimension, new BusAnimationKeyframe(value, duration));
	}

	/** A displacement frame of zero duration */
	public BusAnimationSequence setPos(double x, double y, double z) {
		return addPos(x, y, z, 0, IType.LINEAR);
	}

	/** A linearly interpolated displacement frame */
	public BusAnimationSequence addPos(double x, double y, double z, int duration) {
		return addPos(x, y, z, duration, IType.LINEAR);
	}

	/** A displacement frame with a specified interpolation mode */
	public BusAnimationSequence addPos(double x, double y, double z, int duration, IType type) {
		addKeyframe(Dimension.TX, new BusAnimationKeyframe(x, duration, type));
		addKeyframe(Dimension.TY, new BusAnimationKeyframe(y, duration, type));
		addKeyframe(Dimension.TZ, new BusAnimationKeyframe(z, duration, type));
		return this;
	}

	public BusAnimationSequence addRot(double x, double y, double z, int duration) {
		addKeyframe(Dimension.RX, new BusAnimationKeyframe(x, duration));
		addKeyframe(Dimension.RY, new BusAnimationKeyframe(y, duration));
		addKeyframe(Dimension.RZ, new BusAnimationKeyframe(z, duration));
		return this;
	}

	/** Pins one more frame at the current value — the animation pauses in place for the given milliseconds */
	public BusAnimationSequence hold(int duration) {
		addKeyframe(Dimension.TX, new BusAnimationKeyframe(getLast(Dimension.TX), duration));
		addKeyframe(Dimension.TY, new BusAnimationKeyframe(getLast(Dimension.TY), duration));
		addKeyframe(Dimension.TZ, new BusAnimationKeyframe(getLast(Dimension.TZ), duration));
		return this;
	}

	/** Holds until the given timestamp, used to align several buses onto one beat */
	public BusAnimationSequence holdUntil(int end) {
		int duration = end - getTotalTime();
		//FIXME: this drifts when playback speed isn't 1
		return hold(duration);
	}

	public BusAnimationSequence multiplyTime(double mult) {

		for(Dimension dim : Dimension.values()) {
			List<BusAnimationKeyframe> keyframes = transformKeyframes.get(dim.ordinal());
			for(BusAnimationKeyframe keyframe : keyframes) {
				keyframe.duration = (int) (keyframe.originalDuration * mult);
			}
		}
		return this;
	}

	/** The latest frame's value in that dimension */
	private double getLast(Dimension dim) {
		BusAnimationKeyframe frame = getLastFrame(dim);
		return frame != null ? frame.value : 0D;
	}

	private BusAnimationKeyframe getLastFrame(Dimension dim) {
		List<BusAnimationKeyframe> keyframes = transformKeyframes.get(dim.ordinal());
		if(keyframes.isEmpty()) return null;
		return keyframes.get(keyframes.size() - 1);
	}

	//All transforms are absolute; additive transforms aren't implemented
	public double[] getTransformation(int millis) {
		double[] transform = new double[15];

		for(int i = 0; i < 9; i++) {
			List<BusAnimationKeyframe> keyframes = transformKeyframes.get(i);

			BusAnimationKeyframe currentFrame = null;
			BusAnimationKeyframe previousFrame = null;

			int startTime = 0;
			int endTime = 0;
			for(BusAnimationKeyframe keyframe : keyframes) {
				startTime = endTime;
				endTime += keyframe.duration;
				previousFrame = currentFrame;
				currentFrame = keyframe;
				if(millis < endTime) break;
			}

			if(currentFrame == null) {
				// Scale defaults to 1, everything else to 0
				transform[i] = i >= 6 ? 1 : 0;
				continue;
			}

			if(millis >= endTime || currentFrame.duration == 0) {
				transform[i] = currentFrame.value;
				continue;
			}

			if(previousFrame != null && previousFrame.interpolationType == IType.CONSTANT) {
				transform[i] = previousFrame.value;
				continue;
			}

			transform[i] = currentFrame.interpolate(startTime, millis, previousFrame);
		}

		transform[9] = offset[0];
		transform[10] = offset[1];
		transform[11] = offset[2];

		transform[12] = rotMode[0];
		transform[13] = rotMode[1];
		transform[14] = rotMode[2];

		return transform;
	}

	public int getTotalTime() {
		int highestTime = 0;

		for(List<BusAnimationKeyframe> keyframes : transformKeyframes) {
			int time = 0;
			for(BusAnimationKeyframe frame : keyframes) {
				time += frame.duration;
			}

			highestTime = Math.max(time, highestTime);
		}

		return highestTime;
	}
}
