package com.hbm.capability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.CapabilityEnergy;

import net.minecraft.item.ItemStack;

/** Makes a battery item present itself to Forge as an IEnergyStorage */
public class CapabilityBatteryProvider implements ICapabilityProvider {

	private final HbmBatteryStorage storage;

	public CapabilityBatteryProvider(ItemStack stack) {
		this.storage = new HbmBatteryStorage(stack);
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
