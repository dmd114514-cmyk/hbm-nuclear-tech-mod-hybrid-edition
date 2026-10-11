package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.main.MainRegistry;
import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * The generic "aux particle" packet: throws a lump of particle data at the client and lets the proxy decide how to draw it.
 *
 * Position goes straight into the NBT instead of three extra double fields, because that NBT is exactly what the downstream consumer reads,
 * and every extra field is one more place to disagree.
 */
public class AuxParticlePacketNT extends PacketBase {

	public NBTTagCompound nbt;

	public AuxParticlePacketNT() { }

	public AuxParticlePacketNT(NBTTagCompound nbt, double x, double y, double z) {
		nbt.setDouble("posX", x);
		nbt.setDouble("posY", y);
		nbt.setDouble("posZ", z);
		this.nbt = nbt;
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		writeNBT(buf, nbt);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.nbt = readNBT(buf);
	}

	public static class Handler implements IMessageHandler<AuxParticlePacketNT, IMessage> {

		@Override
		public IMessage onMessage(AuxParticlePacketNT message, MessageContext ctx) {

			// packets arrive on the network thread; drawing particles means returning to the client main thread
			NetworkHandler.scheduleClient(() -> {
				if(MainRegistry.proxy.me() == null || message.nbt == null) return;
				MainRegistry.proxy.effectNT(message.nbt);
			});
			return null;
		}
	}
}
