package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmLivingProps.ContaminationEffect;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Nuclear fallout: a thin sheet on the block below; walking on it irradiates, scratching it bare-handed contaminates.
 *
 * Drops aren't hard-coded here — the hbm:blocks/fallout and hbm:blocks/salted_fallout tables decide (contract §15);
 * the table door is inherited from BlockGenericDrop.
 */
public class BlockFallout extends BlockGenericDrop {

	private static final AxisAlignedBB THIN = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);

	/**
	 * The salted tier (salted_fallout) and the regular one are two instances of one class.
	 *
	 * The old version recognized itself via `if(this == ModBlocks.salted_fallout)` in the constructor — **dead code**:
	 * static fields assign only after new returns, so during construction it's never equal ⇒ the old salted tier was never setTickRandomly'd,
	 * and its "keep adding radiation around" never ran once. This version uses an explicit flag, but **the switch stays off** (see
	 */
	private final boolean salted;

	public BlockFallout(Material material, boolean salted) {
		super(material);
		this.salted = salted;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	/*1.12.2 has no setBlockBounds; the thin sheet reports its box here; the default collision box takes this path too, so the old two layers */
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return THIN;
	}

	/*This block has no item form (block registry only), and silk touch's path would new ItemStack(this block) ⇒ disabled */
	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		return false;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {

		BlockPos below = pos.down();
		IBlockState state = world.getBlockState(below);
		Block block = state.getBlock();

		if(block == Blocks.ICE || block == Blocks.PACKED_ICE) return false;
		if(block.isLeaves(state, world, below)) return true;
		// The old "stable at the eighth layer" check read metadata; this block carries no property and meta is always 0 ⇒ the condition copies as-is
		if(block == this && (block.getMetaFromState(state) & 7) == 7) return true;

		return state.isOpaqueCube() && block.getMaterial(state).blocksMovement();
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {

		if(world.isRemote || !(entity instanceof EntityLivingBase)) return;

		EntityLivingBase living = (EntityLivingBase) entity;
		if(living instanceof EntityPlayer && ((EntityPlayer) living).capabilities.isCreativeMode) return;

		PotionEffect effect = new PotionEffect(HbmPotion.radiation, 10 * 60 * 20, 0);
		effect.setCurativeItems(new ArrayList<ItemStack>());
		living.addPotionEffect(effect);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {

		if(world.isRemote) return;

		HbmLivingProps.addCont(player, new ContaminationEffect(1F, 200, false));
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos fromPos) {

		if(!canPlaceBlockAt(world, pos)) {
			world.setBlockToAir(pos);
		}
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, BlockPos pos) {
		return true;
	}

	/**
	 * The salted tier's incremental radiation. It hung in updateTick, driven by random ticks; 1.12.2 split that port into
	 * randomTick (random) and updateTick (scheduled), so per the old intent it wires into randomTick here.
	 *
	 * But **this method is currently never called**: the setTickRandomly enabling it was dead code in the old version too (see the salted field note);
	 * this port copies observable behavior only and doesn't activate it — activating means changing gameplay (fallout would start stacking radiation),
	 */
	@Override
	public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(salted) {
			ChunkRadiationManager.proxy.incrementRad(world, pos.getX(), pos.getY(), pos.getZ(), 50);
		}
	}
}
