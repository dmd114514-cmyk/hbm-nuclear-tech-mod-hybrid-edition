package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ItemBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The fusion reactor's wall plates. Wear lives in NBT (far beyond item damage's ceiling); the melt point is purely cosmetic. */
public class ItemFusionShield extends ItemBase {

	public long maxDamage;
	public int maxTemp;

	public ItemFusionShield(long maxDamage, int maxTemp) {
		this.maxDamage = maxDamage;
		this.maxTemp = maxTemp;
	}

	public static long getShieldDamage(ItemStack stack) {

		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getLong("damage");
	}

	public static void setShieldDamage(ItemStack stack, long damage) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setLong("damage", damage);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		long damage = getShieldDamage(stack);
		int percent = (int) ((maxDamage - damage) * 100 / maxDamage);

		list.add("Durability: " + (maxDamage - damage) + "/" + maxDamage + " (" + percent + "%)");
		list.add("Melting point: " + TextFormatting.RED + "" + maxTemp + "°C");
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) getShieldDamage(stack) / (double) maxDamage;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) != 0;
	}
}
