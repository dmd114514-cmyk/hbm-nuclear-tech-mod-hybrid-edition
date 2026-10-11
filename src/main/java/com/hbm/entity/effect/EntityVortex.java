package com.hbm.entity.effect;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * The vortex: the black hole's self-tightening variant, shrinking its size by shrinkRate each tick until it hits zero and vanishes.
 *
 * Two deltas from the parent, copied: shrink first, then call super.onUpdate() (so the final tick is "shrunk away, stops chewing blocks").
 */
public class EntityVortex extends EntityBlackHole {

	public float shrinkRate = 0.0025F;

	public EntityVortex(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	public EntityVortex(World world, float size) {
		super(world);
		this.setSizeValue(size);
	}

	public EntityVortex setShrinkRate(float shrinkRate) {
		this.shrinkRate = shrinkRate;
		return this;
	}

	@Override
	public void onUpdate() {

		this.setSizeValue(this.getSize() - shrinkRate);
		if(this.getSize() <= 0) {
			this.setDead();
			return;
		}

		super.onUpdate();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		this.shrinkRate = nbt.getFloat("shrinkRate");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("shrinkRate", this.shrinkRate);
	}
}
