package com.hbm.items.weapon.grenade;

import com.hbm.items.ItemEnumMulti;

/**
 * The grenade shells: each shape's "stack size / pin time / bounce / throw force" all live in the enum,
 * with grenade bodies reading these tiers off the shell — data only here.
 */
public class ItemGrenadeShell extends ItemEnumMulti {

	public ItemGrenadeShell() {
		super(EnumGrenadeShell.class, true, true);
	}

	public static enum EnumGrenadeShell {
		FRAG(4, 30, 0.5D, 1D),		 // extra fragments
		STICK(4, 43, 0.25D, 1.5D),	 // thrown further
		TECH(2, 30, 0.5D, 1D),		 // the EMP/plasma shells with electronics inside
		NUKE(1, 43, 0.25D, 1.5D);	 // the high-yield nuke grenade's shell

		private int stackLimit;
		private int drawDuration;
		private double bounceModifier = 1D;
		private double yeetForce = 1D;

		private EnumGrenadeShell(int stackLimit, int drawDuration, double bounceModifier, double yeetForce) {
			this.stackLimit = stackLimit;
			this.drawDuration = drawDuration;
			this.bounceModifier = bounceModifier;
			this.yeetForce = yeetForce;
		}

		public int getStackLimit() { return stackLimit; }
		public int getDrawDuration() { return drawDuration; }
		public double getBounce() { return bounceModifier; }
		public double getYeetForce() { return yeetForce; }
	}
}
