package com.hbm.entity.train;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

/**
 * The cargo layer: internal slot array, with the occupied-slot count synced to clients (rendering and the "how loaded" tooltip both read it).
 *
 * This layer keeps vanilla's {@link IInventory} — vanilla's own cargo animals (llamas and friends) are shaped the same;
 * §16's IItemHandler is the externally exposed capability face — whoever wants a capability wraps it outside; no second storage inside the entity.
 */
public abstract class EntityRailCarCargo extends EntityRailCarBase implements IInventory {

	private static final DataParameter<Integer> DW_OCCUPIED = EntityDataManager.createKey(EntityRailCarCargo.class, DataSerializers.VARINT);

	protected String entityName;
	protected ItemStack[] slots = new ItemStack[this.getSizeInventory()];

	public EntityRailCarCargo(World world) {
		super(world);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.getDataManager().register(DW_OCCUPIED, Integer.valueOf(0));
	}

	public int countOccupiedSlots() {
		int slots = 0;

		for(int i = 0; i < this.getSizeInventory(); i++) {
			if(!this.getStackInSlot(i).isEmpty()) slots++;
		}

		return slots;
	}

	/** Occupied slots. The client's slot array is empty; "how loaded" is readable only from this synced value */
	public int getOccupiedSlotCount() {
		return this.getDataManager().get(DW_OCCUPIED).intValue();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		ItemStack stack = this.slots[slot];
		return stack == null ? ItemStack.EMPTY : stack;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack stack = this.slots[slot];

		if(stack == null || stack.isEmpty()) {
			return ItemStack.EMPTY;
		}

		if(stack.getCount() <= amount) {
			this.slots[slot] = null;
			return stack;
		}

		ItemStack split = stack.splitStack(amount);

		if(stack.getCount() == 0) {
			this.slots[slot] = null;
		}

		return split;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		ItemStack stack = this.slots[slot];
		this.slots[slot] = null;
		return stack == null ? ItemStack.EMPTY : stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.slots[slot] = stack;

		if(!stack.isEmpty() && stack.getCount() > this.getInventoryStackLimit()) {
			stack.setCount(this.getInventoryStackLimit());
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if(!this.world.isRemote) this.getDataManager().set(DW_OCCUPIED, Integer.valueOf(this.countOccupiedSlots()));
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isEmpty() {
		return this.countOccupiedSlots() == 0;
	}

	@Override
	public void clear() {
		this.slots = new ItemStack[this.getSizeInventory()];
	}

	@Override
	public void markDirty() { }

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return this.isDead ? false : player.getDistanceSq(this) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) { }

	@Override
	public void closeInventory(EntityPlayer player) { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) { }

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for(int i = 0; i < this.slots.length; ++i) {
			if(this.slots[i] != null && !this.slots[i].isEmpty()) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.slots[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbt.setTag("Items", nbttaglist);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		this.slots = new ItemStack[this.getSizeInventory()];

		for(int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if(j >= 0 && j < this.slots.length) {
				this.slots[j] = new ItemStack(nbttagcompound1);
			}
		}

		this.getDataManager().set(DW_OCCUPIED, Integer.valueOf(this.countOccupiedSlots()));
	}

	@Override
	public boolean hasCustomName() {
		return this.entityName != null;
	}

	public String getEntityName() {
		return this.entityName;
	}

	public void setEntityName(String name) {
		this.entityName = name;
	}

	@Override
	public String getName() {
		return this.entityName != null ? this.entityName : super.getName();
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.hasCustomName() ? new net.minecraft.util.text.TextComponentString(this.entityName) : super.getDisplayName();
	}
}
