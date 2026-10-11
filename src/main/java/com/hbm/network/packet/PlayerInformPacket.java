package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.main.MainRegistry;
import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * On-screen floating tips (wrench info, equipment status and friends). Same ids replace each other, so high-frequency refreshes
 * with the same id keep only the latest — the screen doesn't get smeared.
 *
 * The fancy branch carries chat-component JSON, for colors and hover notes; the client parses it and hands it to the proxy to render.
 */
public class PlayerInformPacket extends PacketBase {

	private String message = "";
	private int id;
	private int millis;
	private boolean fancy;

	public PlayerInformPacket() { }

	public PlayerInformPacket(String message, int id) {
		this(message, id, 1000);
	}

	public PlayerInformPacket(String message, int id, int millis) {
		this.fancy = false;
		this.message = message;
		this.id = id;
		this.millis = millis;
	}

	public PlayerInformPacket(ITextComponent component, int id) {
		this(component, id, 1000);
	}

	public PlayerInformPacket(ITextComponent component, int id, int millis) {
		this.fancy = true;
		this.message = ITextComponent.Serializer.componentToJson(component);
		this.id = id;
		this.millis = millis;
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeInt(id);
		buf.writeInt(millis);
		buf.writeBoolean(fancy);
		writeString(buf, message);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.id = buf.readInt();
		this.millis = buf.readInt();
		this.fancy = buf.readBoolean();
		this.message = readString(buf);
	}

	public static class Handler implements IMessageHandler<PlayerInformPacket, IMessage> {

		@Override
		public IMessage onMessage(PlayerInformPacket msg, MessageContext ctx) {

			NetworkHandler.scheduleClient(() -> {
				if(msg.fancy) {
					ITextComponent component = ITextComponent.Serializer.jsonToComponent(msg.message);
					if(component != null) MainRegistry.proxy.displayTooltip(component.getFormattedText(), msg.millis, msg.id);
				} else {
					MainRegistry.proxy.displayTooltip(msg.message, msg.millis, msg.id);
				}
			});
			return null;
		}
	}
}
