package com.hbm.items.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.ItemEnumMulti;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The piston set. Four materials times five fuel grades for efficiency — the table is data; the fuel grade enum belongs to the fluid subdomain,
 * so this only reads, never writes.
 */
public class ItemPistons extends ItemEnumMulti {

	public ItemPistons() {
		super(EnumPistonType.class, true, true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + grab(theEnum, stack.getItemDamage()).name().toLowerCase(Locale.US);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		EnumPistonType type = (EnumPistonType) grab(theEnum, stack.getItemDamage());

		list.add(TextFormatting.YELLOW + "Fuel efficiency:");
		for(int i = 0; i < type.eff.length; i++) {
			list.add(TextFormatting.YELLOW + "-" + FuelGrade.values()[i].getLocalizedName() + ": " + TextFormatting.RED + "" + (int) (type.eff[i] * 100) + "%");
		}
	}

	public static enum EnumPistonType {
		STEEL		(1.00, 0.75, 0.25, 0.00, 0.00),
		DURA		(0.50, 1.00, 0.90, 0.50, 0.00),
		DESH		(0.00, 0.50, 1.00, 0.75, 0.00),
		STARMETAL	(0.50, 0.75, 1.00, 0.90, 0.50);

		public double[] eff;

		private EnumPistonType(double... eff) {
			this.eff = new double[Math.min(FuelGrade.values().length, eff.length)];
			for(int i = 0; i < eff.length; i++) {
				this.eff[i] = eff[i];
			}
		}
	}
}
