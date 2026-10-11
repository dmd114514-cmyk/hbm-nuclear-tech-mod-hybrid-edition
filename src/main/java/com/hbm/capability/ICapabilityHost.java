package com.hbm.capability;

import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * The machine's external Q&A port: the base class implements it and forwards to CapabilityHandler in getCapability,
 * subclasses only override the few things they actually hold; no hand-written getCapability per machine.
 *
 * Returning null means "this machine has none of that"; the proxy skips it automatically.
 */
public interface ICapabilityHost {

	/** The Forge view of the machine's energy storage (usually an HbmEnergyStorage wrapping it) */
	default IEnergyStorage getEnergyStorage() {
		return null;
	}

	/** The Forge view of the machine's tanks; with multiple tanks, return the merged one */
	default IFluidHandler getFluidHandler() {
		return null;
	}

	/** The Forge view of the machine's inventory (usually an HbmArrayItemHandler backed by ItemStack[]) */
	default IItemHandler getItemHandler() {
		return null;
	}
}
