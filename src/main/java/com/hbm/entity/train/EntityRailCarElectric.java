package com.hbm.entity.train;

import com.hbm.items.ModItems;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

/**
 * The electric layer: the car carries one battery, moving power from battery into the onboard capacitor each tick.
 *
 * Note the old version already had the two "drain power" lines commented out (canAccelerate just returns true, consumeFuel is empty),
 * the charge being display-only and kept for later — copied unchanged, since changing it changes gameplay, not the port.
 */
public abstract class EntityRailCarElectric extends EntityRailCarRidable {

	private static final DataParameter<Integer> DW_POWER = EntityDataManager.createKey(EntityRailCarElectric.class, DataSerializers.VARINT);

	public EntityRailCarElectric(World world) {
		super(world);
	}

	public abstract int getMaxPower();
	public abstract int getPowerConsumption();

	public boolean hasChargeSlot() { return false; }
	public int getChargeSlot() { return 0; }

	@Override protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_POWER, Integer.valueOf(0));
	}

	@Override public boolean canAccelerate() {
		return true;
		//return this.getPower() >= this.getPowerConsumption();
	}

	@Override public void consumeFuel() {
		//this.setPower(this.getPower() - this.getPowerConsumption());
	}

	public void setPower(int power) {
		this.getDataManager().set(DW_POWER, Integer.valueOf(power));
	}

	public int getPower() {
		return this.getDataManager().get(DW_POWER).intValue();
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(!world.isRemote) {

			if(this.hasChargeSlot()) {
				ItemStack stack = this.getStackInSlot(this.getChargeSlot());

				if(!stack.isEmpty() && stack.getItem() instanceof IBatteryItem) {
					IBatteryItem battery = (IBatteryItem) stack.getItem();
					int powerNeeded = this.getMaxPower() - this.getPower();
					long powerProvided = Math.min(battery.getDischargeRate(stack), battery.getCharge(stack));
					int powerTransfered = (int) Math.min(powerNeeded, powerProvided);

					if(powerTransfered > 0) {
						battery.dischargeBattery(stack, powerTransfered);
						this.setPower(this.getPower() + powerTransfered);
					}
				} else if(!stack.isEmpty()) {
					if(stack.getItem() == ModItems.battery_creative) {
						this.setPower(this.getMaxPower());
					}
				}
			}
		}
	}
}
