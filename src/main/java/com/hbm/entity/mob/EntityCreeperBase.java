package com.hbm.entity.mob;

import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.world.World;

/**
 * The creeper family's base, catching exactly one thing: handing the "detonate" step back to subclasses — a step this version no longer offers.
 *
 * The old version swapped explosions by overriding the hook vanilla's creeper called when its swell filled (public back then). This version's
 * {@code EntityCreeper#explode()} went private along with {@code timeSinceIgnited} and {@code fuseTime}
 * (decompile-verified), neither overridable nor readable, so the vanilla arithmetic is copied and counted here: each tick adds the current state
 * (-1 unwinds the fuse, negatives fold to 0), and 30 means time.
 *
 * On the due tick, each family's body runs first, then a {@code setDead()} — both sides covered: vanilla's whole state machine sits inside
 * an {@code isEntityAlive()} check, so without a server-side death mark its own private explode would fire a second explosion,
 * and without a client-side one an extra puff of vanilla flame appears. The setDead line in each body stays where the old version put it —
 * dying before detonating so the explosion doesn't hit back (the old comment's words: "already-dead entities getting hit play their death animation").
 * play their death animation").
 *
 * One boundary on this fix, don't push past it: the fuse can only shorten (count to due yourself), never lengthen —
 * vanilla's 30-tick wall can't come down. Longer fuses (the radiation creeper's 75 ticks) need another route:
 * either intercept {@code ExplosionEvent.Start} or, like the sibling implementation, leave {@code EntityCreeper} entirely and rewrite.
 */
public abstract class EntityCreeperBase extends EntityCreeper {

	/** Vanilla's fuse length; unreadable as a private field now, so the same number is counted locally */
	private static final int FUSE_TICKS = 30;

	/** The locally counted swell, standing in for the private field this version can't read */
	private int swellTicks;

	protected EntityCreeperBase(World world) {
		super(world);
	}

	@Override
	public void onUpdate() {

		if(this.isEntityAlive()) {
			this.swellTicks += this.getCreeperState();

			if(this.swellTicks < 0) {
				this.swellTicks = 0;
			}

			if(this.swellTicks >= FUSE_TICKS) {
				this.swellTicks = FUSE_TICKS;
				// the old bodies' leading if(!isRemote) moved up here
				if(!this.world.isRemote) {
					this.doExplode();
				}

				this.setDead();
			}
		}

		super.onUpdate();
	}

	/** Each family's own detonation body: where it lands, what it leaves, what it drops — all in this step */
	protected abstract void doExplode();
}
