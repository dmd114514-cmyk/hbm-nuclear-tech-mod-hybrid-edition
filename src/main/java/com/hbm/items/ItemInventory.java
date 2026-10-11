package com.hbm.items;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import com.hbm.main.NTMSounds;
import com.hbm.util.ItemStackUtil;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

/**
 * The base for items carrying an inventory: contents live in the item's own NBT (crates, sealed crates, toolboxes — this family),
 * with the slot array filled by each subclass per its capacity; this handles reads/writes, over-capacity warnings and door sounds.
 */
public abstract class ItemInventory implements IInventory {

	public EntityPlayer player;
	public ItemStack[] slots;
	public ItemStack target;

	@Override
	public void markDirty() {

		for(int i = 0; i < getSizeInventory(); ++i) {
			if(getStackInSlot(i) != null && getStackInSlot(i).getCount() == 0) {
				slots[i] = null;
			}
		}

		//also keeps a stack list in the old style — that's what the sealed crate family reads
		ItemStackUtil.addStacksToNBT(target, slots);
		target.setTagCompound(checkNBT(target.getTagCompound()));
	}

	public NBTTagCompound checkNBT(NBTTagCompound nbt) {

		if(nbt == null || nbt.hasNoTags())
			return null;

		Random random = new Random();

		try {
			//no compress(NBTTagCompound) in this version; write into a memory stream and measure the length instead
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			CompressedStreamTools.writeCompressed(nbt, out);
			byte[] abyte = out.toByteArray();

			if(abyte.length > 6000) {
				player.sendMessage(new TextComponentString(TextFormatting.RED + "Warning: Container NBT exceeds 6kB, contents will be ejected!"));
				for(int i1 = 0; i1 < this.getSizeInventory(); ++i1) {
					ItemStack itemstack = this.getStackInSlot(i1);

					if(itemstack != null) {
						float f = random.nextFloat() * 0.8F + 0.1F;
						float f1 = random.nextFloat() * 0.8F + 0.1F;
						float f2 = random.nextFloat() * 0.8F + 0.1F;

						while(itemstack.getCount() > 0) {
							int j1 = random.nextInt(21) + 10;

							if(j1 > itemstack.getCount()) {
								j1 = itemstack.getCount();
							}

							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(player.world, player.posX + f, player.posY + f1, player.posZ + f2, new ItemStack(itemstack.getItem(), j1, itemstack.getMetadata()));

							if(itemstack.hasTagCompound()) {
								entityitem.getItem().setTagCompound(itemstack.getTagCompound().copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) random.nextGaussian() * f3 + player.motionX;
							entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F + player.motionY;
							entityitem.motionZ = (float) random.nextGaussian() * f3 + player.motionZ;
							player.world.spawnEntity(entityitem);
						}
					}
				}

				return null; // clear
			}
		} catch(IOException ignored) { }

		return nbt;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		ItemStack stack = getStackInSlot(slot);
		if(stack != null) {
			if(stack.getCount() > amount) {
				stack = stack.splitStack(amount);
				markDirty();
			} else {
				setInventorySlotContents(slot, null);
			}
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		if(stack != null) {
			stack.setCount(Math.min(stack.getCount(), this.getInventoryStackLimit()));
		}

		slots[slot] = stack;
		markDirty();
	}

	/*The old take-contents-on-container-close shape; IInventory lost that method in this version, kept as a subclass utility */
	public ItemStack takeStackOnClosing(int slot) {
		ItemStack stack = getStackInSlot(slot);
		setInventorySlotContents(slot, null);
		return stack;
	}

	@Override public ItemStack getStackInSlot(int slot) { return slots[slot]; }

	@Override public boolean isUsableByPlayer(EntityPlayer player) { return true; }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return true; }
	@Override public int getInventoryStackLimit() { return 64; }

	@Override
	public void openInventory(EntityPlayer player) {
		player.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.sound.SoundRefs.resolve(new net.minecraft.util.ResourceLocation(NTMSounds.CRATE_OPEN)), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 0.8F);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		player.world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.sound.SoundRefs.resolve(new net.minecraft.util.ResourceLocation(NTMSounds.CRATE_CLOSE)), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 0.8F);
	}
}
