package com.hbm.blocks.gas;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Explosive gas: the flammable tier's meaner sibling — catching fire also brings a yield-3 explosion along. */
public class BlockGasExplosive extends BlockGasFlammable {

	@Override
	protected void combust(World world, BlockPos pos) {
		super.combust(world, pos);

		world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3F, true, false);
	}
}
