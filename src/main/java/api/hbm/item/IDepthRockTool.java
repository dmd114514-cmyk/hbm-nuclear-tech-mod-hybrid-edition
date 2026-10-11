package api.hbm.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/** Tools that can dig deep rock: all three arguments stay, since restricting by dimension/block/position is exactly what they are for */
public interface IDepthRockTool {

	public boolean canBreakRock(World world, EntityPlayer player, ItemStack tool, Block block, int x, int y, int z);
}
