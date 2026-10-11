package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.handler.HbmKeybindsServer;
import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * client → server: one of the hbm keybinds was pressed or released.
 * <br><br>
 * Keybind enums ride the wire by ordinal, so the packet doesn't need to serialize the enum itself; the ordinal is this table's public face,
 * which means {@code EnumKeybind} can only gain values at the end — inserting mid-list makes both ends read the wrong key.
 */
public class KeybindPacket extends PacketBase {

	private int key;
	private boolean pressed;

	public KeybindPacket() { }

	public KeybindPacket(EnumKeybind key, boolean pressed) {
		this.key = key.ordinal();
		this.pressed = pressed;
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeInt(this.key);
		buf.writeBoolean(this.pressed);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.key = buf.readInt();
		this.pressed = buf.readBoolean();
	}

	public static class Handler implements IMessageHandler<KeybindPacket, IMessage> {

		@Override
		public IMessage onMessage(KeybindPacket message, MessageContext ctx) {

			EnumKeybind[] values = EnumKeybind.values();
			if(message.key < 0 || message.key >= values.length)
				return null;	// reached when the two ends disagree on version; dropping beats an out-of-bounds

			EntityPlayerMP player = ctx.getServerHandler().player;

			NetworkHandler.scheduleServer(player.world, () -> {
				HbmKeybindsServer.onPressedServer(player, values[message.key], message.pressed);
			});
			return null;
		}
	}
}
