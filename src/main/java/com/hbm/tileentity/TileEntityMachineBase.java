package com.hbm.tileentity;

import com.hbm.capability.CapabilityHandler;
import com.hbm.capability.HbmArrayItemHandler;
import com.hbm.capability.ICapabilityHost;
import com.hbm.util.fauxpointtwelve.DirPos;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandler;

/**
 * The machine common base: slot array + player-facing container behavior + a per-tick callback.
 *
 * Slots remain one `ItemStack[]` (old code reads and writes it directly all over); the capability face and container face both hug this same data:
 * exposed outward via IItemHandler for automation and third parties, while inward the container/slot layer keeps the old IInventory contract
 * — until the container layer moves wholesale onto capabilities, both sides have to stay alive.
 */
public abstract class TileEntityMachineBase extends TileEntityLoadedBase implements IInventory, ICapabilityHost, ITickable {

	public ItemStack slots[];

	private String customName;

	/** Capability view over the slots array, built on first use */
	private IItemHandler itemHandler;

	public TileEntityMachineBase(int slotCount) {
		slots = new ItemStack[slotCount];
	}

	@Override
	public int getSizeInventory() {
		return slots.length;
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : slots) {
			if(stack != null && !stack.isEmpty()) return false;
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return slots[i];
	}

	@Override
	public ItemStack removeStackFromSlot(int i) {
		if(slots[i] == null) return ItemStack.EMPTY;
		ItemStack stack = slots[i];
		slots[i] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		slots[i] = stack;
		if(stack != null && stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}
	}

	/** Subclasses name this machine (a lang key) for the container title */
	public abstract String getName();

	@Override
	public boolean hasCustomName() {
		return customName != null && !customName.isEmpty();
	}

	public void setCustomName(String name) {
		this.customName = name;
		markDirty();
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this) {
			return false;
		}
		return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 128;
	}

	@Override public void openInventory(EntityPlayer player) { }
	@Override public void closeInventory(EntityPlayer player) { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if(slots[slot] != null) {

			if(slots[slot].getCount() <= amount) {
				ItemStack stack = slots[slot];
				slots[slot] = null;
				return stack;
			}

			ItemStack stack = slots[slot].splitStack(amount);
			if(slots[slot].getCount() == 0) {
				slots[slot] = null;
			}

			return stack;
		}
		return ItemStack.EMPTY;
	}

	@Override public int getField(int id) { return 0; }
	@Override public void setField(int id, int value) { }
	@Override public int getFieldCount() { return 0; }

	@Override
	public void clear() {
		for(int i = 0; i < slots.length; i++) slots[i] = null;
	}

	/** Capability face: inventory view over the slots array; per §6.6's single-source-of-truth rule, both sides work the same data */
	@Override
	public IItemHandler getItemHandler() {
		if(itemHandler == null) {
			itemHandler = new HbmArrayItemHandler(this.slots, this::markDirty);
		}
		return itemHandler;
	}

	//the capability exit goes through the proxy; subclasses override only what they actually hold (slots/tanks/storage)
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(super.hasCapability(capability, facing)) return true;
		return CapabilityHandler.has(this, capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		T own = super.getCapability(capability, facing);
		if(own != null) return own;
		return CapabilityHandler.get(this, capability, facing);
	}

	public int getGaugeScaled(int i, FluidTank tank) {
		return tank.getFluidAmount() * i / tank.getCapacity();
	}

	/** Per-tick callback; subclasses implement as needed */
	public abstract void updateEntity();

	@Override
	public void update() {
		if(this.isLoaded) updateEntity();
	}

	@Deprecated
	public void handleButtonPacket(int value, int meta) { }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length) {
				slots[b0] = new ItemStack(nbt1);
			}
		}

		customName = nbt.getString("name");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < slots.length; i++) {
			if(slots[i] != null && !slots[i].isEmpty()) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);

		if(customName != null) {
			nbt.setString("name", customName);
		}

		return nbt;
	}

	/** Manually shake a face's redstone connection: for multiblocks changing facing or connection ports */
	public void updateRedstoneConnection(DirPos pos) {

		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		EnumFacing dir = pos.getDir();

		BlockPos bPos = new BlockPos(x, y, z);
		IBlockState state = world.getBlockState(bPos);
		Block block = state.getBlock();

		block.onNeighborChange(world, bPos, this.pos);
		block.neighborChanged(state, world, bPos, this.getBlockType(), this.pos);

		if(state.isNormalCube()) {
			BlockPos next = bPos.offset(dir);
			IBlockState state2 = world.getBlockState(next);
			Block block2 = state2.getBlock();

			if(block2.getWeakChanges(world, next)) {
				block2.onNeighborChange(world, next, this.pos);
				block2.neighborChanged(state2, world, next, this.getBlockType(), this.pos);
			}
		}
	}
}
