package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.items.ItemBase;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * The stamping dies. Registered grouped by die type; machines look dies up by StampType instead of comparing items one by one —
 * the old version's way, sparing the machines a per-tick registry walk.
 */
public class ItemStamp extends ItemBase {

	protected StampType type;
	public static final HashMap<StampType, List<ItemStack>> stamps = new HashMap<>();

	public ItemStamp(int dura, StampType type) {
		this.setMaxDamage(dura);
		this.type = type;

		if(type != null) {
			this.addStampToList(this, 0, type);
		}
	}

	protected void addStampToList(Item item, int meta, StampType type) {
		List<ItemStack> list = stamps.get(type);

		if(list == null) list = new ArrayList<>();

		list.add(new ItemStack(item, 1, meta));
		stamps.put(type, list);
	}

	/*Takes an Item, not an ItemStack — a stack would crash during init; don't ask why */
	public StampType getStampType(Item item, int meta) {
		return type;
	}

	public static enum StampType {
		FLAT,
		PLATE,
		WIRE,
		CIRCUIT,
		C357,
		C44,
		C50,
		C9,
		PRINTING1,
		PRINTING2,
		PRINTING3,
		PRINTING4,
		PRINTING5,
		PRINTING6,
		PRINTING7,
		PRINTING8;
	}
}
