package com.hbm.inventory.fluid.trait;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;

import net.minecraft.util.text.TextFormatting;

public class FluidTraitSimple {

	/** Gaseous at room temperature, like cryogenic hydrogen */
	public static class FT_Gaseous_ART extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("hbmfluid.trait.gaseousRoom") + "]");
		}
	}

	public static class FT_Liquid extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("hbmfluid.trait.liquid") + "]");
		}
	}

	/** Too viscous to atomize */
	public static class FT_Viscous extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("hbmfluid.trait.viscous") + "]");
		}
	}

	@Deprecated public static class FT_Plasma extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.LIGHT_PURPLE + "[Plasma]");
		}
	}

	public static class FT_Amat extends FluidTrait {
		@Override public void addInfo(List<String> info) {
			info.add(TextFormatting.DARK_RED + "[" + I18nUtil.resolveKey("hbmfluid.trait.antimatter") + "]");
		}
	}

	public static class FT_LeadContainer extends FluidTrait {
		@Override public void addInfo(List<String> info) {
			info.add(TextFormatting.DARK_RED + "[" + I18nUtil.resolveKey("hbmfluid.trait.leadContainer") + "]");
		}
	}

	public static class FT_Delicious extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.DARK_GREEN + "[" + I18nUtil.resolveKey("hbmfluid.trait.delicious") + "]");
		}
	}

	public static class FT_ULTRAKILL extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.DARK_RED + "[ULTRAKILL]");
		}
	}

	public static class FT_EXPLOSIVE extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.RED + "[Explosive]");
		}
	}

	public static class FT_Leaded extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[Leaded Fuel]");
		}
	}

	public static class FT_Unsiphonable extends FluidTrait {
		@Override public void addInfoHidden(List<String> info) {
			info.add(TextFormatting.BLUE + "[" + I18nUtil.resolveKey("hbmfluid.trait.unsiphonable") + "]");
		}
	}

	public static class FT_NoID extends FluidTrait { }
	public static class FT_NoContainer extends FluidTrait { }
}
