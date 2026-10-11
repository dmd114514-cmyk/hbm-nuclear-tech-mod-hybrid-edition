package com.hbm.blocks.machine;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.tileentity.machine.TileEntityFoundryBase;
import com.hbm.tileentity.machine.TileEntityFoundryTank;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.material.MaterialStack;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The casting basin: a tall tank holding a whole charge — the chain's reservoir.
 *
 * Fills only from the top; anything flowing in from the sides is refused; a shovel clears the charge into scrap, and breaking spills it too.
 */
public class FoundryTank extends BlockContainerBase implements ICrucibleAcceptor {

	public FoundryTank() {
		super(Material.ROCK);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryTank();
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack<?> pour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).pour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override public boolean canAcceptPartialFlow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) { return false; }
	@Override public MaterialStack<?> flow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) { return stack; }

	/** The top face is closed; every other face accepts contact — metal pours in from the top */
	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side != EnumFacing.UP;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			return true;
		}

		if(hand == EnumHand.MAIN_HAND && isShovel(player.getHeldItemMainhand())) {
			clearIfFoundry(world, pos, player);
			return true;
		}

		return false;
	}

	/** Recognizes shovel-class tools; like the old version, only the tool's class name is asked */
	public static boolean isShovel(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() instanceof ItemTool
				&& ((ItemTool) stack.getItem()).getToolClasses(stack).contains("shovel");
	}

	/** The step shared by shovel and block break: the cell's charge turns to scrap and spills out */
	public static void clearIfFoundry(World world, BlockPos pos, EntityPlayer player) {

		TileEntity te = world.getTileEntity(pos);

		if(te instanceof TileEntityFoundryBase) {
			((TileEntityFoundryBase) te).clearToScrap(world, pos, player);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		clearIfFoundry(world, pos, null);

		super.breakBlock(world, pos, state);
	}
}
