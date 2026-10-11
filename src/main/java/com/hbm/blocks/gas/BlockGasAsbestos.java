package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.util.ArmorRegistry;

import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Asbestos dust: the fine-particle tier; ungated gear means a lung note; the body is a drifting grey-white haze. */
public class BlockGasAsbestos extends BlockGasBase {

	public BlockGasAsbestos() {
		super(0.6F, 0.6F, 0.5F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);

		if(world.rand.nextInt(5) == 0)
			world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(entity instanceof EntityLivingBase) {

			EntityLivingBase living = (EntityLivingBase) entity;

			if(!ArmorRegistry.hasProtection(living, 3, HazardClass.PARTICLE_FINE))
				HbmLivingProps.incrementAsbestos(living, 1);
		}
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

		if(!world.isRemote && rand.nextInt(50) == 0) {
			world.setBlockToAir(pos);
			return;
		}

		super.updateTick(world, pos, state, rand);
	}
}
