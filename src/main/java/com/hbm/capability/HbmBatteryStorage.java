package com.hbm.capability;

import api.hbm.energymk2.EnergyConversion;
import api.hbm.energymk2.IBatteryItem;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * The item-side IEnergyStorage: plugs IBatteryItem's long-capacity storage into the Forge ecosystem.
 * Same dimension clamping to int max, same proportional conversion into FE.
 */
public class HbmBatteryStorage implements IEnergyStorage {

	private final net.minecraft.item.ItemStack stack;
	private final IBatteryItem battery;

	public HbmBatteryStorage(net.minecraft.item.ItemStack stack) {
		this.stack = stack;
		this.battery = (IBatteryItem) stack.getItem();
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if(maxReceive <= 0) return 0;

		long roomFe = EnergyConversion.heToFe(battery.getMaxCharge(stack) - battery.getCharge(stack));
		long rateFe = EnergyConversion.heToFe(battery.getChargeRate(stack));
		long accepted = Math.min(Math.min(Math.min((long) maxReceive, roomFe), rateFe), Integer.MAX_VALUE);
		if(accepted <= 0) return 0;

		if(!simulate) battery.chargeBattery(stack, EnergyConversion.feToHe(accepted));
		return (int) accepted;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if(maxExtract <= 0) return 0;

		long storedFe = EnergyConversion.heToFe(battery.getCharge(stack));
		long rateFe = EnergyConversion.heToFe(battery.getDischargeRate(stack));
		long extracted = Math.min(Math.min(Math.min((long) maxExtract, storedFe), rateFe), Integer.MAX_VALUE);
		if(extracted <= 0) return 0;

		if(!simulate) battery.dischargeBattery(stack, EnergyConversion.feToHe(extracted));
		return (int) extracted;
	}

	@Override
	public int getEnergyStored() {
		return (int) Math.min(EnergyConversion.heToFe(battery.getCharge(stack)), Integer.MAX_VALUE);
	}

	@Override
	public int getMaxEnergyStored() {
		return (int) Math.min(EnergyConversion.heToFe(battery.getMaxCharge(stack)), Integer.MAX_VALUE);
	}

	@Override
	public boolean canExtract() {
		return battery.getDischargeRate(stack) > 0;
	}

	@Override
	public boolean canReceive() {
		return battery.getChargeRate(stack) > 0;
	}
}
