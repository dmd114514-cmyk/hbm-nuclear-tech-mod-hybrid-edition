package com.hbm.tileentity.machine.storage;

import java.util.Random;

import com.hbm.capability.CapabilityHandler;
import com.hbm.capability.HbmArrayItemHandler;
import com.hbm.capability.ICapabilityHost;
import com.hbm.main.NTMSounds;
import com.hbm.sound.SoundRefs;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.machine.TileEntityLockableBase;

import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;

/**
 * The common base for crates and safes. Inventory is the slots array, exposed outward via IItemHandler;
 * locked means neither side gets in or out; also, a spider might live inside (released on opening).
 */
public abstract class TileEntityCrateBase extends TileEntityLockableBase implements IInventory, ICapabilityHost, IGUIProvider, ITickable {

	protected ItemStack[] slots;
	public String customName;

	public boolean hasSpiders = false;

	/** Capability view over the slots array; refuses traffic in both directions while locked */
	private IItemHandler itemHandler;

	public TileEntityCrateBase(int count) {
		slots = new ItemStack[count];
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
		this.markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack stack) {
		slots[i] = stack;
		if(stack != null && stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}
		this.markDirty();
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null && !this.customName.isEmpty();
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
		return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				SoundRefs.resolve(new ResourceLocation(NTMSounds.CRATE_OPEN)), SoundCategory.BLOCKS, 1.0F, 1.0F);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				SoundRefs.resolve(new ResourceLocation(NTMSounds.CRATE_CLOSE)), SoundCategory.BLOCKS, 1.0F, 1.0F);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return true;
	}

	/** Per-tick callback: does nothing by default; crates that tick (the tungsten one) override it */
	public void updateEntity() { }

	@Override
	public void update() {
		if(this.isLoaded) updateEntity();
	}

	/** Subclasses may block inserts into certain slots by item contents (mass storage's slot-0 rule is one such) */
	public boolean canInsertSlot(int slot, ItemStack stack) {
		return true;
	}

	/** Subclasses may block extraction from certain slots by item contents (the tungsten crate does exactly this) */
	public boolean canExtractSlot(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack decrStackSize(int i, int amount) {
		if(slots[i] != null) {
			if(slots[i].getCount() <= amount) {
				ItemStack stack = slots[i];
				slots[i] = null;
				return stack;
			}
			ItemStack stack = slots[i].splitStack(amount);
			if(slots[i].getCount() == 0) {
				slots[i] = null;
			}

			this.markDirty();
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

	@Override
	public IItemHandler getItemHandler() {
		if(itemHandler == null) {
			itemHandler = new HbmArrayItemHandler(this.slots, this::markDirty) {
				@Override
				public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
					if(isLocked()) return stack;
					if(!canInsertSlot(slot, stack)) return stack;
					return super.insertItem(slot, stack, simulate);
				}

				@Override
				public ItemStack extractItem(int slot, int amount, boolean simulate) {
					if(isLocked()) return ItemStack.EMPTY;
					if(!canExtractSlot(slot, getStackInSlot(slot))) return ItemStack.EMPTY;
					return super.extractItem(slot, amount, simulate);
				}
			};
		}
		return itemHandler;
	}

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

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		slots = new ItemStack[getSizeInventory()];

		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length) {
				slots[b0] = new ItemStack(nbt1);
			}
		}
		this.hasSpiders = nbt.getBoolean("spiders");

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
		nbt.setBoolean("spiders", hasSpiders);

		if(customName != null) {
			nbt.setString("name", customName);
		}

		return nbt;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		readFromNBT(packet.getNbtCompound());
	}

	// for hiding the spider
	public void fillWithSpiders() {
		this.hasSpiders = true;
	}

	private static final int numSpiders = 3;

	/** Used when opened from the block: releases the spider around the crate */
	public static void spawnSpiders(EntityPlayer player, World world, TileEntityCrateBase crate) {
		if(crate.hasSpiders) {
			Random random = new Random();

			for(int i = 0; i < numSpiders; i++) {

				EntityCaveSpider spider = new EntityCaveSpider(world);
				spider.setLocationAndAngles(crate.getPos().getX() + random.nextGaussian() * 2, crate.getPos().getY() + 1, crate.getPos().getZ() + random.nextGaussian() * 2, random.nextFloat(), 0);
				spider.setAttackTarget(player);

				world.spawnEntity(spider);
			}
			crate.hasSpiders = false;
			crate.markDirty();
		}
	}

	/** Used when opened from the crate in a player's inventory: the spider lands next to the player and the marker is wiped */
	public static void spawnSpiders(EntityPlayer player, World world, ItemStack crate) {
		if(crate.hasTagCompound() && crate.getTagCompound().getBoolean("spiders")) {
			Random random = new Random();

			for(int i = 0; i < numSpiders; i++) {

				EntityCaveSpider spider = new EntityCaveSpider(world);
				spider.setLocationAndAngles(player.posX + random.nextGaussian() * 2, player.posY + 1, player.posZ + random.nextGaussian() * 2, random.nextFloat(), 0);
				spider.setAttackTarget(player);

				world.spawnEntity(spider);
			}
			crate.getTagCompound().removeTag("spiders");
		}
	}
}
