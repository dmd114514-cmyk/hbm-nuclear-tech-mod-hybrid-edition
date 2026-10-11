package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The wasteland's four grounds: waste_earth / waste_mycelium / frozen_grass / burning_earth.
 *
 * One class for all four, branching by instance identity (the old WasteEarth did exactly this), the base being the existing
 * {@link BlockStepEffect} — already this mod's landing spot for "trample effects + table-driven drops", with the constructor
 * {@code (Material, boolean tick)} matching the old version word for word.
 *
 * This version's changed faces:
 *   1) the old {@code registerBlockIcons} + {@code getIcon(side, meta)} trio of icons (top/bottom/side) belongs to JSON models here;
 *      the class no longer names textures; the {@code models/block/<registry name>.json}'s {@code block/cube_bottom_top} three slots carry them;
 *   2) drops (three drop dirt, frozen_grass drops snowballs) were hard-coded in {@code getItemDropped}; here they go through §15's
 *      {@code BlockLoot} bridge — four tables, one per {@code hbm:blocks/<registry name>}; change drops by changing tables;
 *   3) the old {@code World.canLightningStrikeAt} doesn't exist here (javap: only {@code canBlockSeeSky} / {@code canSeeSky} remain),
 *      its original criterion being "can see sky + raining at this cell"; both doors exist here, so the two are composed;
 *   4) {@code isFlammable(World, int, int, int, ForgeDirection)} became {@code isFlammable(IBlockAccess, BlockPos, EnumFacing)},
 *      {@code isNormalCube()} became the {@code IBlockState}-taking version, and {@code onNeighborBlockChange} became
 *      {@code neighborChanged} (one extra source-cell argument); {@code canSustainPlant} and {@code getPlantType} moved together;
 *   5) the old {@code world.getBlockLightValue(x, y+1, z)} = this version's {@code getLightFromNeighbors(above)},
 *      {@code getBlockLightOpacity} fetches per cell; and the old {@code world.blockExists} is entirely gone
 *      (javap shows no {@code *Exists*} method), replaced by this tree's synonym door {@code isBlockLoaded}.
 *
 * Two more old spots depended on blocks outside this family (burning into {@code impact_dirt}, the overhead mushroom becoming {@code mush}),
 * each held one line; both restored per the old version as they registered — this family's behavior surface is now complete.
 */
public class BlockWasteEarth extends BlockStepEffect {

	public BlockWasteEarth(Material mat, boolean tick) {
		super(mat, tick);
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {

		// The base's checks are identity-based too; none of these four match, run once so future base additions don't surprise
		super.onEntityWalk(world, pos, entity);

		if(entity instanceof EntityLivingBase) {

			EntityLivingBase living = (EntityLivingBase) entity;

			if(this == ModBlocksGeneric.frozen_grass)
				living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2 * 60 * 20, 2));

			if(this == ModBlocksGeneric.waste_mycelium)
				living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, 3));

			if(this == ModBlocksGeneric.burning_earth)
				entity.setFire(5);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {

		super.randomDisplayTick(state, world, pos, rand);

		if(this == ModBlocksGeneric.waste_mycelium) {
			world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
		}

		if(this == ModBlocksGeneric.burning_earth) {
			world.spawnParticle(EnumParticleTypes.FLAME, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		super.updateTick(world, pos, state, rand);

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		if(this == ModBlocksGeneric.waste_mycelium && GeneralConfig.enableMycelium) {
			for(int i = -1; i < 2; i++) {
				for(int j = -1; j < 2; j++) {
					for(int k = -1; k < 2; k++) {

						BlockPos p = pos.add(i, j, k);
						Block b0 = world.getBlockState(p).getBlock();
						IBlockState s1 = world.getBlockState(p.up());

						if(!s1.getBlock().isOpaqueCube(s1) && (b0 == Blocks.DIRT || b0 == Blocks.GRASS || b0 == Blocks.MYCELIUM || b0 == ModBlocksGeneric.waste_earth))
							world.setBlockState(p, ModBlocksGeneric.waste_mycelium.getDefaultState());
					}
				}
			}
		}

		if(this == ModBlocksGeneric.burning_earth) {

			if(rand.nextInt(5) == 0) {
				for(int i = -1; i < 2; i++) {
					for(int j = -1; j < 2; j++) {
						for(int k = -1; k < 2; k++) {

							BlockPos p = pos.add(i, j, k);
							BlockPos up = p.up();

							if(!world.isBlockLoaded(p))
								continue;

							Block b0 = world.getBlockState(p).getBlock();
							IBlockState s1 = world.getBlockState(up);
							Block b1 = s1.getBlock();

							if(!s1.getBlock().isOpaqueCube(s1)
									&& (b0 == Blocks.GRASS || b0 == Blocks.MYCELIUM || b0 == ModBlocksGeneric.waste_earth
											|| b0 == ModBlocksGeneric.frozen_grass || b0 == ModBlocksGeneric.waste_mycelium)
									&& !canBeStruck(world, x, y, z)) {
								world.setBlockState(p, ModBlocksGeneric.burning_earth.getDefaultState());
							}

							if(b0 instanceof BlockLeaves || b0 instanceof BlockBush)
								world.setBlockToAir(p);

							if(b0 == ModBlocksGeneric.frozen_dirt)
								world.setBlockState(p, Blocks.DIRT.getDefaultState());

							// The old version passed the outer coordinates as b1's position into isFlammable (not the cell's own); copied without digging
							if(b1.isFlammable(world, pos, EnumFacing.UP) && !(b1 instanceof BlockLeaves || b1 instanceof BlockBush) && world.isAirBlock(pos.up()))
								world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
						}
					}
				}
			}

			world.setBlockState(pos, ModBlocksGeneric.impact_dirt.getDefaultState());
		}

		if(this == ModBlocksGeneric.waste_earth || this == ModBlocksGeneric.waste_mycelium) {

			BlockPos up = pos.up();

			if(RadiationConfig.cleanupDeadDirt || (world.getLightFromNeighbors(up) < 4 && world.getBlockLightOpacity(up) > 2))
				world.setBlockState(pos, Blocks.DIRT.getDefaultState());

			if(world.getBlockState(up).getBlock() instanceof BlockMushroom)
				world.setBlockState(up, ModBlocksGeneric.mush.getDefaultState());
		}
	}

	/** The old {@code World.canLightningStrikeAt} recomposed here: can see sky + raining at this cell */
	private boolean canBeStruck(World world, int x, int y, int z) {
		BlockPos p = new BlockPos(x, y, z);
		return world.canBlockSeeSky(p) && world.isRainingAt(p);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {

		super.neighborChanged(state, world, pos, blockIn, fromPos);

		if(this == ModBlocksGeneric.burning_earth) {

			IBlockState above = world.getBlockState(pos.up());
			Block b = above.getBlock();

			if(b instanceof BlockLiquid || b instanceof BlockFluidBase || b.isOpaqueCube(above))
				world.setBlockState(pos, Blocks.DIRT.getDefaultState());
		}
	}

	@Override
	public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {

		if(this == ModBlocksGeneric.waste_earth || this == ModBlocksGeneric.waste_mycelium)
			return plantable.getPlantType(world, pos) == EnumPlantType.Cave;

		return false;
	}
}
