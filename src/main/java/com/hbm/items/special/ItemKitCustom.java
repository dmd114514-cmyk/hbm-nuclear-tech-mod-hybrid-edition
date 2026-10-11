package com.hbm.items.special;

import com.hbm.items.IColoredItem;
import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

/**
 * The programmable toolkit: name, description and two texture tint layers all in NBT, assembled in one create call.
 *
 * The old getRenderPasses/registerIcons/multi-pass icon picking is the model JSON's layer1/layer2 here,
 * colors via IColoredItem callbacks by layer number; the item side holds no texture references.
 */
public class ItemKitCustom extends ItemKitNBT implements IColoredItem {

	@Override
	public int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex == 1 || tintIndex == 2) {
			return getColorFromNBT(stack, tintIndex);
		}
		return 0xFFFFFF;
	}

	/**
	 * Builds a loaded toolkit. lore null means no description lines; multiple lines split on $.
	 */
	public static ItemStack create(String name, String lore, int color1, int color2, ItemStack... contents) {
		ItemStack stack = new ItemStack(ModItems.kit_custom);

		stack.setTagCompound(new NBTTagCompound());

		setColor(stack, color1, 1);
		setColor(stack, color2, 2);

		if(lore != null) {
			ItemStackUtil.addTooltipToStack(stack, lore.split("\\$"));
		}
		stack.setStackDisplayName(TextFormatting.RESET + name);
		ItemStackUtil.addStacksToNBT(stack, contents);

		return stack;
	}

	public static void setColor(ItemStack stack, int color, int index) {
		if(!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		stack.getTagCompound().setInteger("color" + index, color);
	}

	public static int getColorFromNBT(ItemStack stack, int index) {
		if(!stack.hasTagCompound()) {
			return 0;
		}
		return stack.getTagCompound().getInteger("color" + index);
	}
}
