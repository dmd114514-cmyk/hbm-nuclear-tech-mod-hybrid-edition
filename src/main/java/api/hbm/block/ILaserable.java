package api.hbm.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** Things a laser can eat - blocks and tile entities both count */
public interface ILaserable {

	public void addEnergy(World world, int x, int y, int z, long energy, EnumFacing dir);
}
