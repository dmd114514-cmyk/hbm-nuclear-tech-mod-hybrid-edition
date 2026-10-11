package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Radon: one breath is the internal-irradiation route; the full hazmat set (fine-particle tier) only stalls it by burning filters. */
public class BlockGasRadon extends BlockGasBase {

	public BlockGasRadon() {
		super(0.1F, 0.8F, 0.1F);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) entity;

		if(ArmorRegistry.hasAllProtection(living, 3, HazardClass.PARTICLE_FINE)) {
			ArmorUtil.damageGasMaskFilter(living, 1);
		} else {
			ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 0.05F);
			HbmLivingProps.incrementAsbestos(living, 1);
		}
	}

	@Override
	public EnumFacing getFirstDirection(World world, BlockPos pos) {

		if(world.rand.nextInt(5) == 0)
			return EnumFacing.UP;

		return EnumFacing.DOWN;
	}

	@Override
	public EnumFacing getSecondDirection(World world, BlockPos pos) {
		return this.randomHorizontal(world);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote && rand.nextInt(50) == 0) {
			world.setBlockToAir(pos);
			return;
		}

		super.updateTick(world, pos, state, rand);
	}
}
