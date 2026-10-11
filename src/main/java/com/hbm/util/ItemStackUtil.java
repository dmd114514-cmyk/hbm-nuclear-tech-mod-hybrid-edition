package com.hbm.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

/** ItemStack odd jobs. Avoid newing things here on the hot path. */
public final class ItemStackUtil {

	private ItemStackUtil() { }

	/** Item by "modid:name"; null when missing */
	public static Item getItem(String name) {
		if(name == null || name.isEmpty()) return null;
		ResourceLocation rl = new ResourceLocation(name);
		return ForgeRegistries.ITEMS.getValue(rl);
	}

	/** Reverse-lookup an item's registry name */
	public static String getName(Item item) {
		ResourceLocation rl = ForgeRegistries.ITEMS.getKey(item);
		return rl == null ? null : rl.toString();
	}

	/** All ore dict names on an item; empty array when none (never null — call sites would get ugly) */
	public static String[] getOreDictNames(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return new String[0];
		int[] ids = OreDictionary.getOreIDs(stack);
		String[] names = new String[ids.length];
		for(int i = 0; i < ids.length; i++) names[i] = OreDictionary.getOreName(ids[i]);
		return names;
	}

	/*Stuffing NBT written as JSON text into an item; illegal NBT skips silently — recipe parsing must not explode over it */
	public static void addNBTFromString(ItemStack stack, String nbt) {
		if(stack == null || nbt == null || nbt.isEmpty()) return;
		try {
			NBTTagCompound tag = JsonToNBT.getTagFromJson(nbt);
			stack.setTagCompound(tag);
		} catch(Exception ex) {
			// a recipe author's NBT typo shouldn't fail the whole world load
		}
	}

	/*
	 * The unified subtype enumeration entry.
	 *
	 * 1.12.2's Item.getSubItems(tab, list) gets filtered by each subtype's own isInCreativeTab line,
	 * so enumerating with CreativeTabs.SEARCH **silently returns empty** for items **without a creative tab** —
	 * while 1.7.10's getSubItems(Item, tab, List) was unconditional, and ore dict expansion, turret ammo lists,
	 * manual rendering all want exactly the "tab-independent full enumeration".
	 * So the whole project goes through here; nobody news up their own NonNullList to call vanilla.
	 *
	 * The companion convention (recorded in docs/1122-api-deltas.md): HBM items' getSubItems overrides all read
	 * "tab == CreativeTabs.SEARCH means add every subtype", and enumerable items must have a creative tab.
	 */

	/** Enumerates one item's full subtype set */
	/*Same item means compatible: id + meta + NBT all matching (1.7.10's check is these two lines in this version) */
	public static boolean areStacksCompatible(ItemStack a, ItemStack b) {
		if(a == null || b == null || a.isEmpty() || b.isEmpty()) return false;
		return a.isItemEqual(b) && ItemStack.areItemStackTagsEqual(a, b);
	}

	public static NonNullList<ItemStack> getSubItems(Item item) {
		NonNullList<ItemStack> out = NonNullList.create();
		item.getSubItems(CreativeTabs.SEARCH, out);

		if(out.isEmpty()) {
			// An empty enumeration almost always means "forgot the creative tab"; better to complain than let callers accept an empty list as normal
			LoggingUtil.warn("1.12.2 reads a single file per domain, `assets/<domain>/lang/<language code>.lang` (verified against the constants in `Locale`", getName(item));
			out.add(new ItemStack(item));
		}
		return out;
	}

	/** Enumerates the prototype item's own subtypes; the fallback keeps the prototype's meta so it doesn't degrade to AIR */
	public static NonNullList<ItemStack> getSubItems(ItemStack prototype) {
		NonNullList<ItemStack> out = NonNullList.create();
		if(prototype == null || prototype.isEmpty()) return out;

		Item item = prototype.getItem();
		item.getSubItems(CreativeTabs.SEARCH, out);

		if(out.isEmpty()) {
			LoggingUtil.warn("1.12.2 reads a single file per domain, `assets/<domain>/lang/<language code>.lang` (verified against the constants in `Locale`", getName(item));
			ItemStack fallback = prototype.copy();
			fallback.setCount(1);
			out.add(fallback);
		}
		return out;
	}

	/*
	 * Container items (toolboxes, drums, recipe displays) write a pile of items into their own NBT,
	 * using the old "items" tag list + one slot byte per cell format, kept verbatim,
	 * or every old save's crate contents would read out empty.
	 */

	public static void addStacksToNBT(ItemStack stack, ItemStack... stacks) {

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		NBTTagList tags = new NBTTagList();

		for(int i = 0; i < stacks.length; i++) {
			if(stacks[i] != null && !stacks[i].isEmpty()) {
				NBTTagCompound slotNBT = new NBTTagCompound();
				slotNBT.setByte("slot", (byte) i);
				stacks[i].writeToNBT(slotNBT);
				tags.appendTag(slotNBT);
			}
		}
		stack.getTagCompound().setTag("items", tags);
	}

	/** count 0 means "read as many cells as exist"; returns null rather than an empty array when there's no NBT, per the old convention */
	public static ItemStack[] readStacksFromNBT(ItemStack stack, int count) {

		if(!stack.hasTagCompound())
			return null;

		NBTTagList list = stack.getTagCompound().getTagList("items", 10);
		if(count == 0) {
			count = list.tagCount();
		}

		ItemStack[] stacks = new ItemStack[count];

		for(int i = 0; i < count; i++) {
			NBTTagCompound slotNBT = list.getCompoundTagAt(i);
			byte slot = slotNBT.getByte("slot");
			// 1.12.2 has no loadItemStackFromNBT; the constructor is its stand-in; the difference is it hands back
			// EMPTY rather than null on failure, so emptiness checks must include isEmpty
			ItemStack loadedStack = slotNBT.hasKey("id") ? new ItemStack(slotNBT) : null;
			if(slot >= 0 && slot < stacks.length && loadedStack != null && !loadedStack.isEmpty()) {
				stacks[slot] = loadedStack;
			}
		}

		return stacks;
	}

	public static ItemStack[] readStacksFromNBT(ItemStack stack) {
		return readStacksFromNBT(stack, 0);
	}

	/**
	 * Dangerous: display overwrites wholesale, wiping the item's own name/enchantment tooltips.
	 * Only for things known to carry no display tag; existing NBT untouched.
	 */
	public static ItemStack addTooltipToStack(ItemStack stack, String... lines) {

		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());

		NBTTagCompound display = new NBTTagCompound();
		NBTTagList lore = new NBTTagList();

		for(String line : lines) {
			lore.appendTag(new NBTTagString(TextFormatting.RESET + "" + TextFormatting.GRAY + line));
		}

		display.setTag("Lore", lore);
		stack.getTagCompound().setTag("display", display);

		return stack;
	}

	/**
	 * Null-safe copy: null in, null out, sparing vanilla's copy() an NPE.
	 */
	public static ItemStack carefulCopy(ItemStack stack) {
		if(stack == null) return null;
		return stack.copy();
	}

	public static ItemStack carefulCopyWithSize(ItemStack stack, int size) {
		if(stack == null)
			return null;

		ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}

	/**
	 * Copies only the [start, end] slice into a fresh array; fields outside are null.
	 */
	public static ItemStack[] carefulCopyArrayTruncate(ItemStack[] array, int start, int end) {
		if(array == null)
			return null;

		int length = end - start + 1;
		ItemStack[] copy = new ItemStack[length];

		for(int i = 0; i < length; i++) {
			copy[i] = carefulCopy(array[start + i]);
		}

		return copy;
	}
}
