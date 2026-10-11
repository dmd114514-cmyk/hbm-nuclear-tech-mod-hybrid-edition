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

/** The short standard rail: one cell and the trip is done, so the core doesn't push outward */
public class RailStandardStraightShort extends BlockDummyable implements IRailNTM {

	private static final double RAIL_TOP = 0.1875D;

	public RailStandardStraightShort() {
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
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 1, 0};
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

		double vx = trainX, vy = y + RAIL_TOP, vz = trainZ;

		if(dir == Library.POS_X || dir == Library.NEG_X) {
			double targetX = trainX;
			if(motionX > 0) {
				targetX += speed;
				info.yaw(-90F);
			} else {
				targetX -= speed;
				info.yaw(90F);
			}
			vx = MathHelper.clamp(targetX, cX, cX + 1);
			vz = cZ + 0.5 + rot.getFrontOffsetZ() * 0.5;
			info.dist(Math.abs(targetX - vx) * Math.signum(speed));
			info.pos(new BlockPos(cX + (motionX * speed > 0 ? 1 : -1), y, cZ));
		} else {
			double targetZ = trainZ;
			if(motionZ > 0) {
				targetZ += speed;
				info.yaw(0F);
			} else {
				targetZ -= speed;
				info.yaw(180F);
			}
			vx = cX + 0.5 + rot.getFrontOffsetX() * 0.5;
			vz = MathHelper.clamp(targetZ, cZ, cZ + 1);
			info.dist(Math.abs(targetZ - vz) * Math.signum(speed));
			info.pos(new BlockPos(cX, y, cZ + (motionZ * speed > 0 ? 1 : -1)));
		}

		return new Vec3d(vx, vy, vz);
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.STANDARD;
	}
}
