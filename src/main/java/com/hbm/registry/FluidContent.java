package com.hbm.registry;

import com.hbm.inventory.fluid.Fluids;

/**
 * The creation unit for fluid types.
 *
 * The creation unit for fluid types: FluidType keeps its own id table and Forge has no matching RegistryEvent
 * (Fluid isn't an IForgeRegistryEntry), so someone has to call `Fluids.init()` explicitly once;
 * miss it and every fluids static field stays null — compilation and startup both fine, until some fluid item asks for its display name and blows up.
 * Per §16, FluidForgeBridge also hangs a Forge Fluid off each type at registration (before the registries freeze).
 * It hangs in the assembly point rather than some class's static block, so it shows up in the assembly log and gets checked round by round.
 */
public class FluidContent implements IRegistrar {

	@Override
	public String id() {
		return "fluids";
	}

	@Override
	public void registerFluids() {
		// Toxic traits reference potion objects directly when building effects; brew the potions before the fluids
		com.hbm.potion.HbmPotions.build();
		Fluids.init();
	}
}
