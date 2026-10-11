package api.hbm.energymk2;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 * For plain blocks that have no energy TE but still want cables to visually connect over.
 * Note: this ONLY affects rendering - actual energy transfer still goes through a TE or a net.
 */
public interface IEnergyConnectorBlock {

	public boolean canConnect(IBlockAccess world, BlockPos pos, EnumFacing dir);
}
