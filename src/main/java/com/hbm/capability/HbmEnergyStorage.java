package com.hbm.capability;

import api.hbm.energymk2.EnergyConversion;
import api.hbm.energymk2.IEnergyHandlerMK2;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Bridges HBM's long-scale power grid onto Forge's IEnergyStorage.
 *
 * Two traps: ① dimension — HBM machines deal in billions of HE while IEnergyStorage is all int,
 * so everything clamps up to Integer.MAX_VALUE ("act like an infinite capacitor" beats "overflow negative");
 * ② units — the outside sees FE; both read and write convert once by EnergyConversion's ratio,
 * default 1:1, ratio changes take effect immediately.
 */
public class HbmEnergyStorage implements IEnergyStorage {

	private final IEnergyHandlerMK2 handler;

	public HbmEnergyStorage(IEnergyHandlerMK2 handler) {
		this.handler = handler;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		if(maxReceive <= 0) return 0;

		long spaceFe = EnergyConversion.heToFe(handler.getMaxPower() - handler.getPower());
		long accepted = Math.min(Math.min((long) maxReceive, spaceFe), Integer.MAX_VALUE);
		if(accepted <= 0) return 0;

		if(!simulate) handler.setPower(Math.min(handler.getMaxPower(), handler.getPower() + EnergyConversion.feToHe(accepted)));
		return (int) accepted;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		if(maxExtract <= 0) return 0;

		long storedFe = EnergyConversion.heToFe(handler.getPower());
		long extracted = Math.min(Math.min((long) maxExtract, storedFe), Integer.MAX_VALUE);
		if(extracted <= 0) return 0;

		if(!simulate) handler.setPower(Math.max(0L, handler.getPower() - EnergyConversion.feToHe(extracted)));
		return (int) extracted;
	}

	@Override
	public int getEnergyStored() {
		return (int) Math.min(EnergyConversion.heToFe(handler.getPower()), Integer.MAX_VALUE);
	}

	@Override
	public int getMaxEnergyStored() {
		return (int) Math.min(EnergyConversion.heToFe(handler.getMaxPower()), Integer.MAX_VALUE);
	}

	@Override
	public boolean canExtract() {
		return handler instanceof IEnergyProviderMK2;
	}

	@Override
	public boolean canReceive() {
		return handler instanceof IEnergyReceiverMK2;
	}
}
