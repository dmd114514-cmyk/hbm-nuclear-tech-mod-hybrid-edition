package com.hbm.items.machine;

import java.util.List;

import com.hbm.interfaces.IOrderedEnum;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * Batteries and capacitors. Subtypes line up by capacity: batteries get more charge-tolerant toward the end, capacitors being "fast in, fast out, few cycles"
 *
 * The enum's `texture` points at machine textures (for the machines that mount them), so it's public —
 * the render side reads it directly, no getter layer.
 */
public class ItemBatteryPack extends ItemEnumMulti implements IBatteryItem {

	public ItemBatteryPack() {
		super(EnumBatteryPack.class, true, false);
		this.setMaxStackSize(1);
		this.setCreativeTab(ModItems.controlTab);
	}

	public static enum EnumBatteryPack {
		BATTERY_REDSTONE	("battery_redstone",	      100L, false),
		BATTERY_LEAD		("battery_lead",		    1_000L, false),
		BATTERY_LITHIUM		("battery_lithium",		   10_000L, false),
		BATTERY_SODIUM		("battery_sodium",		   50_000L, false),
		BATTERY_SCHRABIDIUM	("battery_schrabidium",	  250_000L, false),
		BATTERY_QUANTUM		("battery_quantum",		1_000_000L, 20 * 60 * 60),

		CAPACITOR_COPPER	("capacitor_copper",	     1_000L, true),
		CAPACITOR_GOLD		("capacitor_gold",		    10_000L, true),
		CAPACITOR_NIOBIUM	("capacitor_niobium",	   100_000L, true),
		CAPACITOR_TANTALUM	("capacitor_tantalum",	   500_000L, true),
		CAPACITOR_BISMUTH	("capacitor_bismuth",	 2_500_000L, true),
		CAPACITOR_SPARK		("capacitor_spark",		10_000_000L, true);

		public ResourceLocation texture;
		public long capacity;
		public long chargeRate;
		public long dischargeRate;

		private EnumBatteryPack(String tex, long dischargeRate, boolean capacitor) {
			this(tex,
					capacitor ? (dischargeRate * 20 * 30) : (dischargeRate * 20 * 60 * 15),
					capacitor ? dischargeRate : dischargeRate * 10,
					dischargeRate);
		}

		private EnumBatteryPack(String tex, long dischargeRate, long duration) {
			this(tex, dischargeRate * duration, dischargeRate * 10, dischargeRate);
		}

		private EnumBatteryPack(String tex, long capacity, long chargeRate, long dischargeRate) {
			this.texture = new ResourceLocation(RefStrings.MODID, "textures/models/machines/" + tex + ".png");
			this.capacity = capacity;
			this.chargeRate = chargeRate;
			this.dischargeRate = dischargeRate;
		}

		public boolean isCapacitor() { return this.ordinal() > BATTERY_QUANTUM.ordinal(); }
		public ItemStack stack() { return new ItemStack(ModItems.battery_pack, 1, this.ordinal()); }
	}

	@Override
	public void chargeBattery(ItemStack stack, long i) {

		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }

		tag.setLong("charge", tag.getLong("charge") + i);
	}

	@Override
	public void setCharge(ItemStack stack, long i) {

		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }

		tag.setLong("charge", i);
	}

	@Override
	public void dischargeBattery(ItemStack stack, long i) {

		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }

		tag.setLong("charge", tag.getLong("charge") - i);
	}

	@Override
	public long getCharge(ItemStack stack) {

		NBTTagCompound tag = stack.getTagCompound();
		if(tag == null) { tag = new NBTTagCompound(); stack.setTagCompound(tag); }

		return tag.getLong("charge");
	}

	@Override
	public long getMaxCharge(ItemStack stack) {
		return ((EnumBatteryPack) grab(EnumBatteryPack.class, stack.getItemDamage())).capacity;
	}

	@Override
	public long getChargeRate(ItemStack stack) {
		return ((EnumBatteryPack) grab(EnumBatteryPack.class, stack.getItemDamage())).chargeRate;
	}

	@Override
	public long getDischargeRate(ItemStack stack) {
		return ((EnumBatteryPack) grab(EnumBatteryPack.class, stack.getItemDamage())).dischargeRate;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDurabilityForDisplay(stack) != 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1D - (double) getCharge(stack) / (double) getMaxCharge(stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		long maxCharge = this.getMaxCharge(stack);
		long chargeRate = this.getChargeRate(stack);
		long dischargeRate = this.getDischargeRate(stack);
		long charge = stack.hasTagCompound() ? getCharge(stack) : maxCharge;

		list.add(TextFormatting.GREEN + "Energy stored: " + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE (" + (charge * 1000 / maxCharge / 10D) + "%)");
		list.add(TextFormatting.YELLOW + "Charge rate: " + BobMathUtil.getShortNumber(chargeRate) + "HE/t");
		list.add(TextFormatting.YELLOW + "Discharge rate: " + BobMathUtil.getShortNumber(dischargeRate) + "HE/t");
		list.add(TextFormatting.GOLD + "Time for full charge: " + (maxCharge / chargeRate / 20 / 60D) + "min");
		list.add(TextFormatting.GOLD + "Charge lasts for: " + (maxCharge / dischargeRate / 20 / 60D) + "min");
	}

	public static ItemStack makeEmptyBattery(ItemStack stack) {

		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("charge", 0);
		stack.setTagCompound(tag);

		return stack;
	}

	public static ItemStack makeFullBattery(ItemStack stack) {

		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("charge", ((ItemBatteryPack) stack.getItem()).getMaxCharge(stack));
		stack.setTagCompound(tag);

		return stack;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		Enum[] order = theEnum.getEnumConstants();
		if(order[0] instanceof IOrderedEnum) order = ((IOrderedEnum) order[0]).getOrder();

		for(Enum e : order) {
			list.add(makeEmptyBattery(new ItemStack(this, 1, e.ordinal())));
			list.add(makeFullBattery(new ItemStack(this, 1, e.ordinal())));
		}
	}
}
