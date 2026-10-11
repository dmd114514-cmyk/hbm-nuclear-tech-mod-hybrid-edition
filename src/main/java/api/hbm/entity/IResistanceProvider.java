package api.hbm.entity;

import net.minecraft.util.DamageSource;

/**
 * An entity reporting how much damage reduction/armour it has left.
 *
 * Damage resistance normally goes through the equipment table (`DamageResistanceHandler`), but some mobs take damage from their own
 * internal state - a swarm's shell gets worn down by hits, for instance. The equipment table cannot express that, so such an entity implements this,
 * and the reduction maths asks it first.
 */
public interface IResistanceProvider {

	/** Returns the {reduction, resistance} actually applied to this hit; the two pierce arguments are the hit's own penetration */
	public float[] getCurrentDTDR(DamageSource damage, float amount, float pierceDT, float pierce);

	/** Tell the entity once the hit is done, so it can wear down its own shell/shield */
	public void onDamageDealt(DamageSource damage, float amount);
}
