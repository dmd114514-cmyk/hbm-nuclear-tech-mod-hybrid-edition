package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Pillar blocks with axes (the pillar-and-log shape tier).
 *
 * Shape and axis are off-the-shelf here: vanilla's `BlockRotatedPillar` ships the `AXIS` state, and top versus side textures
 * are no longer a code-side icon registration, all belonging to `models/block/*.json`. So this thin layer keeps four doors
 * completing the "old had it, hook changed" behaviors:
 *   1) naming via `IBlockNaming` (single inheritance is taken by vanilla here); step sounds in 1.12.2 are the protected
 *      `setSoundType`, reachable only from subclasses.
 *   2) the default state pins back to vertical: this version's `getBaseState()` takes the property's first value, and `EnumFacing.Axis` orders
 *      X before Y ⇒ unpinned, `setblock` always comes out sideways (a live `testforblock` returning data 4 proves it,
 *      while the old meta 0 was vertical).
 *   3) placement rotation: the old version leaned on `BlockRotatedPillar.onBlockPlaced` (up/down → Y, north/south → Z, east/west → X);
 *      what `ItemBlock` really calls here is the `EnumHand`-carrying `getStateForPlacement`; vanilla `BlockLog` overrides
 *      the hand-less version, which nothing in this mapping calls — so rotation wires up locally.
 */
public class BlockRotatablePillar extends BlockRotatedPillar implements IBlockNaming {

	public BlockRotatablePillar(Material material) {
		super(material);
		setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return this.getDefaultState().withProperty(AXIS, facing.getAxis());
	}

	/** Textures belong to models; this only absorbs the old chained writing */
	public BlockRotatablePillar setBlockTextureName(String name) {
		return this;
	}

	public BlockRotatablePillar setStepSound(SoundType type) {
		setSoundType(type);
		return this;
	}
}
