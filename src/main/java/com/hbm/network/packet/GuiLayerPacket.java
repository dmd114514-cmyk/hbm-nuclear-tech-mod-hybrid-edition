package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.inventory.SlotLayer;
import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** client → server: tells the server which GUI layer was switched to, so the container's layered slots follow */
public class GuiLayerPacket extends PacketBase {

	private int layer;

	public GuiLayerPacket() { }

	public GuiLayerPacket(int layer) {
		this.layer = layer;
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeByte(this.layer);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.layer = buf.readByte();
	}

	public static class Handler implements IMessageHandler<GuiLayerPacket, IMessage> {

		@Override
		public IMessage onMessage(GuiLayerPacket message, MessageContext ctx) {

			EntityPlayerMP player = ctx.getServerHandler().player;

			NetworkHandler.scheduleServer(player.world, () -> {

				if(player.openContainer == null) return;

				for(int i = 0; i < player.openContainer.inventorySlots.size(); i++) {
					Slot slot = player.openContainer.inventorySlots.get(i);
					if(slot instanceof SlotLayer) {
						((SlotLayer) slot).setLayer(message.layer);
					}
				}
			});
			return null;
		}
	}
}
