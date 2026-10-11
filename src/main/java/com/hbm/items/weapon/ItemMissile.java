package com.hbm.items.weapon;

import java.util.List;

import com.hbm.items.ItemCustomLore;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The ballistic missile itself. The model decides default fuel and caliber; the fuel decides the ceiling and display color.
 * The `notLaunchable()` ones are display pieces (a rusted Armageddon, say) and still sit on the launch pad just fine.
 */
public class ItemMissile extends ItemCustomLore {

	public final MissileFormFactor formFactor;
	public final MissileTier tier;
	public final MissileFuel fuel;
	public int fuelCap;
	public boolean launchable = true;

	public ItemMissile(MissileFormFactor form, MissileTier tier) {
		this(form, tier, form.defaultFuel);
	}

	public ItemMissile(MissileFormFactor form, MissileTier tier, MissileFuel fuel) {
		this.formFactor = form;
		this.tier = tier;
		this.fuel = fuel;
		this.setFuelCap(this.fuel.defaultCap);
	}

	public ItemMissile notLaunchable() {
		this.launchable = false;
		return this;
	}

	public ItemMissile setFuelCap(int fuelCap) {
		this.fuelCap = fuelCap;
		return this;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.ITALIC + I18nUtil.resolveKey("item.missile.tier." + this.tier.name().toLowerCase()));

		if(!this.launchable) {
			list.add(TextFormatting.RED + I18nUtil.resolveKey("item.missile.desc.notLaunchable"));
		} else {
			list.add(I18nUtil.resolveKey("item.missile.desc.fuel") + ": " + this.fuel.getDisplay());
			if(this.fuelCap > 0) list.add(I18nUtil.resolveKey("item.missile.desc.fuelCapacity") + ": " + this.fuelCap + "mB");
			super.addInformation(stack, world, list, flag);
		}
	}

	public enum MissileFormFactor {
		ABM(MissileFuel.SOLID),
		MICRO(MissileFuel.SOLID),
		V2(MissileFuel.ETHANOL_PEROXIDE),
		STRONG(MissileFuel.KEROSENE_PEROXIDE),
		HUGE(MissileFuel.KEROSENE_LOXY),
		ATLAS(MissileFuel.JETFUEL_LOXY),
		OTHER(MissileFuel.KEROSENE_PEROXIDE);

		protected MissileFuel defaultFuel;

		private MissileFormFactor(MissileFuel defaultFuel) {
			this.defaultFuel = defaultFuel;
		}
	}

	public enum MissileTier {
		TIER0("Tier 0"),
		TIER1("Tier 1"),
		TIER2("Tier 2"),
		TIER3("Tier 3"),
		TIER4("Tier 4");

		public String display;

		private MissileTier(String display) {
			this.display = display;
		}
	}

	public enum MissileFuel {
		SOLID("item.missile.fuel.solid.prefueled", TextFormatting.GOLD, 0),
		ETHANOL_PEROXIDE("item.missile.fuel.ethanol_peroxide", TextFormatting.AQUA, 4_000),
		KEROSENE_PEROXIDE("item.missile.fuel.kerosene_peroxide", TextFormatting.BLUE, 8_000),
		KEROSENE_LOXY("item.missile.fuel.kerosene_loxy", TextFormatting.LIGHT_PURPLE, 12_000),
		JETFUEL_LOXY("item.missile.fuel.jetfuel_loxy", TextFormatting.RED, 16_000);

		private final String key;
		public final TextFormatting color;
		public final int defaultCap;

		private MissileFuel(String key, TextFormatting color, int defaultCap) {
			this.key = key;
			this.color = color;
			this.defaultCap = defaultCap;
		}

		/** Fuel names with color codes, for tooltip text */
		public String getDisplay() {
			return color + I18nUtil.resolveKey(this.key);
		}
	}
}
