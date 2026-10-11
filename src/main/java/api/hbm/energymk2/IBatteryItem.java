package api.hbm.energymk2;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/** Chargeable items (batteries, energy armor...). Charge is still a long; one long in NBT is enough. */
public interface IBatteryItem {

	public void chargeBattery(ItemStack stack, long i);
	public void setCharge(ItemStack stack, long i);
	public void dischargeBattery(ItemStack stack, long i);
	public long getCharge(ItemStack stack);
	public long getMaxCharge(ItemStack stack);
	public long getChargeRate(ItemStack stack);
	public long getDischargeRate(ItemStack stack);

	/** NBT key for the stored charge */
	public default String getChargeTagName() {
		return "charge";
	}

	public static String getChargeTagName(ItemStack stack) {
		return ((IBatteryItem) stack.getItem()).getChargeTagName();
	}

	/** Copy an empty battery without touching the original */
	public static ItemStack emptyBattery(ItemStack stack) {
		if(stack != null && stack.getItem() instanceof IBatteryItem) {
			String keyName = getChargeTagName(stack);
			ItemStack stackOut = stack.copy();
			NBTTagCompound tag = new NBTTagCompound();
			tag.setLong(keyName, 0);
			stackOut.setTagCompound(tag);
			return stackOut;
		}
		return null;
	}

	public static ItemStack emptyBattery(Item item) {
		return item instanceof IBatteryItem ? emptyBattery(new ItemStack(item)) : null;
	}
}
