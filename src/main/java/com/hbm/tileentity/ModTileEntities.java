package com.hbm.tileentity;

import com.hbm.registry.HbmRegistry;
import com.hbm.registry.IRegistrar;
import com.hbm.tileentity.machine.storage.TileEntityCrateDesh;
import com.hbm.tileentity.machine.storage.TileEntityCrateIron;
import com.hbm.tileentity.machine.storage.TileEntityCrateSteel;
import com.hbm.tileentity.machine.storage.TileEntityCrateTungsten;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import com.hbm.tileentity.machine.storage.TileEntitySafe;

/**
 * The tile entity registration unit.
 *
 * A TE's registry name is part of the save: tile entities already in the world look their class up through it, and a missing registration shows as
 * "places fine, main menu fine, the tick it saves it dies with `class … is missing a mapping!`".
 * So for any block family carrying a TE, beyond the block itself, not one mapping line here can be skipped.
 */
public class ModTileEntities implements IRegistrar {

	@Override
	public void preInit() {

		//storage family: four crate tiers + safe + mass storage
		HbmRegistry.tile(TileEntityCrateIron.class, "crate_iron");
		HbmRegistry.tile(TileEntityCrateSteel.class, "crate_steel");
		HbmRegistry.tile(TileEntityCrateDesh.class, "crate_desh");
		HbmRegistry.tile(TileEntityCrateTungsten.class, "crate_tungsten");
		HbmRegistry.tile(TileEntitySafe.class, "safe");
		HbmRegistry.tile(TileEntityMassStorage.class, "mass_storage");
	}
}
