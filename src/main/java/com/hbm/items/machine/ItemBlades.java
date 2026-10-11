package com.hbm.items.machine;

import com.hbm.items.ItemBase;

/** The shredder blades. Durability 0 means it never wears out. */
public class ItemBlades extends ItemBase {

	public ItemBlades(int dura) {
		this.setMaxDamage(dura);
	}
}
