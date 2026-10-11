package com.hbm.items;

import net.minecraft.item.ItemStack;

/**
 * Generic machine parts. Six subtypes' texture names don't match the enum names (`PISTON_PNEUMATIC` maps to
 * `piston_pneumatic`, not `part_generic_piston_pneumatic`), so the model names are self-reported.
 */
public class ItemGenericPart extends ItemEnumMulti implements IMultiModelItem {

	public static enum EnumPartType {
		PISTON_PNEUMATIC("piston_pneumatic"),
		PISTON_HYDRAULIC("piston_hydraulic"),
		PISTON_ELECTRIC("piston_electric"),
		LDE("low_density_element"),
		HDE("heavy_duty_element"),
		GLASS_POLARIZED("glass_polarized");

		public String texName;

		private EnumPartType(String texName) {
			this.texName = texName;
		}
	}

	public ItemGenericPart() {
		super(EnumPartType.class, true, true);
	}

	@Override
	public String[] subtypeModels() {

		EnumPartType[] types = EnumPartType.values();
		String[] names = new String[types.length];

		for(int i = 0; i < types.length; i++) names[i] = types[i].texName;

		return names;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + ((EnumPartType) grab(EnumPartType.class, stack.getItemDamage())).name().toLowerCase(java.util.Locale.US);
	}
}
