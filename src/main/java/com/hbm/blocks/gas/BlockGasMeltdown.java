package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Meltdown gas: the reactor-meltdown poison, the heaviest dose tier — contact means 0.5 radiation plus 60 seconds of Radiation 3,
 * with gear only deciding "burn the filter" vs "log an asbestos note on the lungs"; the dose itself isn't blocked.
 *
 * It also breeds dense radon: each round, 1/7 of placing a {@link ModBlocksGas#gas_radon_dense} blob into a random empty neighbor;
 * outdoors it adds 5 doses to its chunk directly; only 1/350 disperses it.
 */
public class BlockGasMeltdown extends BlockGasBase {

	public BlockGasMeltdown() {
		super(0.1F, 0.4F, 0.1F);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) entity;

		ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F);
		living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 60 * 20, 2));

		// The old line's own comment: lungs irradiated still grow mesothelioma, so without gear an asbestos note is logged anyway
		if(ArmorRegistry.hasAllProtection(living, 3, HazardClass.PARTICLE_FINE)) {
			ArmorUtil.damageGasMaskFilter(living, 1);
		} else {
			HbmLivingProps.incrementAsbestos(living, 5);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);

		world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
	}

	@Override
	public EnumFacing getFirstDirection(World world, BlockPos pos) {

		if(world.rand.nextInt(2) == 0)
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

			EnumFacing dir = EnumFacing.getFront(rand.nextInt(6));
			BlockPos next = pos.offset(dir);

			if(rand.nextInt(7) == 0 && world.isAirBlock(next))
				world.setBlockState(next, ModBlocksGas.gas_radon_dense.getDefaultState());

			if(world.canBlockSeeSky(pos))
				ChunkRadiationManager.proxy.incrementRad(world, pos.getX(), pos.getY(), pos.getZ(), 5);

			if(rand.nextInt(350) == 0) {
				world.setBlockToAir(pos);
				return;
			}
		}

		super.updateTick(world, pos, state, rand);
	}
}
