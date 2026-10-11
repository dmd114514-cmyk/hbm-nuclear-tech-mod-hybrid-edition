package com.hbm.world.gen.nbt;

import net.minecraft.world.World;

/** Same idea as INBTBlockTransformable but for TileEntities: lets a tile entity adjust its own state when spawned from a .nbt structure */
public interface INBTTileEntityTransformable {

	// coordBaseMode is the structure's facing baseline (0 south / 1 west / 2 north / 3 east)
	public void transformTE(World world, int coordBaseMode);

}
