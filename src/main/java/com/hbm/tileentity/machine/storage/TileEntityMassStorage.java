package com.hbm.tileentity.machine.storage;

import com.hbm.inventory.container.ContainerMassStorage;
import com.hbm.inventory.gui.GUIMassStorage;
import com.hbm.items.ModItems;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.util.ItemStackUtil;

import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Mass storage: one big vat holding a single thing (slot 1 is the filter); slot 0 is the input, slot 2 the output.
 * Capacity is set by the block subtype, the comparator reads the fill ratio, and redstone radio can read and write it.
 */
public class TileEntityMassStorage extends TileEntityCrateBase implements IControlReceiverFilter, IRORValueProvider, IRORInteractive {

	private int stack = 0;
	public boolean output = false;
	private int capacity;
	public int redstone = 0;

	@SideOnly(Side.CLIENT) public ItemStack type;

	public TileEntityMassStorage() {
		super(3);
	}

	public TileEntityMassStorage(int capacity) {
		this();
		this.capacity = capacity;
	}

	@Override
	public String getName() {
		return "container.massStorage";
	}

	@Override
	public void updateEntity() {

		if(!world.isRemote) {

			int newRed = this.getStockpile() * 15 / this.capacity;

			if(newRed != this.redstone) {
				this.redstone = newRed;
				this.markDirty();
			}

			//an infinite bucket poured in fills the vat
			if(slots[0] != null && slots[0].getItem() == ModItems.fluid_barrel_infinite) {
				this.stack = this.getCapacity();
			}

			if(slots[1] == null) {
				this.stack = 0;
			}

			if(canInsert(slots[0])) {

				int remaining = getCapacity() - getStockpile();
				int toRemove = Math.min(remaining, slots[0].getCount());
				this.decrStackSize(0, toRemove);
				this.stack += toRemove;
				this.markChanged();
			}

			if(output && slots[1] != null) {

				if(slots[2] != null && !ItemStackUtil.areStacksCompatible(slots[2], getType())) {
					return;
				}

				int amount = Math.min(getStockpile(), getType().getMaxStackSize());

				if(amount > 0) {
					if(slots[2] == null) {
						slots[2] = slots[1].copy();
						slots[2].setCount(amount);
					} else {
						amount = Math.min(amount, slots[2].getMaxStackSize() - slots[2].getCount());
						slots[2].setCount(slots[2].getCount() + amount);
					}
					this.stack -= amount;
				}
			}

			//when amount or toggle changes, nearby clients get a fresh copy of the tile entity data (this version uses vanilla's TE update packet)
			this.world.notifyBlockUpdate(this.pos, this.world.getBlockState(this.pos), this.world.getBlockState(this.pos), 2);
		}
	}

	public boolean canInsert(ItemStack stack) {
		return stack != null && !stack.isEmpty() && slots[1] != null && getStockpile() < getCapacity() && ItemStackUtil.areStacksCompatible(stack, getType());
	}

	public boolean quickInsert(ItemStack stack) {
		if(!canInsert(stack)) return false;

		int remaining = getCapacity() - getStockpile();
		if(remaining < stack.getCount()) return false;

		this.stack += stack.getCount();
		stack.setCount(0);
		this.markDirty();

		return true;
	}

	public ItemStack quickExtract() {
		if(!output || slots[1] == null) return ItemStack.EMPTY;

		int amount = getType().getMaxStackSize();

		if(getStockpile() < amount) return ItemStack.EMPTY;

		ItemStack result = slots[1].copy();
		result.setCount(amount);
		this.stack -= amount;
		this.markDirty();

		return result;
	}

	//the three below are the whole-vat readouts for automation/external mods

	public int getTotalStockpile() {

		if(slots[1] == null) return 0;

		int result = getStockpile();

		ItemStack inStack = slots[0];
		if(inStack != null && ItemStackUtil.areStacksCompatible(getType(), inStack)) {
			result += inStack.getCount();
		}

		ItemStack outStack = slots[2];
		if(outStack != null && ItemStackUtil.areStacksCompatible(getType(), outStack)) {
			result += outStack.getCount();
		}

		return result;
	}

	/** Returns what didn't fit; actually false only simulates without touching state */
	public int increaseTotalStockpile(int amount, boolean actually) {
		return changeTotalStockpile(amount, actually, +1);
	}

	/** Returns what couldn't be drawn; actually false only simulates without touching state */
	public int decreaseTotalStockpile(int amount, boolean actually) {
		return changeTotalStockpile(amount, actually, -1);
	}

	private int changeTotalStockpile(int amount, boolean actually, int sign) {

		if(slots[1] == null) return amount;

		ItemStack type = getType();
		int stockpileAvail = sign > 0 ? getCapacity() - getStockpile() : getStockpile();

		if(amount > 0 && stockpileAvail > 0) {
			int depositStockpile = Math.min(amount, stockpileAvail);
			if(actually) {
				this.stack += sign * depositStockpile;
			}
			amount -= depositStockpile;
		}

		ItemStack inStack = slots[0];
		int inputAvail = 0;

		if(inStack != null && ItemStackUtil.areStacksCompatible(type, inStack)) {
			inputAvail = sign > 0 ? inStack.getMaxStackSize() - inStack.getCount() : inStack.getCount();
		} else if(inStack == null) {
			inputAvail = sign > 0 ? type.getMaxStackSize() : 0;
		}

		if(amount > 0 && inputAvail > 0) {
			int depositInput = Math.min(amount, inputAvail);
			if(actually) {
				if(slots[0] == null) { //only possible when sign == +1
					slots[0] = slots[1].copy();
					slots[0].setCount(0);
				}
				slots[0].setCount(slots[0].getCount() + sign * depositInput);
				if(slots[0].getCount() == 0) {
					slots[0] = null;
				}
			}
			amount -= depositInput;
		}

		ItemStack outStack = slots[2];
		int outputAvail = 0;

		if(outStack != null && ItemStackUtil.areStacksCompatible(type, outStack)) {
			outputAvail = sign > 0 ? outStack.getMaxStackSize() - outStack.getCount() : outStack.getCount();
		} else if(outStack == null) {
			outputAvail = sign > 0 ? type.getMaxStackSize() : 0;
		}

		if(amount > 0 && outputAvail > 0) {
			int depositOutput = Math.min(amount, outputAvail);
			if(actually) {
				if(slots[2] == null) { //only possible when sign == +1
					slots[2] = slots[1].copy();
					slots[2].setCount(0);
				}
				slots[2].setCount(slots[2].getCount() + sign * depositOutput);
				if(slots[2].getCount() == 0) {
					slots[2] = null;
				}
			}
			amount -= depositOutput;
		}

		if(actually) {
			this.markDirty();
		}

		return amount;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeInt(this.stack);
		buf.writeBoolean(this.output);
		ByteBufUtils.writeItemStack(buf, this.slots[1]);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.stack = buf.readInt();
		this.output = buf.readBoolean();
		this.type = ByteBufUtils.readItemStack(buf);
	}

	public int getCapacity() {
		return capacity;
	}

	public ItemStack getType() {
		return slots[1] == null ? null : slots[1].copy();
	}

	public int getStockpile() {
		return stack;
	}

	public void setStockpile(int stack) {
		this.stack = stack;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return new Vec3d(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).lengthVector() < 20;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				com.hbm.sound.SoundRefs.resolve(new net.minecraft.util.ResourceLocation("hbm:block.storageopen")),
				net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				com.hbm.sound.SoundRefs.resolve(new net.minecraft.util.ResourceLocation("hbm:block.storageclose")),
				net.minecraft.util.SoundCategory.BLOCKS, 0.5F, 1.0F);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.stack = nbt.getInteger("stack");
		this.output = nbt.getBoolean("output");
		this.capacity = nbt.getInteger("capacity");
		this.redstone = nbt.getByte("redstone");

		if(this.capacity <= 0) {
			this.capacity = 10_000;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setInteger("stack", stack);
		nbt.setBoolean("output", output);
		nbt.setInteger("capacity", capacity);
		nbt.setByte("redstone", (byte) redstone);
		return nbt;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public double getMaxRenderDistanceSquared() {
		return 1024.0D; //only shows the fill readout within 32 blocks; saves a little performance
	}

	@Override
	public void nextMode(int i) { }

	@Override
	public void receiveControl(NBTTagCompound data) {

		if(data.hasKey("provide") && slots[1] != null) {

			if(this.getStockpile() == 0) {
				return;
			}

			int amount = data.getBoolean("provide") ? slots[1].getMaxStackSize() : 1;
			amount = Math.min(amount, getStockpile());

			if(slots[2] != null && !ItemStackUtil.areStacksCompatible(slots[2], getType())) {
				return;
			}

			if(slots[2] == null) {
				slots[2] = slots[1].copy();
				slots[2].setCount(amount);
			} else {
				amount = Math.min(amount, slots[2].getMaxStackSize() - slots[2].getCount());
				slots[2].setCount(slots[2].getCount() + amount);
			}
			this.stack -= amount;
		}

		if(data.hasKey("toggle")) {
			this.output = !output;
		}

		if(data.hasKey("slot") && this.getStockpile() <= 0) {
			setFilterContents(data);
		}
	}

	/** The input only takes what matches the filter, the output only releases slot 2; both obey the lock */
	@Override
	public boolean canInsertSlot(int slot, ItemStack stack) {
		return !this.isLocked() && slot == 0
				&& (slots[1] == null || ItemStackUtil.areStacksCompatible(stack, getType()));
	}

	@Override
	public boolean canExtractSlot(int slot, ItemStack stack) {
		return !this.isLocked() && slot == 2;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerMassStorage(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUIMassStorage(player.inventory, this);
	}

	@Override
	public int[] getFilterSlots() {
		return new int[] { 1, 2 };
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_VALUE + "type",
				PREFIX_VALUE + "fill",
				PREFIX_VALUE + "fillpercent",
				PREFIX_FUNCTION + "toggleoutput",
		};
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "fill").equals(name)) return "" + this.stack;
		if((PREFIX_VALUE + "fillpercent").equals(name)) return "" + this.stack * 100 / this.capacity;
		if((PREFIX_VALUE + "type").equals(name)) {
			if(slots[1] == null) return "None";
			return slots[1].getDisplayName();
		}
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if((PREFIX_FUNCTION + "toggleoutput").equals(name)) {
			this.output = !this.output;
			this.markDirty();
		}

		return null;
	}
}
