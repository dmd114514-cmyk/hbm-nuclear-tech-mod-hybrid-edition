package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.blocks.generic.ModBlocksGeneric;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraft.init.Blocks;
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
 * Dense radon: radon's settled form, one dose tier heavier than radon itself (0.5 per touch), and it settles into fallout on its own.
 *
 * Drift as before — mostly sinking, occasionally rising, spreading sideways when it can't sink.
 * Dispersal splits two ways: 1/30 it vanishes leaving a fallout blob in place; 1/20 it turns the grass beneath into waste earth.
 *
 * The grass swap's dependency (`waste_earth`) shared the tomb gas's missing piece, restored once that registered.
 */
public class BlockGasRadonDense extends BlockGasBase {

	public BlockGasRadonDense() {
		super(0.1F, 0.5F, 0.1F);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) entity;

		if(ArmorRegistry.hasAllProtection(living, 3, HazardClass.PARTICLE_FINE)) {
			ArmorUtil.damageGasMaskFilter(living, 1);
		} else {
			ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F);
			living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 15 * 20, 0));
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

		if(!world.isRemote) {

			if(rand.nextInt(20) == 0) {
				BlockPos below = pos.down();

				if(world.getBlockState(below).getBlock() == Blocks.GRASS)
					world.setBlockState(below, ModBlocksGeneric.waste_earth.getDefaultState());
			}

			if(rand.nextInt(30) == 0) {
				world.setBlockToAir(pos);

				if(ModBlocksGeneric.fallout.canPlaceBlockAt(world, pos))
					world.setBlockState(pos, ModBlocksGeneric.fallout.getDefaultState());

				return;
			}
		}

		super.updateTick(world, pos, state, rand);
	}
}
