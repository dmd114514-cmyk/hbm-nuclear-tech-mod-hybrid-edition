package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockNaming;

import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The steel-capable trapdoor (the mod's single steel piece).
 *
 * Three hand-edits on that old brick, mapped one by one:
 *   1) vanilla's iron trapdoor only answers redstone, ignoring clicks; here a click toggles it too (the old `meta ^ 4`);
 *   2) an open door standing against the wall becomes climbable when a ladder sits below — vanilla never had that;
 *   3) the old version also hand-wrote a set of 0.125-thick collision boxes making the open panel ladder-thin when climbable.
 *
 * Item 3 needs no override here: {@code Block.getCollisionBoundingBox} returns {@code state.getBoundingBox(world, pos)} directly in this version
 * (javap-verified), and {@code BlockTrapDoor.getBoundingBox} hands out exactly that {@code *_OPEN_AABB} set when open —
 * those four hand-written boxes match it dimension for dimension; writing them again would only break the facing handling downstream.
 *
 * The old item 2 tested "open" via {@code func_150118_d(meta)}, the author's own note reading {@code // isOpen},
 * which matches that wall-hung box set — so this version wires the {@code OPEN} property.
 */
public class BlockNTMTrapdoor extends BlockTrapDoor implements IBlockNaming {

	public BlockNTMTrapdoor(Material mat) {
		super(mat);
	}

	/** Textures belong to JSON models; the old chained writing is caught here; the return type must be this class, or the chain breaks */
	public BlockNTMTrapdoor setBlockTextureName(String name) {
		return this;
	}

	/** Same door as {@code BlockBase}'s: step sounds became the protected {@code setSoundType(SoundType)} here */
	public BlockNTMTrapdoor setStepSound(SoundType type) {
		setSoundType(type);
		return this;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		IBlockState toggled = state.cycleProperty(OPEN);

		world.setBlockState(pos, toggled, 2);
		this.playSound(player, world, pos, toggled.getValue(OPEN).booleanValue());

		return true;
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {

		if(!state.getValue(OPEN).booleanValue())
			return false;

		IBlockState below = world.getBlockState(pos.down());

		return below.getBlock().isLadder(below, world, pos.down(), entity);
	}
}
