package com.hbm.blocks;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

/**
 * The material for gas blocks: pushable, replaceable, not solid enough to block — but not air either (grass and leaf checks must recognize it).
 *
 * The old version also overrode `getCanBlockGrass()`; 1.12.2's Material no longer has that, so it's dropped.
 */
public class MaterialGas extends Material {

	public MaterialGas() {
		super(MapColor.AIR);
		this.setNoPushMobility();
		this.setReplaceable();
	}

	@Override
	public boolean isSolid() {
		return true;
	}

	@Override
	public boolean blocksMovement() {
		return false;
	}
}
