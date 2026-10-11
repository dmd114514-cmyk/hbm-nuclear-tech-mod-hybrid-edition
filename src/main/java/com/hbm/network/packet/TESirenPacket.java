package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;
import api.hbm.tile.ISirenSync;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * The siren start/stop/track-switch sync packet.
 *
 * Carries only coordinates + track id + on/off, no content-layer types at all: the sender (the machine TE) knows what it wants
 * to play, the client finds the TE by coordinates and asks — no need to serialize the whole machine's state across.
 */
public class TESirenPacket extends PacketBase {

	private int x;
	private int y;
	private int z;
	private int id;
	private boolean active;

	public TESirenPacket() { }

	public TESirenPacket(int x, int y, int z, int id, boolean active) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.id = id;
		this.active = active;
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(id);
		buf.writeBoolean(active);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.id = buf.readInt();
		this.active = buf.readBoolean();
	}

	public static class Handler implements IMessageHandler<TESirenPacket, IMessage> {

		@Override
		public IMessage onMessage(TESirenPacket msg, MessageContext ctx) {

			// packets arrive on the network thread; grabbing a tile entity means returning to the main thread
			NetworkHandler.scheduleClient(() -> {
				net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
				if(mc.world == null) return;

				TileEntity te = mc.world.getTileEntity(new BlockPos(msg.x, msg.y, msg.z));
				if(te instanceof ISirenSync) ((ISirenSync) te).setSirenTrack(msg.id, msg.active);
			});
			return null;
		}
	}
}
