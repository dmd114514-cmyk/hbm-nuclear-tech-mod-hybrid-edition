package com.hbm.tileentity;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ICopiable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** The common face of machines with a "filter grid": filters can be copied/pasted and toggled from the GUI */
public interface IControlReceiverFilter extends IControlReceiver, ICopiable {

	void nextMode(int i);

	@Override
	default void receiveControl(NBTTagCompound data) {
		if(data.hasKey("slot")) {
			setFilterContents(data);
		}
	}

	/** Implementers must be a tile entity + IInventory */
	default void setFilterContents(NBTTagCompound nbt) {
		TileEntity tile = (TileEntity) this;
		IInventory inv = (IInventory) this;

		int slot = nbt.getInteger("slot");
		ItemStack item = new ItemStack(nbt.getCompoundTag("stack"));
		if(item.isEmpty()) return;

		item.setCount(1);
		inv.setInventorySlotContents(slot, item);
		nextMode(slot);
		tile.getWorld().markChunkDirty(tile.getPos(), tile);
	}

	/** For the copy tool: the filter grid's start/end (start inclusive, end exclusive) */
	int[] getFilterSlots();

	@Override
	default NBTTagCompound getSettings(World world, BlockPos pos) {
		IInventory inv = (IInventory) this;
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList tags = new NBTTagList();
		int count = 0;

		for(int i = getFilterSlots()[0]; i < getFilterSlots()[1]; i++) {
			ItemStack stack = inv.getStackInSlot(i);

			if(stack != null && !stack.isEmpty()) {
				NBTTagCompound slotNBT = new NBTTagCompound();
				slotNBT.setByte("slot", (byte) count);
				stack.writeToNBT(slotNBT);
				tags.appendTag(slotNBT);
			}
			count++;
		}
		nbt.setTag("items", tags);

		return nbt;
	}

	@Override
	default void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, BlockPos pos) {
		TileEntity tile = (TileEntity) this;
		IInventory inv = (IInventory) this;

		NBTTagList items = nbt.getTagList("items", 10);
		int listSize = items.tagCount();

		if(listSize > 0) {
			int count = 0;
			int from = getFilterSlots()[0];
			int to = getFilterSlots()[1];

			for(int i = from; i < to; i++) {
				if(i < listSize) {
					NBTTagCompound slotNBT = items.getCompoundTagAt(count);
					byte slot = slotNBT.getByte("slot");
					ItemStack loadedStack = new ItemStack(slotNBT);

					//filter data may come from a router, which carries the modes marker
					boolean router = nbt.hasKey("modes") && slot > index * 5 && slot < index * 5;

					if(!loadedStack.isEmpty() && index < listSize && (slot < to || router)) {
						inv.setInventorySlotContents(slot + from, loadedStack);
						nextMode(slot);
						tile.getWorld().markChunkDirty(tile.getPos(), tile);
					}
				}
				count++;
			}
		}
	}

	@Override
	default String[] infoForDisplay(World world, BlockPos pos) {
		return new String[] { "copytool.filter" };
	}
}
