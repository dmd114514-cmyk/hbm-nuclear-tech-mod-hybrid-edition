package com.hbm.items;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 * The items' common base, absorbing 1.12.2's naming differences.
 *
 * Point one: the old code had 1,600+ `.setTextureName("hbm:xxx")` calls, and 1.12.2 has no such
 * concept (textures are decided by JSON models). An empty implementation swallows it here so the old chains don't need per-site edits.
 *
 * Point two: the parent's setters all return `Item` in 1.12.2, so chaining into the next method that only exists on the subclass
 * (like setTextureName) breaks the chain at compile time. So everything the old code strung through the middle is overridden here to
 * return ItemBase — the goal is **not breaking the chain**, not changing behavior.
 *
 * **Registry names are HbmRegistry.item(item, name)'s job**; not set here — 1.12.2's
 * setRegistryName is callable once, and both sides setting it throws outright.
 */
public class ItemBase extends Item {

	public ItemBase() {
		super();
	}

	@Override
	public ItemBase setUnlocalizedName(String name) {
		super.setUnlocalizedName(name);
		return this;
	}

	/*No texture-name concept in 1.12.2; textures ride JSON. This method stays only so old chained calls don't change */
	public ItemBase setTextureName(String name) {
		return this;
	}

	@Override
	public ItemBase setCreativeTab(CreativeTabs tab) {
		super.setCreativeTab(tab);
		return this;
	}

	@Override
	public ItemBase setMaxStackSize(int size) {
		super.setMaxStackSize(size);
		return this;
	}

	@Override
	public ItemBase setHasSubtypes(boolean subtypes) {
		super.setHasSubtypes(subtypes);
		return this;
	}

	@Override
	public ItemBase setMaxDamage(int damage) {
		super.setMaxDamage(damage);
		return this;
	}

	@Override
	public ItemBase setFull3D() {
		super.setFull3D();
		return this;
	}

	@Override
	public ItemBase setContainerItem(Item item) {
		super.setContainerItem(item);
		return this;
	}

	@Override
	public ItemBase setNoRepair() {
		super.setNoRepair();
		return this;
	}
}
