package com.hbm.blocks.rail;

import com.hbm.blocks.BlockDummyable;
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

/** The narrow-gauge curve: radius fixed at 4.5, laying via BlockDummyable's generic dimensions rather than its own table */
public class RailNarrowCurve extends BlockDummyable implements IRailNTM {

	private static final double TURN_RADIUS = 4.5D;

	public RailNarrowCurve() {
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

		double axisX = cX + 0.5 + dir.getFrontOffsetX() * 0.5 + rot.getFrontOffsetX() * TURN_RADIUS;
		double axisZ = cZ + 0.5 + dir.getFrontOffsetZ() * 0.5 + rot.getFrontOffsetZ() * TURN_RADIUS;

		double dx = trainX - axisX;
		double dz = trainZ - axisZ;
		double len = Math.sqrt(dx * dx + dz * dz);

		if(len > 0) {
			dx = dx / len * TURN_RADIUS;
			dz = dz / len * TURN_RADIUS;
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
		double length90Deg = TURN_RADIUS * Math.PI / 2D;
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
			info.dist(lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX - dir.getFrontOffsetX() * 4 + rot.getFrontOffsetX() * 5, y, cZ - dir.getFrontOffsetZ() * 4 + rot.getFrontOffsetZ() * 5)).yaw((float) moveAngle);
			return new Vec3d(axisX - dir.getFrontOffsetX() * TURN_RADIUS, y, axisZ - dir.getFrontOffsetZ() * TURN_RADIUS);
		}

		if(effAngle < 0) {
			double angleOvershoot = -effAngle;
			moveAngle -= angleOvershoot;
			double lengthOvershoot = angleOvershoot * length90Deg / 90D;
			info.dist(-lengthOvershoot * Math.signum(speed * angularChange)).pos(new BlockPos(cX + dir.getFrontOffsetX(), y, cZ + dir.getFrontOffsetZ())).yaw((float) moveAngle);
			return new Vec3d(axisX - rot.getFrontOffsetX() * TURN_RADIUS, y, axisZ - rot.getFrontOffsetZ() * TURN_RADIUS);
		}

		double radianChange = angularChange * Math.PI / 180D;
		Vec3d turned = new Vec3d(dx, 0, dz).rotateYaw((float)radianChange);

		return new Vec3d(axisX + turned.x, y, axisZ + turned.z);
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.NARROW;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 4, 0, 4, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
	}
}
