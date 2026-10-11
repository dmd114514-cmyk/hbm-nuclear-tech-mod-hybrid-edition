package com.hbm.entity.mob;

import net.minecraft.world.World;

/**
 * The sifter eel: a six-form fish, form deciding its texture (RenderEntityMulti picks by getEnum()).
 *
 * One old wart kept honestly: type is a plain field, never entered the data watcher,
 * so the client picks its own form from its own randomness, possibly differing from the server's — each fish's pattern rolls independently.
 * Copied, not a typo; syncing type across sides is a gameplay change, so it's logged and left alone.
 */
public class EntitySifterEel extends EntityFish implements IEntityEnumMulti {

	public enum SifterEel {
		PLAIN,
		FAST,
		EXOTIC,
		PHASED,
		ELEMENTAL,
		PERFECT,
	}

	public SifterEel type;

	public EntitySifterEel(World world) {
		super(world, 1.8, 8.0F);

		type = SifterEel.values()[world.rand.nextInt(SifterEel.values().length)];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getEnum() {
		return type;
	}
}
