package com.hbm.tileentity;

import net.minecraft.util.math.BlockPos;

public interface IDysonConverter {

	//Try to output power at some point on the converter; false if that point won't take it
	public boolean provideEnergy(BlockPos pos, long energy);

	//Tell the dyson receiver our conversion ceiling
	public long maximumEnergy();

}
