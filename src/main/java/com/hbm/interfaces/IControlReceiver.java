package com.hbm.interfaces;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Receives "complex control data" from clients. Data always rides NBT; don't add new packet types.
 */
public interface IControlReceiver {

	public boolean hasPermission(EntityPlayer player);

	public void receiveControl(NBTTagCompound data);

	/** Fallback overload so the existing implementations don't all need touching */
	public default void receiveControl(EntityPlayer player, NBTTagCompound data) { }
}
