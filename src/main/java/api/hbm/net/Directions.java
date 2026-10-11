package api.hbm.net;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;

/**
 * Direction constants and offset helpers. The old atto- naming (POS_X/NEG_X...) is already burned into all the machine code,
 * so the names stay, just backed by 1.12.2's EnumFacing.
 *
 * Offsets are deliberately a switch instead of EnumFacing's getOffset methods - those names keep changing between
 * MCP mappings, and hardcoding one means recompiling the whole mod over a symbol name sooner or later.
 */
public final class Directions {

	public static final EnumFacing POS_X = EnumFacing.EAST;
	public static final EnumFacing NEG_X = EnumFacing.WEST;
	public static final EnumFacing POS_Y = EnumFacing.UP;
	public static final EnumFacing NEG_Y = EnumFacing.DOWN;
	public static final EnumFacing POS_Z = EnumFacing.SOUTH;
	public static final EnumFacing NEG_Z = EnumFacing.NORTH;

	public static final EnumFacing[] HORIZONTALS = { NEG_Z, POS_Z, NEG_X, POS_X };
	public static final EnumFacing[] ALL = { NEG_Y, POS_Y, NEG_Z, POS_Z, NEG_X, POS_X };

	private Directions() { }

	public static int offsetX(EnumFacing dir) {
		switch(dir) {
		case EAST: return 1;
		case WEST: return -1;
		default: return 0;
		}
	}

	public static int offsetY(EnumFacing dir) {
		switch(dir) {
		case UP: return 1;
		case DOWN: return -1;
		default: return 0;
		}
	}

	public static int offsetZ(EnumFacing dir) {
		switch(dir) {
		case SOUTH: return 1;
		case NORTH: return -1;
		default: return 0;
		}
	}

	/**
	 * Converts which way a machine's front faces into a rotation around the vertical axis, with north as zero.
	 * Vanilla Rotation has no such thing (it turns faces, it does not invert back to a rotation), so it stays in the facing helpers.
	 *
	 * This is the **undo table**, not the forward table that turns a north-based model to a facing - the two differ by one inversion:
	 * Measured property: `getBlockRotation(d).rotate(d) == NORTH` (EAST gives COUNTERCLOCKWISE_90).
	 * New placement/structure code almost always wants the forward table `rotationFromNorth`; this one is only kept for the door family,
	 * because they carry a -90 degree model convention.
	 */
	public static Rotation getBlockRotation(EnumFacing dir) {
		switch(dir) {
		case SOUTH: return Rotation.CLOCKWISE_180;
		case EAST: return Rotation.COUNTERCLOCKWISE_90;
		case WEST: return Rotation.CLOCKWISE_90;
		default: return Rotation.NONE;
		}
	}

	/**
	 * Forward table: the rotation needed to bring a north-facing model/offset to the target facing.
	 * Chirality taken from what vanilla actually does: `Rotation.CLOCKWISE_90.rotate(NORTH) == EAST`.
	 */
	public static Rotation rotationFromNorth(EnumFacing dir) {
		switch(dir) {
		case NORTH: return Rotation.NONE;
		case EAST: return Rotation.CLOCKWISE_90;
		case SOUTH: return Rotation.CLOCKWISE_180;
		case WEST: return Rotation.COUNTERCLOCKWISE_90;
		default: return Rotation.NONE;
		}
	}
}
