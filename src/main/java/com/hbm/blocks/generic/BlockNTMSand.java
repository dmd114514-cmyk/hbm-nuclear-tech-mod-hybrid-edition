package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** The five-subtype fallible mixed sand: mixed with different materials, falling like plain sand */
public class BlockNTMSand extends BlockEnumMulti<EnumSandType> {

	public BlockNTMSand(Material mat) {
		super(mat, true, true);
	}

	@Override
	public Class<EnumSandType> getEnumClass() {
		return EnumSandType.class;
	}

	/** Texture base names differ from enum names (boron/lead/uranium/polonium/quartz), so a hand-written mapping it is */
	@Override
	public String getUnlocalizedName(net.minecraft.item.ItemStack stack) {
		String[] names = {"boron", "lead", "uranium", "polonium", "quartz"};
		return "tile.sand_" + names[Math.abs(stack.getMetadata()) % names.length];
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block b, BlockPos fromPos) {
		world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		if(!world.isRemote) {
			this.fall(world, pos);
		}
	}

	private void fall(World world, BlockPos pos) {
		if(BlockFalling.canFallThrough(world.getBlockState(pos.down())) && pos.getY() >= 0) {
			byte b0 = 32;

			if(!BlockFalling.fallInstantly && world.isAreaLoaded(pos, b0)) {
				if(!world.isRemote) {
					EntityFallingBlock entityfallingblock = new EntityFallingBlock(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, world.getBlockState(pos));
					world.spawnEntity(entityfallingblock);
				}
			} else {
				world.setBlockToAir(pos);

				BlockPos p = pos;
				while(BlockFalling.canFallThrough(world.getBlockState(p.down())) && p.getY() > 0) {
					p = p.down();
				}

				if(p.getY() > 0) {
					world.setBlockState(p, this.getDefaultState());
				}
			}
		}
	}

	@Override
	public int tickRate(World world) {
		return 2;
	}
}
