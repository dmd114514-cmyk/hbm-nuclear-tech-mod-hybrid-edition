package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;
import api.hbm.tile.ISwitchSync;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * The instant-sync packet for a rail switch position.
 *
 * Carries only coordinates + one boolean: the switch state is "click and it flips", and waiting for the vanilla chunk/block update flush
 * lags a beat, which makes the client-side rail drawing jitter. Coordinates are always the **core** cell — on a spread-out multiblock
 * the player clicks any cell, the TE sits on the core; the sender converts the position itself.
 */
public class TESwitchPacket extends PacketBase {

	private int x;
	private int y;
	private int z;
	private boolean switched;

	public TESwitchPacket() { }

	public TESwitchPacket(int x, int y, int z, boolean switched) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.switched = switched;
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeBoolean(switched);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		this.switched = buf.readBoolean();
	}

	public static class Handler implements IMessageHandler<TESwitchPacket, IMessage> {

		@Override
		public IMessage onMessage(TESwitchPacket msg, MessageContext ctx) {

			// packets arrive on the network thread; grabbing a tile entity means returning to the main thread
			NetworkHandler.scheduleClient(() -> {
				net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
				if(mc.world == null) return;

				TileEntity te = mc.world.getTileEntity(new BlockPos(msg.x, msg.y, msg.z));
				if(te instanceof ISwitchSync) ((ISwitchSync) te).setSwitched(msg.switched);
			});
			return null;
		}
	}
}
