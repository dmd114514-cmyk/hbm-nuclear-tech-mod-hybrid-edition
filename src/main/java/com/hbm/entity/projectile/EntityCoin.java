package com.hbm.entity.projectile;

import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

/**
 * The thrown coin: vanishes on hitting a block, spinning until it lands.
 *
 * The old setPosition override didn't come over: it hand-computed the collision box from 1.7's yOffset/ySize fields,
 * neither of which remains on Entity (yOffset survives only as the getYOffset() render offset).
 * So the "lifted half a block" look is kept by overriding getYOffset(), and the collision box is this version's own setPosition's job.
 */
public class EntityCoin extends EntityThrowableInterp {

	public EntityCoin(World world) {
		super(world);
		this.setSize(1F, 1F);
	}

	@Override
	public double getYOffset() {
		return 0.5D;
	}

	@Override
	protected void onImpact(RayTraceResult mop) {
		if(mop.typeOfHit == RayTraceResult.Type.BLOCK) this.setDead();
	}

	@Override
	protected float getAirDrag() {
		return 1F;
	}

	@Override
	public double getGravityVelocity() {
		return 0.02D;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canBeAttackedWithItem() {
		return true;
	}
}
