package com.hbm.tileentity.machine;

import java.util.Arrays;

import com.hbm.inventory.container.ContainerMachineSiren;
import com.hbm.inventory.gui.GUIMachineSiren;
import com.hbm.items.machine.ItemCassette;
import com.hbm.items.machine.ItemCassette.SoundType;
import com.hbm.items.machine.ItemCassette.TrackType;
import com.hbm.network.NetworkHandler;
import com.hbm.network.packet.TESirenPacket;
import com.hbm.sound.SoundLoopSiren;
import com.hbm.capability.HbmArrayItemHandler;
import com.hbm.capability.ICapabilityHost;
import com.hbm.tileentity.IGUIProvider;

import api.hbm.tile.ISirenSync;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityMachineSiren extends TileEntity implements ICapabilityHost, ITickable, IGUIProvider, ISirenSync {

	private ItemStack[] slots;

	/** The inventory's Forge view hugs the same slots array; capability and old code, neither holds more truth */
	private IItemHandler itemHandler;
	
	
	public boolean lock = false;
	
	private String customName;
	
	public TileEntityMachineSiren() {
		slots = new ItemStack[1];
	}

	public int getSizeInventory() {
		return slots.length;
	}

	public boolean isEmpty() {
		for(ItemStack stack : slots) {
			if(stack != null && !stack.isEmpty()) return false;
		}
		return true;
	}

	public String getName() {
		return this.hasCustomName() ? this.customName : "container.siren";
	}

	public boolean hasCustomName() {
		return this.customName != null && this.customName.length() > 0;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	public void setCustomName(String name) {
		this.customName = name;
		markDirty();
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public boolean isUsableByPlayer(EntityPlayer player) {
		if(world.getTileEntity(pos) != this)
		{
			return false;
		}else{
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}
	
	@Override
	public IItemHandler getItemHandler() {
		if(itemHandler == null) {
			//slot 0 has never accepted external inserts (isItemValidForSlot always false); keeping the same rule here
			itemHandler = new HbmArrayItemHandler(this.slots, this::markDirty) {
				@Override
				public boolean isItemValid(int slot, ItemStack stack) {
					return false;
				}
			};
		}
		return itemHandler;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("items", 10);

		slots = new ItemStack[getSizeInventory()];
		
		for(int i = 0; i < list.tagCount(); i++)
		{
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < slots.length)
			{
				slots[b0] = new ItemStack(nbt1);
			}
		}

		customName = nbt.getString("name");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		NBTTagList list = new NBTTagList();
		
		for(int i = 0; i < slots.length; i++)
		{
			if(slots[i] != null)
			{
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte)i);
				slots[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("items", list);
		
		if (customName != null) {
			nbt.setString("name", customName);
		}
		return nbt;
	}

	@Override
	public void update() {
		
		if(!world.isRemote) {
			int id = Arrays.asList(TrackType.values()).indexOf(getCurrentType());
			
			if(getCurrentType().name().equals(TrackType.NULL.name())) {
				sendTrack(id, false);
				return;
			}
			
			boolean active = world.isBlockIndirectlyGettingPowered(pos) > 0;
			
			if(getCurrentType().getType().name().equals(SoundType.LOOP.name())) {
				
				sendTrack(id, active);
			} else {
				
				if(!lock && active) {
					lock = true;
					sendTrack(id, false);
					sendTrack(id, true);
				}
				
				if(lock && !active) {
					lock = false;
				}
			}
		}
	}
	
	/** Track state goes to the client only; the radius is the old version's: this thing is meant to be heard from far away */
	private void sendTrack(int id, boolean active) {
		NetworkHandler.sendToAllAround(new TESirenPacket(pos.getX(), pos.getY(), pos.getZ(), id, active), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 1500));
	}
	
	public TrackType getCurrentType() {
		if(slots[0] != null && slots[0].getItem() instanceof ItemCassette) {
			return TrackType.getEnum(slots[0].getItemDamage());
		}
		
		return TrackType.NULL;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setSirenTrack(int id, boolean active) {
		// Only the client's packet handling reaches here; with SideOnly the server strips the whole method,
		// so the SoundLoopSiren inside (an ISound) never even gets resolved on the server
		if(world.isRemote) {
			TrackType track = TrackType.getEnum(id);
			SoundLoopSiren sound = null;
			for(int i = 0; i < SoundLoopSiren.list.size(); i++) {
				if(SoundLoopSiren.list.get(i).getTE() == this)
					sound = SoundLoopSiren.list.get(i);
			}
			
			if(active) {
				
				if(sound == null) {
					//attack
					if(id > 0) {
						boolean b = track.getType().name().equals(SoundType.LOOP.name());
						SoundLoopSiren s = new SoundLoopSiren(track.getSoundLocation(), this, track.getType());
						s.setRepeat(b);
						s.intendedVolume = track.getVolume();
						Minecraft.getMinecraft().getSoundHandler().playSound(s);
					}
				} else {
					net.minecraft.util.ResourceLocation loc = track.getSoundLocation();
					
					if(loc != null) {
						String path = loc.getResourceDomain() + ":" + loc.getResourcePath();
						
						if(!sound.getPath().equals(path)) {
							//track changed, stop then start
							sound.endSound();
							if(id > 0)
								Minecraft.getMinecraft().getSoundHandler().playSound(new SoundLoopSiren(loc, this, track.getType()));
						}
					}
					
					sound.intendedVolume = track.getVolume();
				}
				
			} else {
				
				if(sound != null) {
					//release
					sound.endSound();
					SoundLoopSiren.list.remove(sound);
				}
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new ContainerMachineSiren(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, BlockPos pos) {
		return new GUIMachineSiren(player.inventory, this);
	}
}
