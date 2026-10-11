package com.hbm.items;

import com.hbm.util.ItemStackUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Broken items. What smashed them is recorded in NBT's `itemID`/`itemMeta`, the display name composed off that item
 * (`item.broken_item.prefix`, shaped like "Broken %s").
 *
 * The old version drew it with the swallowed item's icon — `requiresMultipleRenderPasses` rendering twice, first the other item's texture, then
 * overlaying its own broken base. Item looks are model-JSON-driven here and one item can't swap textures by NBT,
 * so it draws only its broken base, identity staying wholly in NBT (the repair machine reads it to restore the original).
 */
public class BrokenItem extends Item {

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		Item broken = getBrokenItem(stack);
		if(broken == null) return super.getItemStackDisplayName(stack);

		ItemStack sta = new ItemStack(broken, 1, getBrokenMeta(stack));
		return I18nUtil.resolveKey(this.getUnlocalizedNameInefficiently(stack) + ".prefix", sta.getDisplayName());
	}

	/** NBT stores the registry-name string; null when unresolvable (item removed / invalid name) */
	public static Item getBrokenItem(ItemStack stack) {
		if(stack == null || !stack.hasTagCompound()) return null;
		return ItemStackUtil.getItem(stack.getTagCompound().getString("itemID"));
	}

	public static int getBrokenMeta(ItemStack stack) {
		if(stack == null || !stack.hasTagCompound()) return 0;
		return stack.getTagCompound().getInteger("itemMeta");
	}

	public static ItemStack make(ItemStack stack) {		return make(stack.getItem(), stack.getCount(), stack.getItemDamage()); }
	public static ItemStack make(Item item) {			return make(item, 1, 0); }
	public static ItemStack make(Item item, int meta) {	return make(item, 1, meta); }

	public static ItemStack make(Item item, int stacksize, int meta) {
		ItemStack stack = new ItemStack(ModItems.broken_item, stacksize);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("itemID", ItemStackUtil.getName(item));
		nbt.setInteger("itemMeta", meta);
		stack.setTagCompound(nbt);
		return stack;
	}
}
