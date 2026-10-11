package com.hbm.inventory.fluid;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hbm.inventory.fluid.trait.FT_Gaseous;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * The FluidType ↔ Forge Fluid cross-reference table.
 *
 * Our side only knows its own type ids, unreachable by third parties; this mints a Forge Fluid per type and registers it,
 * letting outsiders touch our tanks with standard fluid stacks. Registry names get the hbm_ prefix and lowercase,
 * avoiding collisions with vanilla or other mods' fluids. Registration happens at type creation (preInit),
 * i.e. before the fluid registry freezes.
 */
public final class FluidForgeBridge {

	private static final Logger LOGGER = LogManager.getLogger("hbm-fluid");

	private static final Map<FluidType, Fluid> BY_TYPE = new HashMap<FluidType, Fluid>();
	private static final Map<Fluid, FluidType> BY_FLUID = new HashMap<Fluid, FluidType>();

	private FluidForgeBridge() { }

	/** Hangs a Forge Fluid when creating/registering a type; an existing same-named one is reused */
	public static void hook(FluidType type) {
		if(type == null || BY_TYPE.containsKey(type)) return;

		String id = type.getName();
		//NONE means "the can is empty", not a liquid; it takes no registry name
		if(id == null || "NONE".equals(id)) return;

		String name = ("hbm_" + id).toLowerCase(Locale.US);
		Fluid fluid = FluidRegistry.getFluid(name);

		if(fluid == null) {
			fluid = new Fluid(name, type.getTexture(), type.getTexture());
			fluid.setUnlocalizedName(type.getUnlocalizedName());
			fluid.setTemperature(type.temperature);
			fluid.setGaseous(type.hasTrait(FT_Gaseous.class));

			if(!FluidRegistry.registerFluid(fluid)) {
				Fluid existing = FluidRegistry.getFluid(name);
				if(existing == null) {
					LOGGER.warn("[fluid] {} never made it into the Forge registry (registry frozen?); in-canister conversion still works, but it's unreachable from outside", name);
				} else {
					fluid = existing;
				}
			}
		}

		BY_TYPE.put(type, fluid);
		BY_FLUID.put(fluid, type);
	}

	public static Fluid getFluid(FluidType type) {
		return type == null ? null : BY_TYPE.get(type);
	}

	public static FluidType getFluidType(Fluid fluid) {
		return fluid == null ? null : BY_FLUID.get(fluid);
	}
}
