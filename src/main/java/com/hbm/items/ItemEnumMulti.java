package com.hbm.items;

import java.util.Locale;

import com.hbm.interfaces.IOrderedEnum;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * Items using enums as subtypes — unclear whose idea this originally was, but a large share of the 459 items lean on it.
 * Meta is the enum ordinal; names/textures derive from the lowercase enum name, so the old lang and texture names need zero edits.
 *
 * 1.12.2 dropped IIcon; textures go to per-meta model JSONs — only the data face remains here.
 */
public class ItemEnumMulti extends ItemBase {

	protected Class<? extends Enum> theEnum;
	protected boolean multiName;
	protected boolean multiTexture;

	public ItemEnumMulti(Class<? extends Enum> theEnum, boolean multiName, boolean multiTexture) {
		this.setHasSubtypes(true);
		this.theEnum = theEnum;
		this.multiName = multiName;
		this.multiTexture = multiTexture;
	}

	/**
	 * SEARCH always passes: 1.12.2's isInCreativeTab returns false for items without a creative tab,
	 * silently dropping this batch of hidden subtype items, which rely on full enumeration to be found by recipes/ore dict.
	 */
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		Enum[] order = theEnum.getEnumConstants();
		if(order.length > 0 && order[0] instanceof IOrderedEnum) order = ((IOrderedEnum) order[0]).getOrder();

		for(Enum e : order) {
			list.add(new ItemStack(this, 1, e.ordinal()));
		}
	}

	/** Out-of-range meta clamps back to 0, so a corrupted meta in an old save doesn't take down the session */
	protected Enum grab(Class<? extends Enum> clazz, int index) {
		Enum[] values = clazz.getEnumConstants();
		return values[Math.abs(index % values.length)];
	}

	public int subtypeCount() {
		return theEnum.getEnumConstants().length;
	}

	/** true means each subtype has its own texture (model names carry the enum suffix); false means one shared texture told apart only by tinting */
	public boolean usesSubtypeTextures() {
		return multiTexture;
	}

	/** The subtype model/texture suffix, same lineage as 1.7.10's `getIconString() + "." + lowercase enum name` */
	public String subtypeSuffix(int meta) {
		return grab(theEnum, meta).name().toLowerCase(Locale.US);
	}

	/** Returns null for a wrong enum. It only ever served recipes; same now */
	public ItemStack stackFromEnum(int count, Enum num) {

		if(num.getClass() != this.theEnum) return null;

		return new ItemStack(this, count, num.ordinal());
	}

	public ItemStack stackFromEnum(Enum num) {
		return stackFromEnum(1, num);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		if(multiName) {
			Enum num = grab(theEnum, stack.getItemDamage());
			return super.getUnlocalizedName() + "." + num.name().toLowerCase(Locale.US);
		}

		return super.getUnlocalizedName(stack);
	}
}
