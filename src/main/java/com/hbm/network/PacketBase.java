package com.hbm.network;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Packet base class. Dries up IMessage's pair of "theoretically throws Exception" methods,
 * subclasses just write serialize/deserialize, and a forgotten exception handling won't wedge the build.
 *
 * Contract: serialize and deserialize field order must mirror exactly; change one, change the other.
 */
public abstract class PacketBase implements IMessage {

	@Override
	public final void fromBytes(ByteBuf buf) {
		try {
			deserialize(buf);
		} catch(IOException ex) {
			throw new IllegalStateException("Failed to read packet " + getClass().getSimpleName(), ex);
		}
	}

	@Override
	public final void toBytes(ByteBuf buf) {
		try {
			serialize(buf);
		} catch(IOException ex) {
			throw new IllegalStateException("Failed to write packet " + getClass().getSimpleName(), ex);
		}
	}

	protected abstract void serialize(ByteBuf buf) throws IOException;
	protected abstract void deserialize(ByteBuf buf) throws IOException;

	/*
	 * Common field read/write, so every packet doesn't reinvent it
	 */

	protected static void writeString(ByteBuf buf, String value) {
		ByteBufUtils.writeUTF8String(buf, value == null ? "" : value);
	}

	protected static String readString(ByteBuf buf) {
		return ByteBufUtils.readUTF8String(buf);
	}

	protected static void writeNBT(ByteBuf buf, NBTTagCompound tag) {
		ByteBufUtils.writeTag(buf, tag);
	}

	protected static NBTTagCompound readNBT(ByteBuf buf) {
		return ByteBufUtils.readTag(buf);
	}

	protected static void writeItemStack(ByteBuf buf, ItemStack stack) {
		ByteBufUtils.writeItemStack(buf, stack == null ? ItemStack.EMPTY : stack);
	}

	protected static ItemStack readItemStack(ByteBuf buf) {
		return ByteBufUtils.readItemStack(buf);
	}
}
