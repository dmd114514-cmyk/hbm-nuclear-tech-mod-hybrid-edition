package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBase extends Block implements IBlockNaming {

	private boolean beaconable = false;
	private boolean canSpawn = true;

	public BlockBase() {
		super(Material.ROCK);
	}

	public BlockBase(Material material) {
		super(material);
	}

	/** Chained style: marks the block as a valid beacon base */
	public BlockBase setBeaconable() {
		this.beaconable = true;
		return this;
	}

	/**
	 * 1.12.2 gives textures to JSON models; there's no setBlockTextureName anymore. This is an empty absorber,
	 * letting the old few thousand declaration chains move over verbatim while keeping the "which texture this block uses" clue.
	 * Being this class's own method, it **must lead the chain** — once a vanilla method runs, the static type falls back to Block.
	 */
	public BlockBase setBlockTextureName(String name) {
		return this;
	}

	/**
	 * 1.12.2 made the step sound a protected setSoundType(SoundType), unreachable outside the package;
	 * the old few thousand .setStepSound(Block.soundTypeX) calls come in through this door.
	 * Also this class's own method, so it **must lead the chain too**. soundTypeGravel maps to GROUND, soundTypeGrass to PLANT.
	 */
	public BlockBase setStepSound(SoundType type) {
		setSoundType(type);
		return this;
	}

	public BlockBase noMobSpawn() {
		this.canSpawn = false;
		return this;
	}

	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
		return this.canSpawn && super.canCreatureSpawn(state, world, pos, type);
	}

	@Override
	public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beaconPos) {
		return this.beaconable;
	}

	/** Clears the block and spits it out as a drop */
	public void dismantle(World world, BlockPos pos) {

		world.setBlockToAir(pos);

		ItemStack itemstack = new ItemStack(this, 1);
		float f = world.rand.nextFloat() * 0.6F + 0.2F;
		float f1 = world.rand.nextFloat() * 0.2F;
		float f2 = world.rand.nextFloat() * 0.6F + 0.2F;

		EntityItem entityitem = new EntityItem(world, pos.getX() + f, pos.getY() + f1 + 1, pos.getZ() + f2, itemstack);

		float f3 = 0.05F;
		entityitem.motionX = world.rand.nextGaussian() * f3;
		entityitem.motionY = world.rand.nextGaussian() * f3 + 0.2F;
		entityitem.motionZ = world.rand.nextGaussian() * f3;

		if(!world.isRemote)
			world.spawnEntity(entityitem);
	}
}
