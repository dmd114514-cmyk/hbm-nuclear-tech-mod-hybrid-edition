package com.hbm.dim;

import com.hbm.registry.IRegistrar;

/**
 * Registration unit for the dim subdomain: the construction entry point of the body data layer.
 *
 * Nothing gets pushed into Forge registries here; the single job is building the body table, same shape as FluidContent.
 * Must run before the recipe tables: the tables that read bodies (Arc Furnace / Centrifuge family) pull CelestialBody entries
 * line by line in their own init, and a missing table means null recipes. The dimensions themselves (the WorldProvider chain) aren't wired yet,
 * so canLand bodies have no matching world right after this — expected state.
 */
public class SolarSystemContent implements IRegistrar {

	@Override
	public String id() {
		return "solarsystem";
	}

	@Override
	public void init() {
		SolarSystem.init();
	}
}
