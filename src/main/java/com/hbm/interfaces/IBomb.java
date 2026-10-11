package com.hbm.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Things that can be "detonated". The return code only matters server-side — most bombs keep their inventory there,
 * whatever the client receives, ignore it.
 *
 * Call sites are usually inside block neighbor updates, so implementations must check world.isRemote first.
 */
public interface IBomb {

	public BombReturnCode explode(World world, BlockPos pos);

	enum BombReturnCode {
		UNDEFINED(false, ""),                                     // placeholder for clients that don't handle return codes
		DETONATED(true, "bomb.detonated"),                        // bomb went off successfully
		TRIGGERED(true, "bomb.triggered"),                        // triggered another device successfully
		LAUNCHED(true, "bomb.launched"),                          // missile launched successfully
		ERROR_MISSING_COMPONENT(false, "bomb.missingComponent"),  // missing a component
		ERROR_INCOMPATIBLE(false, "bomb.incompatible"),           // target incompatible (like a locked blast door)
		ERROR_NO_BOMB(false, "bomb.nobomb");                      // generic error when the target is air

		private final String unloc;
		private final boolean success;

		BombReturnCode(boolean success, String unloc) {
			this.unloc = unloc;
			this.success = success;
		}

		public String getUnlocalizedMessage() {
			return this.unloc;
		}

		public boolean wasSuccessful() {
			return this.success;
		}
	}
}
