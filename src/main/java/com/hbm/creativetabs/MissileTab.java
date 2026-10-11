package com.hbm.creativetabs;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** Ammo tab: missiles and satellites live here */
public class MissileTab extends CreativeTabs {

	public MissileTab(String label) {
		super(label);
	}

	@Override
	public ItemStack getTabIconItem() {
		// The old icon was the nuclear-tipped missile; that item is still a few item batches out, holding the spot for now
		return new ItemStack(Items.IRON_PICKAXE);
	}

	/*
	 * The old version overrode displayAllReleventItems to stuff 9 hand-built sample rockets into the tab footer (the Lil Bub lineup).
	 * It wants ItemCustomMissile plus a pile of mp_* part fields; none of that has landed, so no override yet;
	 * once the parts are in, bring it back with the 1.12.2 signature: displayAllRelevantItems(NonNullList<ItemStack>)
	 * — note the misspelling in that method name got fixed ages ago; copying the old name verbatim won't compile.
	 */
}
