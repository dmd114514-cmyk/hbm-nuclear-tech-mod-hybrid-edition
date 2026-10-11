package com.hbm.blocks.machine;

import com.hbm.blocks.BlockContainerBase;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityMachineSiren;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

/**
 * The siren: slot a record in and it loops; right-click opens the GUI.
 *
 * The tile entity, container and GUI have long been in the tree (TileEntityMachineSiren / ContainerMachineSiren / GUIMachineSiren),
 * missing only this block and its registry name — without a block nobody creates the entity, so this completes both together.
 */
public class MachineSiren extends BlockContainerBase {

	public MachineSiren() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityMachineSiren();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		//both hands come through; only the main hand counts, or one click opens the GUI twice
		if(hand != EnumHand.MAIN_HAND) return false;
		if(world.isRemote) return true;
		if(player.isSneaking()) return false;

		if(world.getTileEntity(pos) instanceof TileEntityMachineSiren) {
			player.openGui(MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}

		return false;
	}

	/**
	 * On break the record inside spills to the ground; inventory shouldn't evaporate with the block.
	 *
	 * The crate family is twenty lines of batched spilling; this machine holds one cell and at most one item, and vanilla's spawnAsEntity is that same thing,
	 * so the short version wins — no third copy. The old private static keepInventory was written by nobody in the whole class, a dead field, not ported.
	 */
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {

		if(!world.isRemote) {
			TileEntity te = world.getTileEntity(pos);

			if(te instanceof TileEntityMachineSiren) {
				IItemHandler handler = ((TileEntityMachineSiren) te).getItemHandler();

				for(int i = 0; i < handler.getSlots(); i++) {
					ItemStack stack = handler.extractItem(i, handler.getStackInSlot(i).getCount(), false);
					if(!stack.isEmpty()) {
						spawnAsEntity(world, pos, stack);
					}
				}
			}
		}

		super.breakBlock(world, pos, state);
	}
}
