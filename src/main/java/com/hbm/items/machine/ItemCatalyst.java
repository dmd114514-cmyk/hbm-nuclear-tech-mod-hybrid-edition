package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * The AMS core's catalysts. Power/heat/fuel multipliers plus one fixed absorption,
 * deciding whether this run burns hot or lasts long — the color is cosmetic.
 */
public class ItemCatalyst extends ItemBase {

	int color;
	long powerAbs;
	float powerMod;
	float heatMod;
	float fuelMod;

	public ItemCatalyst(int color) {
		this.color = color;
		this.powerAbs = 0;
		this.powerMod = 1.0F;
		this.heatMod = 1.0F;
		this.fuelMod = 1.0F;
	}

	public ItemCatalyst(int color, long powerAbs, float powerMod, float heatMod, float fuelMod) {
		this.color = color;
		this.powerAbs = powerAbs;
		this.powerMod = powerMod;
		this.heatMod = heatMod;
		this.fuelMod = fuelMod;
	}

	public int getColor() {
		return this.color;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add("Adds spice to the core.");
		list.add("Look at all those colors!");
	}

	public static long getPowerAbs(ItemStack stack) {
		if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemCatalyst)) return 0;
		return ((ItemCatalyst) stack.getItem()).powerAbs;
	}

	public static float getPowerMod(ItemStack stack) {
		if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemCatalyst)) return 0;
		return ((ItemCatalyst) stack.getItem()).powerMod;
	}

	public static float getHeatMod(ItemStack stack) {
		if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemCatalyst)) return 0;
		return ((ItemCatalyst) stack.getItem()).heatMod;
	}

	public static float getFuelMod(ItemStack stack) {
		if(stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemCatalyst)) return 0;
		return ((ItemCatalyst) stack.getItem()).fuelMod;
	}
}
