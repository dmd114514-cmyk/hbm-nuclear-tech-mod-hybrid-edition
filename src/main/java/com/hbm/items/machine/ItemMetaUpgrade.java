package com.hbm.items.machine;

import com.hbm.items.IMultiModelItem;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

/**
 * The tiered machine upgrades: one tier per meta, the tier riding the name suffix ({@code upgrade_stack_1} through {@code upgrade_stack_3}).
 *
 * The old version picked textures per tier via registerIcons; one model per tier here — the first tier uses the base-named file — the repo's
 * {@code upgrade_stack.png}, whose model references the {@code upgrade_stack_1} image directly; later tiers step into {@code _2}, {@code _3}.
 */
public class ItemMetaUpgrade extends ItemMachineUpgrade implements IMultiModelItem {

	protected int levels;

	public ItemMetaUpgrade(int levels) {
		super();
		this.setup(levels);
	}

	public ItemMetaUpgrade(UpgradeType type, int levels) {
		super(type);
		this.setup(levels);
	}

	private void setup(int levels) {
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.levels = levels;
	}

	@Override
	public String[] subtypeModels() {

		String name = this.getRegistryName().getResourcePath();
		String[] names = new String[this.levels];

		for(int i = 0; i < names.length; i++) names[i] = i == 0 ? name : name + "_" + (i + 1);

		return names;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {

		if(tab != CreativeTabs.SEARCH && !isInCreativeTab(tab)) return;

		for(int i = 0; i < this.levels; i++) list.add(new ItemStack(this, 1, i));
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "_" + (stack.getItemDamage() + 1);
	}
}
