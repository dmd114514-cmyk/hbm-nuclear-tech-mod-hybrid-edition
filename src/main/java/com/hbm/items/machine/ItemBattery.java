package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

/**
 * The battery. Charge rides NBT's `charge` key; no tag reads as "full" —
 * so batteries straight off the production line work without an initialization pass.
 */
public class ItemBattery extends ItemBase implements IBatteryItem {

	protected long maxCharge;
	protected long chargeRate;
	protected long dischargeRate;

	public ItemBattery(long dura, long chargeRate, long dischargeRate) {
		this.maxCharge = dura;
		this.chargeRate = chargeRate;
		this.dischargeRate = dischargeRate;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		long charge = stack.getTagCompound() != null ? getCharge(stack) : maxCharge;

		list.add("Energy stored: " + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE");
		list.add("Charge rate: " + BobMathUtil.getShortNumber(chargeRate) + "HE/t");
		list.add("Discharge rate: " + BobMathUtil.getShortNumber(dischargeRate) + "HE/t");
	}

	@Override
	public void chargeBattery(ItemStack stack, long i) {
		if(!(stack.getItem() instanceof ItemBattery)) return;
		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }
		tag.setLong("charge", tag.getLong("charge") + i);
	}

	@Override
	public void setCharge(ItemStack stack, long i) {
		if(!(stack.getItem() instanceof ItemBattery)) return;
		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }
		tag.setLong("charge", i);
	}

	@Override
	public void dischargeBattery(ItemStack stack, long i) {
		if(!(stack.getItem() instanceof ItemBattery)) return;
		NBTTagCompound tag = stack.getTagCompound();

		// untagged batteries deduct from full, keeping the first hit from going negative
		long charge = tag == null ? this.maxCharge : tag.getLong("charge");
		if(tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }
		tag.setLong("charge", charge - i);
	}

	@Override
	public long getCharge(ItemStack stack) {
		if(!(stack.getItem() instanceof ItemBattery)) return 0;

		NBTTagCompound tag = stack.getTagCompound();

		if(tag == null) {
			tag = new NBTTagCompound();
			stack.setTagCompound(tag);
			tag.setLong("charge", ((ItemBattery) stack.getItem()).maxCharge);
		}

		return tag.getLong("charge");
	}

	@Override
	public long getMaxCharge(ItemStack stack) {
		return maxCharge;
	}

	@Override
	public long getChargeRate(ItemStack stack) {
		return chargeRate;
	}

	@Override
	public long getDischargeRate(ItemStack stack) {
		return dischargeRate;
	}

	public static ItemStack getEmptyBattery(Item item) {

		if(!(item instanceof ItemBattery)) return null;

		ItemStack stack = new ItemStack(item);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("charge", 0);
		stack.setTagCompound(tag);
		return stack;
	}

	public static ItemStack getFullBattery(Item item) {

		if(!(item instanceof ItemBattery)) return new ItemStack(item);

		ItemStack stack = new ItemStack(item);
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("charge", ((ItemBattery) item).getMaxCharge(stack));
		stack.setTagCompound(tag);
		return stack;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1D - (double) getCharge(stack) / (double) getMaxCharge(stack);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		// charge-only ones read empty, discharge-capable ones get a full reading, easing comparisons
		if(this.chargeRate > 0) list.add(getEmptyBattery(this));
		if(this.dischargeRate > 0) list.add(getFullBattery(this));
	}
}
