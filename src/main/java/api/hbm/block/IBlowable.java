package api.hbm.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** Things a fan blows on every tick */
public interface IBlowable {

	public void applyFan(World world, int x, int y, int z, EnumFacing dir, int dist);
}
