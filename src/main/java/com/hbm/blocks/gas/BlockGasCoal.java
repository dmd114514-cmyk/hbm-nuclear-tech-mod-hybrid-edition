package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.util.ArmorRegistry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Soot: the coarse-particle tier; without the right gear it logs a black-lung note; the body is a drifting grey haze. */
public class BlockGasCoal extends BlockGasBase {

	public BlockGasCoal() {
		super(0.2F, 0.2F, 0.2F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);

		world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) entity;

		if(!ArmorRegistry.hasProtection(living, 3, HazardClass.PARTICLE_COARSE))
			HbmLivingProps.incrementBlackLung(living, 10);
	}

	@Override
	public EnumFacing getFirstDirection(World world, BlockPos pos) {

		if(world.rand.nextInt(5) == 0)
			return EnumFacing.DOWN;

		return EnumFacing.getFront(world.rand.nextInt(6));
	}

	@Override
	public EnumFacing getSecondDirection(World world, BlockPos pos) {
		return this.randomHorizontal(world);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote && rand.nextInt(20) == 0) {
			world.setBlockToAir(pos);
			return;
		}

		super.updateTick(world, pos, state, rand);
	}
}
