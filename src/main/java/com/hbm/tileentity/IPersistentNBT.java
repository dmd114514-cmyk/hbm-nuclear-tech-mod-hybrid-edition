package com.hbm.tileentity;

import java.util.ArrayList;

import com.hbm.tileentity.machine.TileEntityDummy;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implemented by tile entities that must carry machine contents (recipe progress, inventory and such) out when the block breaks.
 *
 * The caller grabs an NBT at onBlockDestroyed / harvest-style moments and stuffs it into the drop,
 * then restoreData reads it back on placement, so multiblocks don't lose their guts the moment they're dismantled.
 */
public interface IPersistentNBT {

	String NBT_PERSISTENT_KEY = "persistent";

	void writeNBT(NBTTagCompound nbt);

	void readNBT(NBTTagCompound nbt);

	/** By default drops a block carrying its own data; drop counts are deliberately not handled here */
	default ArrayList<ItemStack> getDrops(Block b) {
		ArrayList<ItemStack> list = new ArrayList<>();
		ItemStack stack = new ItemStack(b);
		NBTTagCompound data = new NBTTagCompound();
		writeNBT(data);
		if(!data.hasNoTags())
			stack.setTagCompound(data);
		list.add(stack);
		return list;
	}

	/**
	 * Breaking a dummy cell should drop the core's data, so resolve the coordinate to the core first.
	 *
	 * The old version called util.CompatExternal.getCoreFromPos for this; that utility hasn't migrated,
	 * so the same semantics are implemented inline (dummy → the core it points at, everything else returned as-is),
	 * rather than waiting on another package for one three-way call.
	 */
	static TileEntity resolveCore(World world, BlockPos pos) {

		TileEntity tile = world.getTileEntity(pos);

		if(tile instanceof TileEntityDummy) {
			TileEntityDummy dummy = (TileEntityDummy) tile;
			return world.getTileEntity(new BlockPos(dummy.targetX, dummy.targetY, dummy.targetZ));
		}

		return tile;
	}

	static ArrayList<ItemStack> getDrops(World world, BlockPos pos, Block b) {

		TileEntity tile = resolveCore(world, pos);

		if(tile instanceof IPersistentNBT) {
			return ((IPersistentNBT) tile).getDrops(b);
		}

		return new ArrayList<>();
	}

	static void restoreData(World world, BlockPos pos, ItemStack stack) {
		try {
			if(!stack.hasTagCompound()) return;
			TileEntity tile = world.getTileEntity(pos);
			if(!(tile instanceof IPersistentNBT)) return;
			IPersistentNBT persistent = (IPersistentNBT) tile;
			persistent.readNBT(stack.getTagCompound());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
