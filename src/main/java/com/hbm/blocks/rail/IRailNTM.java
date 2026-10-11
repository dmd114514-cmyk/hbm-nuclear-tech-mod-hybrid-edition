package com.hbm.blocks.rail;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** The rail block's common face: how trains sit on it, how they move forward, whether the gauge matches. The name is admittedly too small. */
public interface IRailNTM {

	/** Given the train's position, returns the nearest snap point on this rail segment */
	public Vec3d getSnappingPos(World world, int x, int y, int z, double trainX, double trainY, double trainZ);

	/**
	 * Given the train's current motion and intended speed along the track, returns the landing point on this segment;
	 * if the train would derail this tick, what you get is the last legal position on this rail.
	 * Safer than "add displacement then snap", because that order itself causes derailments.
	 * Motion must be derived from the train's facing (reversed 180° when backing up); the scalar's size is irrelevant — it only decides direction.
	 * When a train reverses, motion ends up -1 while the pointer still faces forward; also, exit-point info is collected by RailContext.
	 */
	public Vec3d getTravelLocation(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info, MoveContext context);

	/** This rail's gauge; mismatch and the train derails */
	public TrackGauge getGauge(World world, int x, int y, int z);

	public static enum TrackGauge {
		STANDARD,	// about 1.5 meters
		NARROW		// about 0.75 meters
	}

	/** Everything a train carries away when leaving a rail */
	public static class RailContext {

		/** The angle the train should have when leaving this segment */
		public float yaw;
		/** How many blocks of travel remain after finishing this rail */
		public double overshoot;
		/** This rail's exit position */
		public BlockPos pos;

		public RailContext yaw(float y) { this.yaw = y; return this; }
		public RailContext dist(double d) { this.overshoot = d; return this; }
		public RailContext pos(BlockPos d) { this.pos = d; return this; }
	}

	/** Stop-and-go, plus what this pass is even computing (snapping or collision) as extra info */
	public static class MoveContext {

		public RailCheckType type;
		public double collisionBogieDistance;
		/** Whether a buffer stop or similar was triggered */
		public boolean collision = false;
		/** How much travel got cut off */
		public double overshoot;

		public MoveContext(RailCheckType type, double collisionBogieDistance) {
			this.type = type;
			this.collisionBogieDistance = collisionBogieDistance;
		}
	}

	public static enum RailCheckType {
		CORE,
		FRONT,
		BACK,
		OTHER
	}
}
