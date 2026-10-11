package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** The arc furnace electrode. Wear rides NBT; the four materials' durabilities span two orders of magnitude. */
public class ItemArcElectrode extends ItemEnumMulti {

	public ItemArcElectrode() {
		super(EnumElectrodeType.class, true, true);
		this.setFull3D();
		this.setMaxStackSize(1);
	}

	public static int getDurability(ItemStack stack) {
		if(!stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("durability");
	}

	public static boolean damage(ItemStack stack) {

		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());

		int durability = stack.getTagCompound().getInteger("durability") + 1;
		stack.getTagCompound().setInteger("durability", durability);

		return durability >= getMaxDurability(stack);
	}

	public static int getMaxDurability(ItemStack stack) {
		return EnumElectrodeType.values()[Math.abs(stack.getItemDamage() % EnumElectrodeType.values().length)].durability;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) > 0D;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return (double) getDurability(stack) / (double) getMaxDurability(stack);
	}

	public static enum EnumElectrodeType {
		GRAPHITE(	10),
		LANTHANIUM(	100),
		DESH(		500),
		SATURNITE(	1500);

		public int durability;

		private EnumElectrodeType(int dura) {
			this.durability = dura;
		}
	}
}
