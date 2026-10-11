package com.hbm.render.block.ct;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

/**
 * The connected-texture context: which fragment each of a cell's six faces' four corners should wear, plus "how this judgment travels from the world to the model".
 *
 * This version's models can't reach world coordinates, yet connected textures must read neighbors. Forge's official channel is
 * Block#getExtendedState with an unlisted property: the block hangs its own (world, pos) into the state,
 * and the renderer passes that state untouched to IBakedModel#getQuads (see ModelCT).
 * Unlisted properties don't enter blockstate JSON and take no meta, so the "property-carrying blocks must list every tier" rule stays untouched.
 *
 * Each face's "up" and "left" reuse the old table, with ForgeDirection swapped for EnumFacing — the two numberings agree exactly
 * (DOWN0/UP1/NORTH2/SOUTH3/WEST4/EAST5), so indexing by number keeps working as before.
 * The same table also feeds the model side's geometry (CTContext#up/left), so the cut judgment and the quad vertices share one set of directions
 * and the two never talk past each other.
 */
public final class CTContext {

	private CTContext() { }

	/** Face ordinal (EnumFacing.ordinal) → { this face's "up", this face's "left" }, matching the old access table row for row */
	private static final EnumFacing[][] UP_LEFT = {
			{ EnumFacing.SOUTH, EnumFacing.WEST },	// DOWN's best guess
			{ EnumFacing.NORTH, EnumFacing.WEST },	// UP
			{ EnumFacing.UP, EnumFacing.EAST },		// NORTH
			{ EnumFacing.UP, EnumFacing.WEST },		// SOUTH
			{ EnumFacing.UP, EnumFacing.NORTH },	// WEST
			{ EnumFacing.UP, EnumFacing.SOUTH },		// EAST
	};

	/**
	 * Neighbor sampling table: dim1 = face ordinal, dim2 = the eight neighbors (TL,TC,TR,CL,CR,BL,BC,BR), dim3 = x/y/z, values [-1;1].
	 * Computed live from UP_LEFT, same algorithm as the old lcfs/cfs.
	 */
	private static final int[][][] ACCESS = new int[6][8][3];

	static {
		for(int side = 0; side < 6; side++) {
			EnumFacing up = UP_LEFT[side][0];
			EnumFacing left = UP_LEFT[side][1];
			EnumFacing down = up.getOpposite();
			EnumFacing right = left.getOpposite();

			ACCESS[side][0] = coord(up, left);
			ACCESS[side][1] = coord(up);
			ACCESS[side][2] = coord(up, right);
			ACCESS[side][3] = coord(left);
			ACCESS[side][4] = coord(right);
			ACCESS[side][5] = coord(down, left);
			ACCESS[side][6] = coord(down);
			ACCESS[side][7] = coord(down, right);
		}
	}

	/** Stack the directions up and you get that one neighbor's offset */
	private static int[] coord(EnumFacing... dirs) {

		int x = 0;
		int y = 0;
		int z = 0;

		for(EnumFacing dir : dirs) {
			x += dir.getFrontOffsetX();
			y += dir.getFrontOffsetY();
			z += dir.getFrontOffsetZ();
		}

		return new int[] { x, y, z };
	}

	/** This face's "up", which the model side's geometry needs */
	public static EnumFacing up(EnumFacing side) {
		return UP_LEFT[side.ordinal()][0];
	}

	/** This face's "left", i.e. the texture's negative U direction */
	public static EnumFacing left(EnumFacing side) {
		return UP_LEFT[side.ordinal()][1];
	}

	/** A render-time read-only world reference; the old one was (world, x, y, z), this version takes BlockPos */
	public static final class Source {

		public final IBlockAccess world;
		public final BlockPos pos;

		Source(IBlockAccess world, BlockPos pos) {
			this.world = world;
			this.pos = pos;
		}

		@Override
		public String toString() {
			return "ct@" + pos;
		}
	}

	private static final IUnlistedProperty<Source> PROPERTY = new IUnlistedProperty<Source>() {

		@Override
		public String getName() {
			return "hbm_ct_source";
		}

		@Override
		public boolean isValid(Source value) {
			return value != null;
		}

		@Override
		public Class<Source> getType() {
			return Source.class;
		}

		@Override
		public String valueToString(Source value) {
			return value.toString();
		}
	};

	/**
	 * Block side, step two: swap the state container for the version carrying this unlisted property.
	 *
	 * This must happen in the block's own createBlockState() — Forge's withProperty only accepts unlisted properties declared in the container,
	 * and building a container on the spot costs too much (that means recomputing every tier).
	 */
	public static BlockStateContainer container(Block block, BlockStateContainer base) {
		return new ExtendedBlockState(block, base.getProperties().toArray(new IProperty<?>[0]), new IUnlistedProperty<?>[] { PROPERTY });
	}

	/** Block side, step three: hang this cell's world coordinates onto the state */
	public static IBlockState attach(IBlockAccess world, BlockPos pos, IBlockState state) {

		if(!(state instanceof IExtendedBlockState))
			return state; // When the previous step was skipped this just falls back to the plain look; it must not blow up the render thread

		return ((IExtendedBlockState) state).withProperty(PROPERTY, new Source(world, pos));
	}

	/** Model side: read the world coordinates back from the state; nothing there means no context (item form, or the block skipped those steps) */
	public static Source source(IBlockState state) {

		if(!(state instanceof IExtendedBlockState))
			return null;

		return ((IExtendedBlockState) state).getValue(PROPERTY);
	}

	/**
	 * Fragment numbers for one cell's six faces, four corners each. Corner order = TL,TR,BL,BR, matching the model side's quad corners one to one.
	 */
	public static final class Layout {

		private final int[][] corners;

		Layout(int[][] corners) {
			this.corners = corners;
		}

		public int fragment(EnumFacing side, int corner) {
			return corners[side.ordinal()][corner];
		}

		@Override
		public boolean equals(Object o) {

			if(this == o)
				return true;
			if(!(o instanceof Layout))
				return false;

			return Arrays.deepEquals(corners, ((Layout) o).corners);
		}

		@Override
		public int hashCode() {
			return Arrays.deepHashCode(corners);
		}
	}

	/** Nobody connected: the four f fragments taken by corner — exactly the "plain whole-cell look" */
	public static final Layout DISCONNECTED = disconnected();

	private static Layout disconnected() {

		int[][] corners = new int[6][4];

		for(int side = 0; side < 6; side++) {
			corners[side][0] = CT.ftl;
			corners[side][1] = CT.ftr;
			corners[side][2] = CT.fbl;
			corners[side][3] = CT.fbr;
		}

		return new Layout(corners);
	}

	/*
	 * The same cell gets asked seven times (six faces plus one general pass), and the chunk advances one cell at a time, so only the most recent cell is kept.
	 * The cache is not part of correctness: when threads collide, the worst case is one recompute, never reading someone else's layout.
	 */
	private static IBlockAccess lastWorld;
	private static BlockPos lastPos;
	private static Layout lastLayout;

	public static Layout layout(IBlockAccess world, BlockPos pos) {

		if(world == lastWorld && pos.equals(lastPos))
			return lastLayout;

		Layout layout = compute(world, pos);

		lastWorld = world;
		lastPos = pos;
		lastLayout = layout;

		return layout;
	}

	private static Layout compute(IBlockAccess world, BlockPos pos) {

		IBlockState selfState = world.getBlockState(pos);
		Block self = selfState.getBlock();

		if(!(self instanceof IBlockCT))
			return DISCONNECTED;

		IBlockCT ct = (IBlockCT) self;
		int[][] corners = new int[6][4];

		for(int side = 0; side < 6; side++) {

			boolean[] cons = new boolean[8];

			for(int j = 0; j < 8; j++) {

				int[] offset = ACCESS[side][j];
				BlockPos neighborPos = pos.add(offset[0], offset[1], offset[2]);

				if(ct.canConnect(world, selfState, pos, neighborPos)) {
					cons[j] = true;
				}
			}

			/*
			 * 0 1 2
			 * 3   4
			 * 5 6 7
			 */

			corners[side][0] = CT.t | CT.l | cornerType(cons[3], cons[0], cons[1]);
			corners[side][1] = CT.t | CT.r | cornerType(cons[4], cons[2], cons[1]);
			corners[side][2] = CT.b | CT.l | cornerType(cons[3], cons[5], cons[6]);
			corners[side][3] = CT.b | CT.r | cornerType(cons[4], cons[7], cons[6]);
		}

		return new Layout(corners);
	}

	/**
	 * Three booleans — side neighbor, corner neighbor, vertical neighbor — decide which class this corner belongs to.
	 *
	 * C - V
	 * |
	 * H
	 */
	public static int cornerType(boolean hor, boolean corner, boolean vert) {

		if(vert && hor && corner)
			return CT.c;
		else if(vert && hor)
			return CT.j;
		else if(vert)
			return CT.v;
		else if(hor)
			return CT.h;
		else
			return CT.f;
	}
}
