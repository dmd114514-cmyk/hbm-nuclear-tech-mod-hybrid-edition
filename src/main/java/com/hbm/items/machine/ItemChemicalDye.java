package com.hbm.items.machine;

import com.hbm.items.IColoredItem;
import com.hbm.items.ItemEnumMulti;

import net.minecraft.item.ItemStack;

/**
 * The chemical dyes. Sixteen colors sharing one base, told apart by a mask layer tinted — vanilla dye's road.
 * Also registering under vanilla's sixteen dye dict names (`dictName` being the dict suffix).
 */
public class ItemChemicalDye extends ItemEnumMulti implements IColoredItem {

	public ItemChemicalDye() {
		super(EnumChemDye.class, true, false);
	}

	@Override
	public int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex != 1) return 0xFFFFFF;
		return ((EnumChemDye) grab(EnumChemDye.class, stack.getItemDamage())).color;
	}

	public static enum EnumChemDye {
		BLACK(1973019, "Black"),
		RED(11743532, "Red"),
		GREEN(3887386, "Green"),
		BROWN(5320730, "Brown"),
		BLUE(2437522, "Blue"),
		PURPLE(8073150, "Purple"),
		CYAN(2651799, "Cyan"),
		SILVER(11250603, "LightGray"),
		GRAY(4408131, "Gray"),
		PINK(14188952, "Pink"),
		LIME(4312372, "Lime"),
		YELLOW(14602026, "Yellow"),
		LIGHTBLUE(6719955, "LightBlue"),
		MAGENTA(12801229, "Magenta"),
		ORANGE(15435844, "Orange"),
		WHITE(15790320, "White");

		public int color;
		public String dictName;

		private EnumChemDye(int color, String name) {
			this.color = color;
			this.dictName = name;
		}
	}
}
