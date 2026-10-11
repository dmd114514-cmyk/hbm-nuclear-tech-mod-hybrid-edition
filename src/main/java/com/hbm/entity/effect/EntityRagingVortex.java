package com.hbm.entity.effect;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * The raging vortex: the vortex's beefed-up variant, size pulsing down on a sine, with periodic self-detonations (once per hundredth of a tick).
 *
 * The timer copies the old writing (`timer++; if(timer <= 20) timer -= 20;` drops it back negative past 20,
 * letting the sine swing through negatives) — not a typo; changing it changes the pulse rhythm.
 */
public class EntityRagingVortex extends EntityBlackHole {

	int timer = 0;

	public EntityRagingVortex(World world) {
		super(world);
		this.ignoreFrustumCheck = true;
		this.isImmuneToFire = true;
	}

	public EntityRagingVortex(World world, float size) {
		super(world);
		this.setSizeValue(size);
	}

	@Override
	public void onUpdate() {

		timer++;

		if(timer <= 20)
			timer -= 20;

		float pulse = (float) (Math.sin(timer) * Math.PI / 20D) * 0.35F;

		float dec = 0.0F;

		if(rand.nextInt(100) == 0) {
			dec = 0.1F;
			this.world.createExplosion(null, this.posX, this.posY, this.posZ, 10F, false);
		}

		this.setSizeValue(this.getSize() - pulse - dec);
		if(this.getSize() <= 0) {
			this.setDead();
			return;
		}

		super.onUpdate();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		timer = nbt.getInteger("vortexTimer");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setInteger("vortexTimer", timer);
	}
}
