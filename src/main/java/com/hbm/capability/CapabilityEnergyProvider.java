package com.hbm.capability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/** Attaches an IEnergyStorage as a Forge capability */
public class CapabilityEnergyProvider implements ICapabilityProvider {

	private final IEnergyStorage storage;

	public CapabilityEnergyProvider(IEnergyStorage storage) {
		this.storage = storage;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(capability == CapabilityEnergy.ENERGY) return CapabilityEnergy.ENERGY.cast(this.storage);
		return null;
	}
}
