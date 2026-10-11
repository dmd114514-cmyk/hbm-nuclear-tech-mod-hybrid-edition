package api.hbm.net;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * A position with a direction. The node net uses it to check whether the spot on the other side of a face holds the same kind of node.
 * No longer extends BlockPos - 1.12.2's own BlockPos is good enough and composition beats inheritance.
 */
public class DirPos {

	public final int x;
	public final int y;
	public final int z;
	protected final EnumFacing dir;

	public DirPos(int x, int y, int z, EnumFacing dir) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dir = dir;
	}

	public DirPos(TileEntity te, EnumFacing dir) {
		this(te.getPos().getX(), te.getPos().getY(), te.getPos().getZ(), dir);
	}

	public DirPos(BlockPos pos, EnumFacing dir) {
		this(pos.getX(), pos.getY(), pos.getZ(), dir);
	}

	public int getX() { return this.x; }
	public int getY() { return this.y; }
	public int getZ() { return this.z; }
	public EnumFacing getDir() { return this.dir; }

	public BlockPos toBlockPos() { return new BlockPos(this.x, this.y, this.z); }
}
