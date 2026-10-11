package com.hbm.blocks.generic;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The radiation block hot enough to steam neighboring water (the polonium and plutonium-238 blocks).
 *
 * Radiation and tier particles ride the {@link BlockHazard} base; this only layers two purely cosmetic bits:
 *   1) if the cell above is open sky (i.e. exposed), a wisp of cloud rises from the top face;
 *   2) in the six directions minus down, a water neighbor steams against that face.
 * Both run client-side only; the server executes none of it.
 *
 * One delta: the old {@code world.canLightningStrikeAt(x, y + 1, z)} is gone from 1.12.2's World surface
 * (javap-verified; only {@code canSeeSky} / {@code canBlockSeeSky} remain), so its main criterion "can see sky" substitutes here.
 */
public class BlockHotHazard extends BlockHazard {

	@SideOnly(Side.CLIENT)
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		super.randomDisplayTick(state, world, pos, rand);

		if(world.canBlockSeeSky(pos.up())) {
			world.spawnParticle(EnumParticleTypes.CLOUD, pos.getX() + rand.nextFloat(), pos.getY() + 1, pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
		}

		for(EnumFacing dir : EnumFacing.VALUES) {

			if(dir == EnumFacing.DOWN)
				continue;

			if(world.getBlockState(pos.offset(dir)).getMaterial() == Material.WATER) {

				double ix = pos.getX() + 0.5F + dir.getFrontOffsetX() + rand.nextDouble() - 0.5D;
				double iy = pos.getY() + 0.5F + dir.getFrontOffsetY() + rand.nextDouble() - 0.5D;
				double iz = pos.getZ() + 0.5F + dir.getFrontOffsetZ() + rand.nextDouble() - 0.5D;

				if(dir.getFrontOffsetX() != 0)
					ix = pos.getX() + 0.5F + dir.getFrontOffsetX() * 0.5 + rand.nextDouble() * 0.125 * dir.getFrontOffsetX();
				if(dir.getFrontOffsetY() != 0)
					iy = pos.getY() + 0.5F + dir.getFrontOffsetY() * 0.5 + rand.nextDouble() * 0.125 * dir.getFrontOffsetY();
				if(dir.getFrontOffsetZ() != 0)
					iz = pos.getZ() + 0.5F + dir.getFrontOffsetZ() * 0.5 + rand.nextDouble() * 0.125 * dir.getFrontOffsetZ();

				world.spawnParticle(EnumParticleTypes.CLOUD, ix, iy, iz, 0.0D, 0.0D, 0.0D);
			}
		}
	}
}
