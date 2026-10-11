package api.hbm.energymk2;

import api.hbm.tile.TileAccessCache;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Implemented by machines that generate power. */
public interface IEnergyProviderMK2 extends IEnergyHandlerMK2 {

	/** Draw power. The default implementation does no bounds check - callers guarantee power <= getPower() */
	public default void usePower(long power) {
		this.setPower(this.getPower() - power);
	}

	public default long getProviderSpeed() {
		return this.getMaxPower();
	}

	public default void tryProvide(World world, int x, int y, int z, EnumFacing dir) {

		TileEntity te = TileAccessCache.getTileOrCache(world, x, y, z);

		//Neighbor propagation: a cable is just one neighbor; power moves straight between this machine and the receiver/third-party FE device stuck next to it
		if(te instanceof IEnergyReceiverMK2 && te != this) {
			IEnergyReceiverMK2 rec = (IEnergyReceiverMK2) te;
			if(rec.canConnect(dir.getOpposite()) && rec.allowDirectProvision()) {
				long provides = Math.min(this.getPower(), this.getProviderSpeed());
				long receives = Math.min(rec.getMaxPower() - rec.getPower(), rec.getReceiverSpeed());
				long toTransfer = Math.min(provides, receives);
				toTransfer -= rec.transferPower(toTransfer);
				this.usePower(toTransfer);
			}
		}

		//When the other side is not one of our HE connectors but a third-party FE device, push FE at the HE<->FE ratio (generators/storage never have to pick a side)
		EnergyConversion.pushHEtoFE(this, world, x, y, z, dir);
	}

	/** Convenience overload so call sites do not have to new a BlockPos themselves */
	public default void tryProvide(World world, BlockPos pos, EnumFacing dir) {
		this.tryProvide(world, pos.getX(), pos.getY(), pos.getZ(), dir);
	}
}
