package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.machine.ItemArcElectrode.EnumElectrodeType;

/** The burnt electrode. Shares the good electrode's material enum, just a different base image; recycling prices by material */
public class ItemArcElectrodeBurnt extends ItemEnumMulti {

	public ItemArcElectrodeBurnt() {
		super(EnumElectrodeType.class, true, true);
		this.setFull3D();
	}
}
