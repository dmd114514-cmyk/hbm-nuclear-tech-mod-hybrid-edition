package api.hbm.energymk2;

import net.minecraft.util.EnumFacing;

/** Anything that can be wired up: machines, casings, even connectors that are just a block with no TE. */
public interface IEnergyConnectorMK2 {

	/**
	 * dir is THIS block's face, not the facing of the block doing the checking.
	 */
	public default boolean canConnect(EnumFacing dir) {
		return dir != null;
	}
}
