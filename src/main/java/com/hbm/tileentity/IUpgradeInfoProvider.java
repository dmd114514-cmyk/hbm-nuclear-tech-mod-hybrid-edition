package com.hbm.tileentity;

import java.util.HashMap;
import java.util.List;

import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.util.text.TextFormatting;

/** The port where machines describe their own upgrade effects: kits and GUIs both take their copy from here */
public interface IUpgradeInfoProvider {

	/** level 0 means the GUI's indicator icon is asking; non-zero means the upgrade kit's own tooltip is */
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo);
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo);
	public HashMap<UpgradeType, Integer> getValidUpgrades();

	public static String getStandardLabel(Block block) {
		return TextFormatting.YELLOW + ">>> " + I18nUtil.resolveKey(block.getUnlocalizedName() + ".name") + " <<<";
	}

	public static final String KEY_ACID = "upgrade.acid";
	public static final String KEY_BURN = "upgrade.burn";
	public static final String KEY_CONSUMPTION = "upgrade.consumption";
	public static final String KEY_COOLANT_CONSUMPTION = "upgrade.coolantConsumption";
	public static final String KEY_DELAY = "upgrade.delay";
	public static final String KEY_SPEED = "upgrade.speed";
	public static final String KEY_EFFICIENCY = "upgrade.efficiency";
	public static final String KEY_PRODUCTIVITY = "upgrade.productivity";
	public static final String KEY_FORTUNE = "upgrade.fortune";
	public static final String KEY_RANGE = "upgrade.range";
}
