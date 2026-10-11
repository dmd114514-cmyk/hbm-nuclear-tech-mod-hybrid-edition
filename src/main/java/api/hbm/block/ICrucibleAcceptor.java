package api.hbm.block;

import api.hbm.material.MaterialStack;

import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/** Things that catch poured/flowing metal: crucibles, spouts, and entities you can load from the side */
public interface ICrucibleAcceptor {

	//POUR: metal leaves a spout/crucible, usually dropping; the extra coordinates give a more precise landing spot, and top-filling things like the big crucible take this path too
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack);
	public MaterialStack<?> pour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack);

	//FLOW: safe transfers through spouts and the like, usually cell to cell and mostly horizontal; things like minecarts that take material from the side count too
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack);
	public MaterialStack<?> flow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack);

	/**
	 * Where a pour lands: one hitscan along the given line, and it only counts as a target when it hits a block square on and that block can take material.
	 * When hitHolder is non-null, the hit position is written into it even if no taker was found - that is the spot splashing needs.
	 */
	public static ICrucibleAcceptor pouringTarget(World world, Vec3d start, Vec3d end, RayTraceResult[] hitHolder) {

		RayTraceResult hit = world.rayTraceBlocks(start, end, true, true, true);

		if(hitHolder != null) {
			hitHolder[0] = hit;
		}

		if(hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
			return null;
		}

		Block b = world.getBlockState(hit.getBlockPos()).getBlock();

		if(!(b instanceof ICrucibleAcceptor)) {
			return null;
		}

		return (ICrucibleAcceptor) b;
	}
}
