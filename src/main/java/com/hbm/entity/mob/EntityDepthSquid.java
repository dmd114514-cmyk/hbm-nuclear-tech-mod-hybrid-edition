package com.hbm.entity.mob;

import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * The deep-sea squid: seven color forms, form deciding texture (RenderEntityMulti picks by getEnum()).
 *
 * The old block's purpose was re-adding limb swing — vanilla squid's own {@code moveEntityWithHeading} bypasses the general
 * movement path's trailing swing accumulation, so the frilled fins (ModelDepthSquid phasing off limbSwing) never move; this version's
 * {@code EntitySquid.travel} bypasses super the same way and the wart is still there, so the fix copies the old version's.
 * Three conventions: the method name {@code moveEntityWithHeading(strafe, forward)} → {@code travel(strafe, up, forward)};
 * {@code MathHelper.sqrt_double} is just {@code sqrt} here and honestly returns double, needing explicit narrowing;
 * drops untouched, still vanilla squid's table.
 *
 * Flagged honestly like its two siblings: {@code type} is a plain field, never in the data watcher; the client picks its own color form by its own randomness,
 * possibly differing from the server's. Copied from the old version; not a gameplay change.
 */
public class EntityDepthSquid extends EntitySquid implements IEntityEnumMulti {

	public enum DepthSquid {
		AQUA,
		BLACK,
		ORANGE,
		OURPLE,
		RED,
		SILVER,
		VICIOUS,
	}

	public DepthSquid type;

	public EntityDepthSquid(World world) {
		super(world);

		type = DepthSquid.values()[world.rand.nextInt(DepthSquid.values().length)];
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enum getEnum() {
		return type;
	}

	@Override
	public void travel(float strafe, float vertical, float forward) {
		super.travel(strafe, vertical, forward);

		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d0 = this.posX - this.prevPosX;
		double d1 = this.posZ - this.prevPosZ;
		float f6 = (float) MathHelper.sqrt(d0 * d0 + d1 * d1) * 4.0F;

		if(f6 > 1.0F) {
			f6 = 1.0F;
		}

		this.limbSwingAmount += (f6 - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;
	}
}
