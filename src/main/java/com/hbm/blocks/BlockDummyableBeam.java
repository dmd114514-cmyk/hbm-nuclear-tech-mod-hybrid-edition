package com.hbm.blocks;

import java.util.Random;

import com.hbm.handler.MultiblockHandlerXR;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The beam segment: no tile entity of its own; every interaction forwards to the real machine it points at.
 *
 * The old findCore here declared World — an overload of the body's IBlockAccess version, not an override,
 * so base-internal calls actually hit the base's copy; this makes it a same-named override, one less coin flip.
 */
public class BlockDummyableBeam extends BlockDummyable implements ILookOverlay {

	public BlockDummyableBeam(Material mat) {
		super(mat);
		setLightLevel(1.0F);
		setLightOpacity(0);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	/** The beam only checks "is the previous cell the machine body", then hands off to the body to find the real core */
	private BlockPos beamAnchor(IBlockAccess world, BlockPos pos) {

		int metadata = metaAt(world, pos);

		if(hasExtra(metadata))
			metadata -= extra;

		EnumFacing dir = facingForMeta(metadata);

		if(dir == null)
			return null;

		BlockPos anchor = pos.offset(dir.getOpposite());
		Block b = world.getBlockState(anchor).getBlock();

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam))
			return anchor;

		return null;
	}

	@Override
	public BlockPos findCore(IBlockAccess world, BlockPos pos) {

		BlockPos anchor = beamAnchor(world, pos);

		if(anchor == null)
			return null;

		Block b = world.getBlockState(anchor).getBlock();

		return ((BlockDummyable) b).findCore(world, anchor);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		BlockPos anchor = beamAnchor(world, pos);

		if(anchor == null)
			return;

		Block b = world.getBlockState(anchor).getBlock();

		((BlockDummyable) b).breakBlock(world, anchor, world.getBlockState(anchor));
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block cause, BlockPos causePos) {

		if(world.isRemote || MultiblockHandlerXR.safeRem) return;

		dropIfBeamless(world, pos);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		super.updateTick(world, pos, state, rand);

		if(world.isRemote) return;

		dropIfBeamless(world, pos);
	}

	private void dropIfBeamless(World world, BlockPos pos) {

		int metadata = metaAt(world, pos);

		if(hasExtra(metadata))
			metadata -= extra;

		EnumFacing dir = facingForMeta(metadata);

		if(dir == null)
			return;

		Block b = world.getBlockState(pos.offset(dir.getOpposite())).getBlock();

		if(!(b instanceof BlockDummyable)) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		BlockPos anchor = beamAnchor(world, pos);

		if(anchor == null)
			return false;

		Block b = world.getBlockState(anchor).getBlock();

		if(b instanceof BlockDummyableBeam)
			return ((BlockDummyableBeam) b).onBlockActivated(world, anchor, world.getBlockState(anchor), player, hand, side, hitX, hitY, hitZ);

		return ((BlockDummyable) b).onBlockActivated(world, anchor, world.getBlockState(anchor), player, hand, side, hitX, hitY, hitZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, BlockPos pos) {

		int metadata = metaAt(world, pos);

		if(hasExtra(metadata))
			metadata -= extra;

		EnumFacing dir = facingForMeta(metadata);

		if(dir == null)
			return;

		BlockPos anchor = pos.offset(dir.getOpposite());
		Block b = world.getBlockState(anchor).getBlock();

		if(b instanceof BlockDummyable && !(b instanceof BlockDummyableBeam) && b instanceof ILookOverlay)
			((ILookOverlay) b).printHook(event, world, anchor);
	}
}
