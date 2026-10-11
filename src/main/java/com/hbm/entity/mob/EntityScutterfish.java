package com.hbm.entity.mob;

import com.hbm.lib.RefStrings;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

/**
 * The scutterfish: the fish base's first member.
 *
 * The old version returned ModItems.scuttertail straight from `getDropItem()`; EntityWaterMob has no such door here,
 * drops go through loot tables, so this points at its own table (entities/entity_scutterfish).
 * The table is empty for now: the item side belongs to A5 and scuttertail hasn't reached main; and per §15,
 * a legal empty table beats hard-coded drops — resource packs and LootTweaker can add to it later.
 * Once A5 lands that item, one pool in this table finishes the job; the entity side never changes.
 */
public class EntityScutterfish extends EntityFish {

	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(RefStrings.MODID, "entities/entity_scutterfish");

	public EntityScutterfish(World world) {
		super(world, 1.5, 6.0F);
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LOOT_TABLE;
	}
}
