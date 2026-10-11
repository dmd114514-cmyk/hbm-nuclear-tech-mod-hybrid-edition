package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.Library;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The standard-gauge curve's shared algorithm: the whole arc rotates about a virtual center; width decides radius.
 * Subclasses change exactly two things — the radius ({@link #width}) and the placeholder layout.
 */
public class RailStandardCurveBase extends BlockDummyable implements IRailNTM {

	protected int width = 4;

	/** The cells this base tier lays; the third column is the laid cell's own facing afterward (d=incoming r=side) */
	private static final int[][] ARC = {
		{1, 0, 0}, {0, 1, 1}, {1, 1, 1}, {1, 2, 1}, {2, 1, 0}, {2, 2, 0}, {3, 1, 0},
		{3, 2, 0}, {2, 3, 1}, {3, 3, 1}, {4, 3, 0}, {3, 4, 1}, {4, 4, 1},
	};

	private static final double RAIL_TOP = 0.1875D;

	public RailStandardCurveBase() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public Vec3d getSnappingPos(World world, int x, int y, int z, double trainX, double trainY, double trainZ) {
		return snapAndMove(world, x, y, z, trainX, trainY, trainZ, 0, 0, 0, 0, new RailContext());
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info, MoveContext context) {
		return snapAndMove(world, x, y, z, trainX, trainY, trainZ, motionX, motionY, motionZ, speed, info);
	}

	public Vec3d snapAndMove(World world, int x, int y, int z, double trainX, double trainY, double trainZ, double motionX, double motionY, double motionZ, double speed, RailContext info) {

		BlockPos core = this.findCore(world, new BlockPos(x, y, z));
		if(core == null) return new Vec3d(trainX, trainY, trainZ);

		int cX = core.getX();
		int cZ = core.getZ();
		EnumFacing dir = facingForMeta(metaAt(world, core) - offset);
		if(dir == null) return new Vec3d(trainX, trainY, trainZ);
		EnumFacing rot = dir.rotateY();

		double turnRadius = width;
		double axisDist = width + 0.5D;

		double axisX = cX + 0.5 + dir.getFrontOffsetX() * 0.5 + rot.getFrontOffsetX() * axisDist;
		double axisZ = cZ + 0.5 + dir.getFrontOffsetZ() * 0.5 + rot.getFrontOffsetZ() * axisDist;

		double dx = trainX - axisX;
		double dz = trainZ - axisZ;
		double len = Math.sqrt(dx * dx + dz * dz);

		if(len > 0) {
			dx = dx / len * turnRadius;
			dz = dz / len * turnRadius;
		}

		double moveAngle = Math.atan2(motionX, motionZ) * 180D / Math.PI + 90;

		if(speed == 0) {
			info.dist(0).pos(new BlockPos(x, y, z)).yaw((float) moveAngle);
			return new Vec3d(axisX + dx, y, axisZ + dz);
		}

		double angleDeg = Math.atan2(dx, dz) * 180D / Math.PI + 90;
		if(dir == Library.NEG_X) angleDeg -= 90;
		if(dir == Library.POS_X) angleDeg += 90;
		if(dir == Library.POS_Z) angleDeg += 180;
		angleDeg = MathHelper.wrapDegrees(angleDeg);
		double length90Deg = turnRadius * Math.PI / 2D;
		double angularChange = speed / length90Deg * 90D;

		EnumFacing moveDir;

		if(Math.abs(motionX) > Math.abs(motionZ)) {
			moveDir = motionX > 0 ? Library.POS_X : Library.NEG_X;
		} else {
			moveDir = motionZ > 0 ? Library.POS_Z : Library.NEG_Z;
		}

		if(moveDir == dir || moveDir == rot.getOpposite()) {
			angularChange *= -1;
		}

		double effAngle = angleDeg + angularChange;
		moveAngle += angularChange;

		if(effAngle > 90) {
			double angleOvershoot = effAngle - 90D;
			moveAngle -= angleOvershoot;
			double lengthOvershoot = angleOvershoot * length90Deg / 90D;
			info.dist(lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX - dir.getFrontOffsetX() * width + rot.getFrontOffsetX() * (width + 1), y, cZ - dir.getFrontOffsetZ() * width + rot.getFrontOffsetZ() * (width + 1))).yaw((float) moveAngle);
			return new Vec3d(axisX - dir.getFrontOffsetX() * turnRadius, y + RAIL_TOP, axisZ - dir.getFrontOffsetZ() * turnRadius);
		}

		if(effAngle < 0) {
			double angleOvershoot = -effAngle;
			moveAngle -= angleOvershoot;
			double lengthOvershoot = angleOvershoot * length90Deg / 90D;
			info.dist(-lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX + dir.getFrontOffsetX(), y, cZ + dir.getFrontOffsetZ())).yaw((float) moveAngle);
			return new Vec3d(axisX - rot.getFrontOffsetX() * turnRadius, y + RAIL_TOP, axisZ - rot.getFrontOffsetZ() * turnRadius);
		}

		double radianChange = angularChange * Math.PI / 180D;

		// The angle of this segment rotated about Y: via this version's rotateYaw (matching the tree-wide Vec3→Vec3d convention)
		Vec3d turned = new Vec3d(dx, 0, dz).rotateYaw((float)radianChange);

		return new Vec3d(axisX + turned.x, y + RAIL_TOP, axisZ + turned.z);
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.STANDARD;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, width, 0, width, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, EnumFacing dir, int o) {
		return arcFits(world, x, y, z, dir, ARC);
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, EnumFacing dir, int o) {
		placeArc(world, x, y, z, dir, ARC);
	}

	/** Shared by the curve family: incoming reversed plus sideways form the basis vectors; cells are placed by (incoming, side) step counts */
	protected static int[] arcBasis(EnumFacing dir, int[] out) {
		EnumFacing rot = dir.rotateY();
		EnumFacing back = dir.getOpposite();
		out[0] = back.getFrontOffsetX();
		out[1] = back.getFrontOffsetZ();
		out[2] = rot.getFrontOffsetX();
		out[3] = rot.getFrontOffsetZ();
		return out;
	}

	protected boolean arcFits(World world, int x, int y, int z, EnumFacing dir, int[][] arc) {
		int[] b = arcBasis(dir, new int[4]);

		for(int[] cell : arc) {
			int cx = x + b[0] * cell[0] + b[2] * cell[1];
			int cz = z + b[1] * cell[0] + b[3] * cell[1];
			BlockPos pos = new BlockPos(cx, y, cz);
			if(!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) return false;
		}

		return true;
	}

	protected void placeArc(World world, int x, int y, int z, EnumFacing dir, int[][] arc) {
		// While this batch lays, placeholders must not tear each other down; the one gate lives in MultiblockHandlerXR
		MultiblockHandlerXR.safeRem = true;

		int[] b = arcBasis(dir, new int[4]);
		int d = dir.getOpposite().ordinal();
		int r = dir.rotateY().ordinal();

		for(int[] cell : arc) {
			int cx = x + b[0] * cell[0] + b[2] * cell[1];
			int cz = z + b[1] * cell[0] + b[3] * cell[1];
			int meta = cell[2] == 0 ? d : r;
			world.setBlockState(new BlockPos(cx, y, cz), getStateFromMeta(meta), 3);
		}

		MultiblockHandlerXR.safeRem = false;
	}
}
