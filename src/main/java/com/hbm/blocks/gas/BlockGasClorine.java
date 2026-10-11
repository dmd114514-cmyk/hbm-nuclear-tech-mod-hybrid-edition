package com.hbm.blocks.gas;

import java.util.Random;

import api.hbm.hazard.HazardClass;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorUtil;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Chlorine: one breath blinds, poisons, withers and weighs the limbs; the gas mask (GAS_LUNG tier) blocks it at the cost of burning filters.
 *
 * One writing delta: the old chlorine was **visible** as a translucent cube (`getRenderType()` returning 0, on the transparent pass),
 * unlike its invisible siblings, so `shouldSideBeRendered` falls back to vanilla's neighbor-face culling here,
 * letting it draw via the existing `hbm:chlorine_gas` model; the transparent pass is model-JSON/atlas territory in 1.12.2, no longer set in code.
 */
public class BlockGasClorine extends BlockGasBase {

	public BlockGasClorine() {
		super(0.7F, 0.8F, 0.6F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		IBlockState neighbor = world.getBlockState(pos.offset(side));
		Block block = neighbor.getBlock();

		if(block == this)
			return false;

		return !block.isOpaqueCube(neighbor);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {

		if(!(entity instanceof EntityLivingBase))
			return;

		EntityLivingBase living = (EntityLivingBase) entity;

		if(ArmorRegistry.hasAllProtection(living, 3, HazardClass.GAS_LUNG)) {
			ArmorUtil.damageGasMaskFilter(living, 1);
		} else {
			living.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 5 * 20, 0));
			living.addPotionEffect(new PotionEffect(MobEffects.POISON, 20 * 20, 2));
			living.addPotionEffect(new PotionEffect(MobEffects.WITHER, 1 * 20, 1));
			living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 30 * 20, 1));
			living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 30 * 20, 2));
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

		if(!world.isRemote && rand.nextInt(10) == 0) {
			world.setBlockToAir(pos);
			return;
		}

		super.updateTick(world, pos, state, rand);
	}
}
