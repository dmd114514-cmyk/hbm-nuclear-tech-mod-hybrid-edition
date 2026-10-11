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

/** The uphill ramp: one placeholder layer more than a plain straight, so both the placement check and the laying run twice */
public class RailStandardRamp extends BlockDummyable implements IRailNTM {

	private static final double RAIL_TOP = 0.1875D;

	/** The ring a ramp wants beyond flat ground: one cell taller longitudinally, nothing trimmed below */
	private static final int[] RAMP_EXTRA = new int[] {1, -1, 2, 2, 1, 0};

	public RailStandardRamp() {
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
		return new int[] {0, 0, 2, 2, 1, 0};
	}

	@Override
	public int getOffset() {
		return 2;
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
		int cY = core.getY();
		int cZ = core.getZ();
		EnumFacing dir = facingForMeta(metaAt(world, core) - offset);
		if(dir == null) return new Vec3d(trainX, trainY, trainZ);
		EnumFacing rot = dir.rotateY();

		double vx = trainX, vy = trainY, vz = trainZ;

		if(dir == Library.POS_X || dir == Library.NEG_X) {
			double targetX = trainX;
			if(motionX > 0) {
				targetX += speed;
				info.yaw(-90F);
			} else {
				targetX -= speed;
				info.yaw(90F);
			}
			double dist = (cX + 0.5 - targetX + 2.5) / 5;
			vx = MathHelper.clamp(targetX, cX - 2, cX + 3);
			vy = MathHelper.clamp(dir == Library.POS_X ? cY + dist : cY + 1 - dist, cY, cY + 1) + RAIL_TOP;
			vz = cZ + 0.5 + rot.getFrontOffsetZ() * 0.5;
			info.dist(Math.abs(targetX - vx) * Math.signum(speed));
			info.pos(new BlockPos(cX + (motionX * speed > 0 ? 3 : -3), cY + (motionX * speed > 0 ^ dir == Library.POS_X ? 1 : 0), cZ));
		} else {
			double targetZ = trainZ;
			if(motionZ > 0) {
				targetZ += speed;
				info.yaw(0F);
			} else {
				targetZ -= speed;
				info.yaw(180F);
			}
			double dist = (cZ + 0.5 - targetZ + 2.5) / 5;
			vx = cX + 0.5 + rot.getFrontOffsetX() * 0.5;
			vy = MathHelper.clamp(dir == Library.POS_Z ? cY + dist : cY + 1 - dist, cY, cY + 1) + RAIL_TOP;
			vz = MathHelper.clamp(targetZ, cZ - 2, cZ + 3);
			info.dist(Math.abs(targetZ - vz) * Math.signum(speed));
			info.pos(new BlockPos(cX, cY + (motionZ * speed > 0 ^ dir == Library.POS_Z ? 1 : 0), cZ + (motionZ * speed > 0 ? 3 : -3)));
		}

		return new Vec3d(vx, vy, vz);
	}

	@Override
	public TrackGauge getGauge(World world, int x, int y, int z) {
		return TrackGauge.STANDARD;
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, EnumFacing dir, int o) {
		return MultiblockHandlerXR.checkSpace(world, x + dir.getFrontOffsetX() * o, y + dir.getFrontOffsetY() * o, z + dir.getFrontOffsetZ() * o, getDimensions(), x, y, z, dir)
				&& MultiblockHandlerXR.checkSpace(world, x + dir.getFrontOffsetX() * o, y + dir.getFrontOffsetY() * o, z + dir.getFrontOffsetZ() * o, RAMP_EXTRA, x, y, z, dir);
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, EnumFacing dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.getFrontOffsetX() * o, y + dir.getFrontOffsetY() * o, z + dir.getFrontOffsetZ() * o, getDimensions(), this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.getFrontOffsetX() * o, y + dir.getFrontOffsetY() * o, z + dir.getFrontOffsetZ() * o, RAMP_EXTRA, this, dir);
	}
}
