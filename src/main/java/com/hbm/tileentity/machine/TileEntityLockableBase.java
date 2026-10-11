package com.hbm.tileentity.machine;

import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKey;
import com.hbm.sound.SoundRefs;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ArmorUtil;
import com.hbm.util.LoggingUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

/** Locked tile entity: opens when the pin code matches, or gets picked with a pin + screwdriver (a broken pick makes noise) */
public abstract class TileEntityLockableBase extends TileEntityLoadedBase {

	protected int lock;
	private boolean isLocked = false;
	protected double lockMod = 0.1D;
	/** Whether a fake key can talk its way in */
	public boolean cheesable = true;

	public boolean isLocked() {
		return isLocked;
	}

	public void lock() {
		if(lock == 0) {
			LoggingUtil.error("A block has been set to locked state before setting pins, this should not happen and may cause errors! " + this.toString());
		}
		isLocked = true;
		markDirty();
	}

	public void unlock() {
		isLocked = false;
		markDirty();
	}

	public void setPins(int pins) { lock = pins; markDirty(); }
	public int getPins() { return lock; }
	public void setMod(double mod) { lockMod = mod; markDirty(); }
	public double getMod() { return lockMod; }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		lock = nbt.getInteger("lock");
		cheesable = nbt.getBoolean("cheesable");
		isLocked = nbt.getBoolean("isLocked");
		lockMod = nbt.getDouble("lockMod");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("lock", lock);
		nbt.setBoolean("cheesable", cheesable);
		nbt.setBoolean("isLocked", isLocked);
		nbt.setDouble("lockMod", lockMod);

		return nbt;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeInt(lock);
		buf.writeBoolean(cheesable);
		buf.writeBoolean(isLocked);
		buf.writeDouble(lockMod);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		lock = buf.readInt();
		cheesable = buf.readBoolean();
		isLocked = buf.readBoolean();
		lockMod = buf.readDouble();
	}

	public boolean canAccess(EntityPlayer player) {

		if(!isLocked) {
			return true;
		}

		if(player == null) {
			return false;
		}

		ItemStack stack = player.getHeldItemMainhand();

		if(!stack.isEmpty() && stack.getItem() instanceof ItemKey && ItemKey.getPins(stack) == this.lock) {
			playLockSound(player, "block.lockopen", 1.0F, 1.0F);
			return true;
		}

		if(!stack.isEmpty() && stack.getItem() == ModItems.key_red) {
			playLockSound(player, "block.lockopen", 1.0F, 1.0F);
			return true;
		}

		return tryPick(player);
	}

	private boolean tryPick(EntityPlayer player) {

		boolean canPick = false;
		ItemStack stack = player.getHeldItemMainhand();
		double chanceOfSuccess = this.lockMod * 100;

		if(!stack.isEmpty() && stack.getItem() == ModItems.pin
				&& (player.inventory.hasItemStack(new ItemStack(ModItems.screwdriver)) || player.inventory.hasItemStack(new ItemStack(ModItems.screwdriver_desh)))) {
			stack.shrink(1);
			canPick = true;
		}

		if(!stack.isEmpty() && (stack.getItem() == ModItems.screwdriver || stack.getItem() == ModItems.screwdriver_desh)
				&& player.inventory.hasItemStack(new ItemStack(ModItems.pin))) {
			player.inventory.clearMatchingItems(ModItems.pin, 0, 1, null);
			player.inventoryContainer.detectAndSendChanges();
			canPick = true;
		}

		if(canPick) {

			//anti-pick gloves send the success rate straight to max
			if(ArmorUtil.checkArmorPiece(player, ModItems.jackt, 2) || ArmorUtil.checkArmorPiece(player, ModItems.jackt2, 2))
				chanceOfSuccess *= 100D;

			double rand = player.world.rand.nextDouble() * 100;

			if(chanceOfSuccess > rand) {
				playLockSound(player, "item.pinunlock", 1.0F, 1.0F);
				return true;
			}

			playLockSound(player, "item.pinbreak", 1.0F, 0.8F + player.world.rand.nextFloat() * 0.2F);
		}

		return false;
	}

	private void playLockSound(EntityPlayer player, String name, float volume, float pitch) {
		player.world.playSound(null, player.posX, player.posY, player.posZ,
				SoundRefs.resolve(new ResourceLocation("hbm:" + name)), SoundCategory.BLOCKS, volume, pitch);
	}
}
