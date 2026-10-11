package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/*The AMS lens. Wear rides NBT's `damage` (not item damage — it must survive sixty million-plus shots). */
public class ItemLens extends ItemBase {

	public long maxDamage;

	public ItemLens(long maxDamage) {
		this.maxDamage = maxDamage;
	}

	public static long getLensDamage(ItemStack stack) {

		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getLong("damage");
	}

	public static void setLensDamage(ItemStack stack, long damage) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setLong("damage", damage);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		long damage = getLensDamage(stack);
		int percent = (int) ((maxDamage - damage) * 100 / maxDamage);

		list.add("Durability: " + (maxDamage - damage) + "/" + maxDamage + " (" + percent + "%)");
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) getLensDamage(stack) / (double) maxDamage;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) != 0;
	}
}
