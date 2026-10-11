package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The Watz pump: one core cell plus a placeholder overhead whose top face holds weight */
public class WatzPump extends BlockDummyable {

	public WatzPump() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= CORE_MIN) return new TileEntityWatzPump();

		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.UP && metaAt(world, pos) == EnumFacing.UP.ordinal();
	}

	/** No canUpdate in this version: not implementing ITickable means not ticking, equivalent to the old return false */
	public static class TileEntityWatzPump extends TileEntity {

		@Override
		@SideOnly(Side.CLIENT)
		public double getMaxRenderDistanceSquared() {
			return 65536.0D;
		}

		private AxisAlignedBB bb = null;

		@Override
		public AxisAlignedBB getRenderBoundingBox() {
			if(bb == null) {
				BlockPos pos = getPos();
				bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
			}
			return bb;
		}
	}
}
