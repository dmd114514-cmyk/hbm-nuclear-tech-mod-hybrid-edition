package api.hbm.conveyor;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/** Blocks an item on the belt can enter (hopper mouths, unloaders and such) */
public interface IEnterableBlock {

	/** On true, IConveyorItem calls onEnter itself and removes itself */
	public boolean canItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity);
	public void onItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity);

	public boolean canPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity);
	public void onPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity);
}
