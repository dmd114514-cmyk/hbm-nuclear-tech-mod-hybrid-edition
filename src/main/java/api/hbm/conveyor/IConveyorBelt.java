package api.hbm.conveyor;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IConveyorBelt {

	/** true means the thing stays on the belt, false means it drops off */
	public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos);
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed);
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos);
}
