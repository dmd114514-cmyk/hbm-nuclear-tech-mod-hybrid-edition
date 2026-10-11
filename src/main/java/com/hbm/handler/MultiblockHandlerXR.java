package com.hbm.handler;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The multiblock "placeholder block" laying method: checkSpace sees if the space is empty, fillSpace lays the surrounding ring as dummy blocks.
 *
 * The six-way dim array orders U D N S W E; rotate() first converts it per structure facing into the reach along each direction
 * "with that facing as forward", and the triple loop after works off that one copy.
 *
 * Two changes required by this version (neither alters semantics):
 *   · The old fillSpace set BlockDummyable.safeRem = true, laid the ring, then cleared it — a master switch telling dummy blocks
 *     "this batch, don't tear down your own siblings". That field lived on the then-unlanded BlockDummyable; rather than deadlock this chain,
 *     **don't keep a second same-named static** — two flags flipped independently makes dummy cleanup timing a coin flip again.
 *   · Meta now uses EnumFacing.ordinal(): this version is DOWN=0 UP=1 NORTH=2 SOUTH=3 WEST=4 EAST=5;
 *     the old 1.7.10 ForgeDirection was checked against its source file (ForgeDirection.java lines 4-26 in the 1.7.10 MDK)
 *     as DOWN=0 UP=1 NORTH=2 SOUTH=3 WEST=4 EAST=5 with **UNKNOWN=6 at the end** (its own comment reads
 *     "Used only by getOrientation, for invalid inputs") ⇒ **both sides number identically, nobody subtracts 1** (an earlier note here said "off by one", which was wrong,
 *     don't "correct" the numbers along that line). So: always number via the enum, never hard-code digits, and BlockDummyable on the other side reads the same way.
 */
public class MultiblockHandlerXR {

	// While this batch lays, dummies must not tear down their siblings; the flag opens and closes as fillSpace enters and exits
	public static boolean safeRem = false;

	// Reach along each direction when facing due south (no diagram consulted)
	//											U  D  N  S  W  E
	public static int[] uni = new int[] { 3, 0, 4, 4, 4, 4 };

	// ox/oy/oz are placeholder params left by the old signature; the body ignores them; kept so callers write as before
	public static boolean checkSpace(World world, int x, int y, int z, int[] dim, int ox, int oy, int oz, EnumFacing dir) {

		if(dim == null || dim.length != 6) return false;

		int count = 0;

		int[] rot = rotate(dim, dir);

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {

					BlockPos pos = new BlockPos(a, b, c);
					IBlockState state = world.getBlockState(pos);

					if(!state.getBlock().isReplaceable(world, pos)) {
						return false;
					}

					count++;

					if(count > 2000) {
						System.out.println("checkspace: ded " + a + " " + b + " " + c + " " + x + " " + y + " " + z);
						return false;
					}
				}
			}
		}

		return true;
	}

	public static void fillSpace(World world, int x, int y, int z, int[] dim, Block block, EnumFacing dir) {

		if(dim == null || dim.length != 6) return;

		int count = 0;

		int[] rot = rotate(dim, dir);

		safeRem = true;

		for(int a = x - rot[4]; a <= x + rot[5]; a++) {
			for(int b = y - rot[1]; b <= y + rot[0]; b++) {
				for(int c = z - rot[2]; c <= z + rot[3]; c++) {

					EnumFacing facing;

					// The far cell from center wins: vertical first, then east/west, then north/south; the exact center cell stays dummy-free
					if(b < y) {
						facing = EnumFacing.DOWN;
					} else if(b > y) {
						facing = EnumFacing.UP;
					} else if(a < x) {
						facing = EnumFacing.WEST;
					} else if(a > x) {
						facing = EnumFacing.EAST;
					} else if(c < z) {
						facing = EnumFacing.NORTH;
					} else if(c > z) {
						facing = EnumFacing.SOUTH;
					} else {
						continue;
					}

					world.setBlockState(new BlockPos(a, b, c), block.getStateFromMeta(facing.ordinal()), 3);

					count++;

					if(count > 2000) {
						System.out.println("fillspace: ded " + a + " " + b + " " + c + " " + x + " " + y + " " + z);

						safeRem = false;
						return;
					}
				}
			}
		}

		safeRem = false;
	}

	public static int[] rotate(int[] dim, EnumFacing dir) {

		if(dim == null) return null;
		if(dir == EnumFacing.SOUTH) return dim;

		if(dir == EnumFacing.NORTH) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[3], dim[2], dim[5], dim[4] };
		}

		if(dir == EnumFacing.EAST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[5], dim[4], dim[2], dim[3] };
		}

		if(dir == EnumFacing.WEST) {
			//                 U       D       N       S       W       E
			return new int[] { dim[0], dim[1], dim[4], dim[5], dim[3], dim[2] };
		}

		return dim;
	}
}
