package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * client → server: hands a lump of control data to a machine.
 * Every button, switch and dropdown on a GUI goes through this; don't add a new packet per switch.
 */
public class NbtControlPacket extends PacketBase {

	private BlockPos pos = BlockPos.ORIGIN;
	private NBTTagCompound data;

	public NbtControlPacket() { }

	public NbtControlPacket(NBTTagCompound data, int x, int y, int z) {
		this.data = data;
		this.pos = new BlockPos(x, y, z);
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		writeNBT(buf, data);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		int x = buf.readInt();
		int y = buf.readInt();
		int z = buf.readInt();
		this.pos = new BlockPos(x, y, z);
		this.data = readNBT(buf);
	}

	public static class Handler implements IMessageHandler<NbtControlPacket, IMessage> {

		@Override
		public IMessage onMessage(NbtControlPacket message, MessageContext ctx) {

			EntityPlayerMP player = ctx.getServerHandler().player;

			//packets arrive on the network thread; touching the world means returning to the server main thread
			NetworkHandler.scheduleServer(player.world, () -> {

				TileEntity te = player.world.getTileEntity(message.pos);
				if(te instanceof IControlReceiver && message.data != null) {
					IControlReceiver receiver = (IControlReceiver) te;
					if(receiver.hasPermission(player)) {
						receiver.receiveControl(player, message.data);
						receiver.receiveControl(message.data);
					}
				}
			});
			return null;
		}
	}
}
