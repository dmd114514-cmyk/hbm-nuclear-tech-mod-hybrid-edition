package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.tileentity.machine.TileEntityFoundryChannel;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.material.MaterialStack;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The casting channel: a half-block-high duct carrying material from tank or crucible toward the outlet in stretches.
 *
 * The shape is "a central block core + arms reaching toward connectable faces", so collision and selection boxes both compute per facing.
 */
public class FoundryChannel extends BlockContainerBase implements ICrucibleAcceptor {

	/** The central block core, in local coordinates */
	private static final double CORE_MIN = 0.3125;
	private static final double CORE_MAX = 0.6875;
	/** Half a block tall */
	private static final double TOP = 0.5;

	public FoundryChannel() {
		super(Material.ROCK);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryChannel();
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack<?> pour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).pour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialFlow(world, x, y, z, side, stack);
	}

	@Override
	public MaterialStack<?> flow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).flow(world, x, y, z, side, stack);
	}

	/**
	 * Whether this face grows an arm. Only horizontal directions can connect; the outlet wants its front face aligned,
	 * since the outlet only takes material from its back — connect the wrong face and it's decoration.
	 */
	public boolean canConnectTo(IBlockAccess world, BlockPos pos, EnumFacing dir) {

		if(dir.getAxis() == EnumFacing.Axis.Y)
			return false;

		BlockPos next = pos.offset(dir);
		Block b = world.getBlockState(next).getBlock();

		if(b instanceof FoundryOutlet) {
			return world.getBlockState(next).getValue(FoundryOutlet.FACING) == dir;
		}

		// The slag outlet rides the same rule, arriving with batch 3 alongside the slag block; molds likewise
		return b == ModBlocksMachine.foundry_channel;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean isActualState) {

		List<AxisAlignedBB> boxes = new ArrayList<AxisAlignedBB>();

		boxes.add(box(pos, CORE_MIN, 0, CORE_MIN, CORE_MAX, TOP, CORE_MAX));

		if(canConnectTo(world, pos, EnumFacing.EAST)) boxes.add(box(pos, CORE_MAX, 0, CORE_MIN, 1, TOP, CORE_MAX));
		if(canConnectTo(world, pos, EnumFacing.WEST)) boxes.add(box(pos, 0, 0, CORE_MIN, CORE_MIN, TOP, CORE_MAX));
		if(canConnectTo(world, pos, EnumFacing.SOUTH)) boxes.add(box(pos, CORE_MIN, 0, CORE_MAX, CORE_MAX, TOP, 1));
		if(canConnectTo(world, pos, EnumFacing.NORTH)) boxes.add(box(pos, CORE_MIN, 0, 0, CORE_MAX, TOP, CORE_MIN));

		for(AxisAlignedBB bb : boxes) {
			if(entityBox.intersects(bb)) {
				collidingBoxes.add(bb);
			}
		}
	}

	/** The local coordinate set translated to the block's cell */
	private static AxisAlignedBB box(BlockPos pos, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		return new AxisAlignedBB(pos.getX() + minX, pos.getY() + minY, pos.getZ() + minZ, pos.getX() + maxX, pos.getY() + maxY, pos.getZ() + maxZ);
	}

	/** The selection box grows with the arms it touches; returns local coordinates */
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return new AxisAlignedBB(
				canConnectTo(world, pos, EnumFacing.WEST) ? 0 : CORE_MIN,
				0,
				canConnectTo(world, pos, EnumFacing.NORTH) ? 0 : CORE_MIN,
				canConnectTo(world, pos, EnumFacing.EAST) ? 1 : CORE_MAX,
				TOP,
				canConnectTo(world, pos, EnumFacing.SOUTH) ? 1 : CORE_MAX);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			return true;
		}

		if(hand == EnumHand.MAIN_HAND && FoundryTank.isShovel(player.getHeldItemMainhand())) {
			FoundryTank.clearIfFoundry(world, pos, player);
			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		FoundryTank.clearIfFoundry(world, pos, null);

		super.breakBlock(world, pos, state);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
}
