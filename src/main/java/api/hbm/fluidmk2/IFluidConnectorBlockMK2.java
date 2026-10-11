package api.hbm.fluidmk2;

import api.hbm.fluid.IFluidType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** For blocks with no TE that still want a pipe to visually connect over. Rendering only */
public interface IFluidConnectorBlockMK2 {

	/** dir is the connected face, pointing outward from this block */
	public boolean canConnect(IFluidType type, IBlockAccess world, BlockPos pos, EnumFacing dir);
}
