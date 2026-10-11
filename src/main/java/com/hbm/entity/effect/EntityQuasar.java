package com.hbm.entity.effect;

import net.minecraft.world.World;

/**
 * The quasar (digamma quasar): a black-hole subclass with a new color scheme and disk texture; behavior identical to the parent.
 *
 * The old constructors both overwrote the same size field; this goes through the parent's setSizeValue().
 * It also had an onUpdate override that only called super — pure forwarding, not ported.
 */
public class EntityQuasar extends EntityBlackHole {

	public EntityQuasar(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	public EntityQuasar(World world, float size) {
		super(world);
		this.setSizeValue(size);
	}
}
