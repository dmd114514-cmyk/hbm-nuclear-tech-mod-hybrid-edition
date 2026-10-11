package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The self-charging battery (radiovoltaic): charge isn't written to NBT but **decided by meta** —
 * how much power depends on which one it is, so the charge/discharge entries are all empty and reads always return the rated figure.
 *
 * Which also explains dischargeRate == maxCharge: these aren't designed to charge or discharge,
 * and the tooltip's "not suited for battery slots" exists to back that behavior.
 */
public class ItemBatterySC extends ItemEnumMulti implements IBatteryItem {

	public ItemBatterySC() {
		super(EnumBatterySC.class, true, true);
		this.setMaxStackSize(1);
		this.setCreativeTab(ModItems.controlTab);
	}

	public static enum EnumBatterySC {

		EMPTY(0),
		WASTE(150),
		RA226(200),
		TC99(500),
		CO60(750),
		PU238(1_000),
		PO210(1_250),
		AU198(1_500),
		PB209(2_000),
		AM241(2_500);

		public long power;

		private EnumBatterySC(long power) {
			this.power = power;
		}
	}

	/** Out-of-range meta falls back to the EMPTY tier; a bad NBT mustn't stall a whole machine */
	private static EnumBatterySC of(ItemStack stack) {

		EnumBatterySC[] values = EnumBatterySC.values();
		int meta = stack.getMetadata();

		if(meta < 0 || meta >= values.length)
			return values[0];

		return values[meta];
	}

	@Override public void chargeBattery(ItemStack stack, long i) { }
	@Override public void setCharge(ItemStack stack, long i) { }
	@Override public void dischargeBattery(ItemStack stack, long i) { }
	@Override public long getChargeRate(ItemStack stack) { return 0; }

	@Override public long getCharge(ItemStack stack) { return getMaxCharge(stack); }
	@Override public long getDischargeRate(ItemStack stack) { return getMaxCharge(stack); }

	@Override
	public long getMaxCharge(ItemStack stack) {
		return of(stack).power;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		EnumBatterySC pack = of(stack);

		if(pack.power > 0)
			list.add(TextFormatting.YELLOW + "Discharge rate: " + BobMathUtil.getShortNumber(pack.power) + "HE/t");

		for(String line : I18nUtil.resolveKeyArray(this.getUnlocalizedName() + ".desc"))
			list.add(TextFormatting.RED + line);
	}
}
