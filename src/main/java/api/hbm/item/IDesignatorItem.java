package api.hbm.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Target designator: can it fire, and where */
public interface IDesignatorItem {

	/** Whether the target is valid, judged from the pad's own position; world is there to restrict the dimension or fetch an entity */
	public boolean isReady(World world, ItemStack stack, int x, int y, int z);

	/** Gives the target coordinates when ready; this version uses the immutable Vec3d instead of Vec3 */
	public Vec3d getCoords(World world, ItemStack stack, int x, int y, int z);
}
