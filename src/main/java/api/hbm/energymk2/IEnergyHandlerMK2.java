package api.hbm.energymk2;

import api.hbm.tile.ILoadedTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Common ancestor of providers and receivers. Do not implement this directly - a conductor cannot hold power,
 * so they do not extend this interface.
 *
 * Energy is long everywhere (HBM's grid magnitudes are way past int); the int interop with the Forge ecosystem is the Capability Provider's problem.
 */
public interface IEnergyHandlerMK2 extends IEnergyConnectorMK2, ILoadedTile {

	public long getPower();
	public void setPower(long power);
	public long getMaxPower();

	public default Vec3d getDebugParticlePosMK2() {
		TileEntity te = (TileEntity) this;
		return new Vec3d(te.getPos().getX() + 0.5, te.getPos().getY() + 1, te.getPos().getZ() + 0.5);
	}
}
