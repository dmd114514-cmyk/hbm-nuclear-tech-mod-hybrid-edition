package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * The radiating, falling blocks (yellowcake / red phosphorus / fallout, three pieces).
 * The falling half rides {@link BlockFallingMod} (vanilla BlockFalling) as before; this layers only the radiation half:
 * one hazard-table read on world entry, an add every 20 ticks with rescheduling; beacon base as before, default material sand.
 */
public class BlockHazardFalling extends BlockFallingMod {

	private float rad = 0.0F;
	private boolean beaconable = false;

	public BlockHazardFalling() {
		this(Material.SAND);
	}

	public BlockHazardFalling(Material mat) {
		super(mat);
	}

	public BlockHazardFalling setBeaconable() {
		this.beaconable = true;
		return this;
	}

	@Override
	public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beaconPos) {
		return beaconable;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);

		rad = HazardSystem.getHazardLevelFromStack(new ItemStack(Item.getItemFromBlock(this)), HazardRegistry.RADIATION) * 0.1F;

		if(this.rad > 0)
			world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(this.rad > 0) {
			ChunkRadiationManager.proxy.incrementRad(world, pos.getX(), pos.getY(), pos.getZ(), rad);
			world.scheduleUpdate(pos, this, this.tickRate(world));
		}

		super.updateTick(world, pos, state, rand);
	}

	@Override
	public int tickRate(World world) {

		if(this.rad > 0)
			return 20;

		return super.tickRate(world);
	}
}
