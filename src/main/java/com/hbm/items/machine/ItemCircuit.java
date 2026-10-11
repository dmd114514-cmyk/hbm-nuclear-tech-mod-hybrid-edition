package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * The circuit boards. Thirty components in the enum, but the creative tab only shows the ones with legitimate sources —
 * the rest (intermediates like wired vias) aren't handed to players directly, only craftable.
 */
public class ItemCircuit extends ItemEnumMulti {

	public ItemCircuit() {
		super(EnumCircuitType.class, true, true);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		int[] shown = {
				EnumCircuitType.VACUUM_TUBE.ordinal(), EnumCircuitType.NUMITRON.ordinal(),
				EnumCircuitType.CAPACITOR.ordinal(), EnumCircuitType.CAPACITOR_TANTALIUM.ordinal(),
				EnumCircuitType.ATOMIC_CLOCK.ordinal(), EnumCircuitType.PCB.ordinal(),
				EnumCircuitType.SILICON.ordinal(), EnumCircuitType.CHIP.ordinal(),
				EnumCircuitType.CHIP_BISMOID.ordinal(), EnumCircuitType.CHIP_QUANTUM.ordinal(),
				EnumCircuitType.ANALOG.ordinal(), EnumCircuitType.BASIC.ordinal(),
				EnumCircuitType.ADVANCED.ordinal(), EnumCircuitType.CAPACITOR_BOARD.ordinal(),
				EnumCircuitType.BISMOID.ordinal(), EnumCircuitType.AVIONICS.ordinal(),
				EnumCircuitType.AERO.ordinal(), EnumCircuitType.GAAS.ordinal(),
				EnumCircuitType.QUANTUM.ordinal(), EnumCircuitType.CONTROLLER_CHASSIS.ordinal(),
				EnumCircuitType.CONTROLLER.ordinal(), EnumCircuitType.CONTROLLER_ADVANCED.ordinal(),
				EnumCircuitType.CONTROLLER_QUANTUM.ordinal(), EnumCircuitType.CAPACITOR_LANTHANIUM.ordinal(),
				EnumCircuitType.PROCESST1.ordinal(), EnumCircuitType.PROCESST2.ordinal(),
				EnumCircuitType.PROCESST3.ordinal(), EnumCircuitType.GASCHIP.ordinal(),
				EnumCircuitType.HFCHIP.ordinal(), EnumCircuitType.MOLYCHIP.ordinal()
		};

		for(int meta : shown) list.add(new ItemStack(this, 1, meta));
	}

	public static enum EnumCircuitType {
		VACUUM_TUBE,
		CAPACITOR,
		CAPACITOR_TANTALIUM,
		PCB,
		SILICON,
		GAAS,
		CHIP,
		CHIP_BISMOID,
		ANALOG,
		BASIC,
		ADVANCED,
		CAPACITOR_BOARD,
		BISMOID,
		AVIONICS,
		AERO,
		CONTROLLER_CHASSIS,
		CONTROLLER,
		CONTROLLER_ADVANCED,
		QUANTUM,
		CHIP_QUANTUM,
		CONTROLLER_QUANTUM,
		ATOMIC_CLOCK,
		CAPACITOR_LANTHANIUM,
		PROCESST1,
		PROCESST2,
		PROCESST3,
		GASCHIP,
		HFCHIP,
		MOLYCHIP,
		NUMITRON,
	}
}
