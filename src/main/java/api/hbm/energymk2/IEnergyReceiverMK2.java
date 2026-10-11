package api.hbm.energymk2;

import api.hbm.net.DirPos;
import api.hbm.tile.TileAccessCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** Implemented by machines that take power. */
public interface IEnergyReceiverMK2 extends IEnergyHandlerMK2 {

	public default long transferPower(long power) {
		if(power + this.getPower() <= this.getMaxPower()) {
			this.setPower(power + this.getPower());
			return 0;
		}
		long capacity = this.getMaxPower() - this.getPower();
		long overshoot = power - capacity;
		this.setPower(this.getMaxPower());
		return overshoot;
	}

	public default long getReceiverSpeed() {
		return this.getMaxPower();
	}

	/** Whether powering the neighbor directly is allowed (bypassing the net); machines wired straight to a cable rely on it */
	public default boolean allowDirectProvision() { return true; }

	public default void trySubscribe(World world, DirPos pos) { trySubscribe(world, pos.getX(), pos.getY(), pos.getZ(), pos.getDir()); }

	/**
	 * Called once per direction each tick: treat that side's neighbor as a source and take a sip.
	 * Power between our own cables/machines comes in through the provider's tryProvide; this only handles third-party FE devices.
	 */
	public default long trySubscribe(World world, int x, int y, int z, EnumFacing dir) {
		return EnergyConversion.pullFEtoHE(this, world, x, y, z, dir);
	}

	public default long trySubscribe(World world, net.minecraft.util.math.BlockPos pos, EnumFacing dir) {
		return this.trySubscribe(world, pos.getX(), pos.getY(), pos.getZ(), dir);
	}

	/** Supply priority when power is scarce; higher numbers get fed first */
	public enum ConnectionPriority {
		LOWEST,
		LOW,
		NORMAL,
		HIGH,
		HIGHEST
	}

	public default ConnectionPriority getPriority() {
		return ConnectionPriority.NORMAL;
	}
}
