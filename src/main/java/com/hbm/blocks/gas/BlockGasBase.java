package com.hbm.blocks.gas;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.BlockLoot;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.ArmorUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The gas block base: a family of unseen, replaceable, self-drifting blocks (the radon/CO/asbestos-dust tier).
 *
 * Visible surface matches the old version — **it was never visible** (the old `getRenderType()` returned -1 outright); only players wearing the grey glass
 * see tinted clouds drifting from the block's center on the client (randomDisplayTick → effectNT's vanillaExt/cloud).
 * So the block state points at the empty-elements `hbm:gas_invisible`, while the item icon keeps the solid-texture model.
 *
 * Three rewrites for 1.12.2:
 *   1) coordinates and scheduled updates moved to BlockPos / IBlockState;
 *   2) the old version used block metadata as an "already scheduled" flag (onBlockAdded only scheduled at meta==0, clearing meta on neighbor changes to reschedule),
 *      absent here; it schedules directly instead — re-scheduling the same cell keeps one entry (the scheduler dedups by position), semantically equivalent;
 *   3) the old updateTick's `world.scheduledUpdatesAreImmediate = false` was the anti-recursion trick of its day;
 *      the field is protected in 1.12.2 and unreachable outside the package, and this version's scheduler goes by delay, so it's dropped.
 *
 * Drops: gas blocks drop nothing when mined. Per §15, an empty table (`{"pools":[]}`) ships with this batch and harvestBlock wires into
 * BlockLoot — unwired, vanilla's fallback would drop the block itself.
 */
public abstract class BlockGasBase extends BlockBase {

	float red;
	float green;
	float blue;

	public BlockGasBase(float r, float g, float b) {
		super(ModBlocksGas.materialGas);
		this.setHardness(0.0F);
		this.setResistance(0.0F);
		this.setLightOpacity(0);
		this.red = r;
		this.green = g;
		this.blue = b;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid) {
		return false;
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		BlockLoot.harvest(world, pos, state, player, stack);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);

		if(!world.isRemote)
			world.scheduleUpdate(pos, this, 10);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, net.minecraft.block.Block block, BlockPos fromPos) {

		if(!world.isRemote)
			world.scheduleUpdate(pos, this, 10);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote) {

			if(!tryMove(world, pos, getFirstDirection(world, pos)))
				if(!tryMove(world, pos, getSecondDirection(world, pos)))
					world.scheduleUpdate(pos, this, getDelay(world));
		}
	}

	public abstract EnumFacing getFirstDirection(World world, BlockPos pos);

	public EnumFacing getSecondDirection(World world, BlockPos pos) {
		return getFirstDirection(world, pos);
	}

	public boolean tryMove(World world, BlockPos pos, EnumFacing dir) {

		BlockPos next = pos.offset(dir);

		if(world.isAirBlock(next)) {
			world.setBlockToAir(pos);
			world.setBlockState(next, this.getDefaultState());
			world.scheduleUpdate(next, this, getDelay(world));
			return true;
		}

		return false;
	}

	public int getDelay(World world) {
		return 2;
	}

	public EnumFacing randomHorizontal(World world) {
		return EnumFacing.HORIZONTALS[world.rand.nextInt(4)];
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);

		EntityPlayer p = MainRegistry.proxy.me();
		//the grey-glass tier: the old call passed slot 3 (old numbering head0/chest1/legs2/feet3); copied unchanged
		if(ArmorUtil.checkArmorPiece(p, ModItems.ashglasses, 3)) {
			if(rand.nextInt(9) == 1) {
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "vanillaExt");
				data.setString("mode", "cloud");
				data.setDouble("posX", pos.getX() + 0.5);
				data.setDouble("posY", pos.getY() + 0.5);
				data.setDouble("posZ", pos.getZ() + 0.5);
				data.setFloat("r", red);
				data.setFloat("g", green);
				data.setFloat("b", blue);
				MainRegistry.proxy.effectNT(data);
			}
		}
	}
}
