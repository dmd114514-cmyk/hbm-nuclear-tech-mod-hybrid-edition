package api.hbm.energymk2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * The HE/FE conversion, and how to tell ours from a third party.
 *
 * HE is this mod's internal unit, magnitudes far past int; FE is the Forge ecosystem's. 1:1 by default,
 * the ratio is read from config by {@code com.hbm.config.EnergyConfig} into {@link #HE_PER_FE} -
 * it sits on the api side so the interface defaults can use it directly without dragging a reverse com.hbm dependency along.
 */
public final class EnergyConversion {

	/** How much HE one FE is worth (1:1 by default, config can override) */
	public static volatile double HE_PER_FE = 1.0D;

	private static final Logger LOGGER = LogManager.getLogger("hbm-energy");

	private EnergyConversion() { }

	/** Illegal ratios fall back to 1:1 and log the bad value, instead of silently moving power at the wrong rate */
	public static void setRatio(double ratio) {
		if(ratio <= 0D || Double.isNaN(ratio) || Double.isInfinite(ratio)) {
			LOGGER.warn("[energy] HE_PER_FE config value {} is invalid, reverting to 1:1", ratio);
			HE_PER_FE = 1.0D;
			return;
		}
		HE_PER_FE = ratio;
	}

	public static long heToFe(long he) {
		if(he <= 0) return 0;
		return (long) (he / HE_PER_FE);
	}

	public static long feToHe(long fe) {
		if(fe <= 0) return 0;
		return (long) (fe * HE_PER_FE);
	}

	/** Is the other end one of our own HE connectors (machine/cable/casing)? If so go over HE directly, do not round-trip through capability */
	public static boolean isHbmPeer(TileEntity te) {
		return te instanceof IEnergyConnectorMK2;
	}

	/**
	 * Fetch the energy interface the other end exposes to Forge. Our own devices always return null -
	 * they are already handled by the HE channel; going through capability again would deliver the same power twice.
	 */
	public static IEnergyStorage getForeignStorage(World world, int x, int y, int z, EnumFacing side) {
		if(world == null || side == null) return null;
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if(te == null || isHbmPeer(te)) return null;
		return te.getCapability(CapabilityEnergy.ENERGY, side);
	}

	/** Convert source's HE to FE at the ratio and push it to the other end; returns the HE actually spent */
	public static long pushHEtoFE(IEnergyProviderMK2 source, World world, int x, int y, int z, EnumFacing dir) {

		IEnergyStorage storage = getForeignStorage(world, x, y, z, dir.getOpposite());
		if(storage == null || !storage.canReceive()) return 0;

		long budget = Math.min(source.getPower(), source.getProviderSpeed());
		int offer = (int) Math.min(heToFe(budget), Integer.MAX_VALUE);
		if(offer <= 0) return 0;

		int accepted = storage.receiveEnergy(offer, true);
		if(accepted <= 0) return 0;

		long cost = Math.min(feToHe(accepted), source.getPower());
		int send = (int) Math.min(heToFe(cost), Integer.MAX_VALUE);
		if(send <= 0) return 0;

		int sent = storage.receiveEnergy(send, false);
		long spent = Math.min(feToHe(sent), source.getPower());
		if(spent <= 0) return 0;

		source.usePower(spent);
		return spent;
	}

	/** Pull power from the neighboring FE device at the ratio into sink as HE; returns the HE actually received */
	public static long pullFEtoHE(IEnergyHandlerMK2 sink, World world, int x, int y, int z, EnumFacing dir) {

		IEnergyStorage storage = getForeignStorage(world, x, y, z, dir.getOpposite());
		if(storage == null || !storage.canExtract()) return 0;

		long room = sink.getMaxPower() - sink.getPower();
		if(room <= 0) return 0;

		long speed = sink instanceof IEnergyReceiverMK2 ? ((IEnergyReceiverMK2) sink).getReceiverSpeed() : sink.getMaxPower();
		int want = (int) Math.min(heToFe(Math.min(room, speed)), Integer.MAX_VALUE);
		if(want <= 0) return 0;

		int got = storage.extractEnergy(want, true);
		long gain = Math.min(feToHe(got), room);
		int take = (int) Math.min(heToFe(gain), Integer.MAX_VALUE);
		if(take <= 0) return 0;

		int drained = storage.extractEnergy(take, false);
		long added = Math.min(feToHe(drained), room);
		if(added <= 0) return 0;

		sink.setPower(sink.getPower() + added);
		return added;
	}
}
