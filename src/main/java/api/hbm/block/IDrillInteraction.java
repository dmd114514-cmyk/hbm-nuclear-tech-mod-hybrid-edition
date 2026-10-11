package api.hbm.block;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** The drill-to-block interaction contract; the cluster ore vein stuff still uses it, but new code should not depend on it */
@Deprecated
public interface IDrillInteraction {

	/**
	 * Whether this cell can be mined out (mined out means the whole cell disappears and the cluster vein drops nothing).
	 * Put some randomness in the implementation, otherwise a vein will sit there spitting ore forever.
	 */
	public boolean canBreak(World world, int x, int y, int z, int meta, IMiningDrill drill);

	/** What is usually taken when the block is not mined out: laser drills drop it, mechanical drills shove it into their own inventory */
	public ItemStack extractResource(World world, int x, int y, int z, int meta, IMiningDrill drill);

	/** The hardness to use for the calculation (instead of the block's own) */
	public float getRelativeHardness(World world, int x, int y, int z, int meta, IMiningDrill drill);
}
