package api.hbm.energymk2;

/**
 * Cables. The node graph is retired; a cable is now just a neighbor that forwards:
 * Every tick it does one neighbor hop per face (see the two bridge methods below); long-distance routing and caching are the cable's own problem.
 */
public interface IEnergyConductorMK2 extends IEnergyConnectorMK2 {

	/**
	 * Cable-side bridge: when the other end is an FE device, draw HE and hand it to the host. The host is supplied by the caller -
	 * a cable holds no power itself, so the net it sits in has to point it at a machine that can take power.
	 */
	public default long bridgeFromFE(IEnergyHandlerMK2 host, net.minecraft.world.World world, int x, int y, int z, net.minecraft.util.EnumFacing dir) {
		return EnergyConversion.pullFEtoHE(host, world, x, y, z, dir);
	}

	/** The other half of the cable-side bridge: convert the host's power to FE at the configured ratio and push it to the neighbor */
	public default long bridgeToFE(IEnergyProviderMK2 host, net.minecraft.world.World world, int x, int y, int z, net.minecraft.util.EnumFacing dir) {
		return EnergyConversion.pushHEtoFE(host, world, x, y, z, dir);
	}
}
