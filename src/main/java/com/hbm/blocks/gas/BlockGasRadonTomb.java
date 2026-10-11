package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.blocks.generic.ModBlocksGeneric;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The tomb's breath: clears radaway and rad-x effects, pours 0.5 of radiation in, and nibbles grass and leaves.
 *
 * The special branch as before — contact first wipes the radaway/radx effects (the old author's words below),
 * then computes internal irradiation straight through RAD_BYPASS.
 *
 * The old "grass becomes waste_earth" half leaned on the same-family block; restored once it registered: the grass below turns to dirt 1/5 of the time, and friends
 */
public class BlockGasRadonTomb extends BlockGasBase {

	/*
	 * You should not have come here.
	 *
	 * This is not a place of honor. No great deed is commemorated here.
	 *
	 * Nothing of value is here.
	 *
	 * What is here is dangerous and repulsive.
	 *
	 * We considered ourselves a powerful culture. We harnessed the hidden fire,
	 * and used it for our own purposes.
	 *
	 * Then we saw the fire could burn within living things, unnoticed until it
	 * destroyed them.
	 *
	 * And we were afraid.
	 *
	 * We built great tombs to hold the fire for one hundred thousand years,
	 * after which it would no longer kill.
	 *
	 * If this place is opened, the fire will not be isolated from the world,
	 * and we will have failed to protect you.
	 *
	 * Leave this place and never come back.
	 */
	public BlockGasRadonTomb() {
		super(0.1F, 0.3F, 0.1F);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) entity;

			living.removePotionEffect(HbmPotion.radaway); //get fucked
			living.removePotionEffect(HbmPotion.radx);

			ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 0.5F);
			HbmLivingProps.incrementAsbestos(living, 10);
		}
	}

	@Override
	public EnumFacing getFirstDirection(World world, BlockPos pos) {

		if(world.rand.nextInt(3) == 0)
			return EnumFacing.UP;

		return EnumFacing.DOWN;
	}

	@Override
	public EnumFacing getSecondDirection(World world, BlockPos pos) {
		return this.randomHorizontal(world);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote) {

			if(rand.nextInt(10) == 0) {
				BlockPos below = pos.down();
				Block b = world.getBlockState(below).getBlock();

				if(b == Blocks.GRASS) {
					if(rand.nextInt(5) == 0)
						world.setBlockState(below, Blocks.DIRT.getDefaultState(), 3);
					else
						world.setBlockState(below, ModBlocksGeneric.waste_earth.getDefaultState());
				}

				Material mat = world.getBlockState(below).getMaterial();
				if((mat == Material.GRASS || mat == Material.LEAVES || mat == Material.PLANTS || mat == Material.VINE)
						&& !b.isNormalCube(world.getBlockState(below))) {
					world.setBlockToAir(below);
				}
			}

			if(rand.nextInt(600) == 0) {
				world.setBlockToAir(pos);
				return;
			}
		}

		super.updateTick(world, pos, state, rand);
	}
}
