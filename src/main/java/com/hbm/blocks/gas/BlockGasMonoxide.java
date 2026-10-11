package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Carbon monoxide: the lung-only kind; proper hazmat just burns filters, otherwise it counts as suffocation. */
public class BlockGasMonoxide extends BlockGasBase {

	public BlockGasMonoxide() {
		super(0.1F, 0.1F, 0.1F);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) entity;

		if(ArmorRegistry.hasAllProtection(living, 3, HazardClass.GAS_MONOXIDE))
			ArmorUtil.damageGasMaskFilter(living, 1);
		else
			living.attackEntityFrom(ModDamageSource.monoxide, 1);
	}

	@Override
	public EnumFacing getFirstDirection(World world, BlockPos pos) {
		return EnumFacing.DOWN;
	}

	@Override
	public EnumFacing getSecondDirection(World world, BlockPos pos) {
		return this.randomHorizontal(world);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote && rand.nextInt(100) == 0) {
			world.setBlockToAir(pos);
			return;
		}

		super.updateTick(world, pos, state, rand);
	}
}
