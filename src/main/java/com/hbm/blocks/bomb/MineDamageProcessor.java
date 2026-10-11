package com.hbm.blocks.bomb;

import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;

/**
 * This mine family's damage curve: a flat value falling off linearly with distance; the blocked (density under 1/8) take nothing.
 *
 * The old formula rode the cross entity processor family, not in the tree yet; this version's standard processor ships a
 * quadratic damage curve (a yield-3 near-center hit of about 25, well above the old mine's 10), so this only swaps the damage form,
 * keeping range and knockback on the standard processor. Delete this class wholesale once the cross family lands — armor piercing and
 * (the old `attackEntityFromNT` + `BulletConfig`) remain out of this class's reach.
 */
public class MineDamageProcessor extends EntityProcessorStandard {

	private final float fixedDamage;

	public MineDamageProcessor(float fixedDamage) {
		this.fixedDamage = fixedDamage;
	}

	@Override
	public float calculateDamage(double distanceScaled, double density, double knockback, float size) {

		if(density < 0.125D) return 0F;

		return (float)(this.fixedDamage * (1.0D - distanceScaled));
	}
}
