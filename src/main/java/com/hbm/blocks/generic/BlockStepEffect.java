package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.BlockLoot;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The trample-effect, varied-drop "ore and deco" pile (the old author complained about this one himself).
 *
 * Two behaviors ported as before:
 *   1) trample applies effects — permafrost slows, jungle bricks radiate/pollute, molten meteorite ignites;
 *   2) molten meteorite cools into meteorite cobble on random tick and leaves lava when mined.
 *
 * The drop layer changed per this project's style: the old version hard-coded drops in `getItemDropped`, and 1.12.2's block side has no table door there
 * (Block has no getLootTable, and the context carries neither coordinates nor a tool), so it goes through the {@link BlockLoot} bridge instead —
 * each block with an `hbm:blocks/<registry name>` table — change drops by changing tables; no entries, nothing drops.
 * The cost: fortune can't reach the table (no tool in the context), so the old quantityDroppedWithBonus multipliers follow it out —
 * a registered §15 mechanism exception, not an omission. Silk touch still handled by BlockLoot vanilla-style.
 */
public class BlockStepEffect extends BlockBase {

	public BlockStepEffect(Material mat) {
		super(mat);
	}

	public BlockStepEffect(Material mat, boolean tick) {
		super(mat);
		this.setTickRandomly(tick);
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		BlockLoot.harvest(world, pos, state, player, stack);
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {

		if(entity instanceof EntityLivingBase) {
			EntityLivingBase living = (EntityLivingBase) entity;

			if(this == ModBlocksGeneric.frozen_dirt)
				living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2 * 60 * 20, 2));

			if(this == ModBlocksGeneric.brick_jungle_ooze)
				living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 15 * 20, 9));

			if(this == ModBlocksGeneric.brick_jungle_mystic)
				living.addPotionEffect(new PotionEffect(HbmPotion.taint, 15 * 20, 2));
		}

		if(this == ModBlocksGeneric.block_meteor_molten)
			entity.setFire(5);
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(this == ModBlocksGeneric.block_meteor_molten) {
			if(!world.isRemote)
				world.setBlockState(pos, ModBlocksGeneric.block_meteor_cobble.getDefaultState());
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			return;
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {

		if(this == ModBlocksGeneric.block_meteor_molten) {
			if(!world.isRemote)
				world.setBlockState(pos, Blocks.LAVA.getDefaultState());
		}
	}
}
