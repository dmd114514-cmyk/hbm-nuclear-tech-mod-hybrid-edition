package com.hbm.addon.sample;

import com.hbm.capability.HbmItemHandler;

import api.hbm.energymk2.IEnergyHandlerMK2;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

/**
 * Sample tile entity: a small capacitor that slowly charges itself, plus one inventory slot.
 * Its reason to exist is exercising TE registration, ITickable, the energy capability and NBT save/load in one go.
 */
public class SampleTileEntity extends TileEntity implements ITickable, IEnergyHandlerMK2 {

	public static final long CAPACITY = 10_000L;

	private long power;
	private final HbmItemHandler inventory = new HbmItemHandler(1, this::markDirty);

	@Override
	public void update() {
		// The client doesn't charge on its own, it waits for the sync packet; Phase 0 has no sync yet, so clients see 0
		if(world == null || world.isRemote) return;

		if(power < CAPACITY) {
			power += 5;
			if(power > CAPACITY) power = CAPACITY;
			markDirty();
		}
	}

	/** An unloaded chunk shouldn't keep squatting on the network */
	@Override
	public boolean isLoaded() {
		return world != null && world.isBlockLoaded(pos);
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return CAPACITY;
	}

	@Override
	public boolean canConnect(EnumFacing dir) {
		return true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setLong("power", power);
		nbt.setTag("inventory", inventory.serializeNBT());
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		power = nbt.getLong("power");
		if(nbt.hasKey("inventory")) inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
	}
}
