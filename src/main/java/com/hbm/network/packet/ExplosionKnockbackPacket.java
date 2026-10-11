package com.hbm.network.packet;

import java.io.IOException;

import com.hbm.main.MainRegistry;
import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** The kick the explosion gives the player themselves: the client adds it straight onto its own velocity */
public class ExplosionKnockbackPacket extends PacketBase {

	private float motionX;
	private float motionY;
	private float motionZ;

	public ExplosionKnockbackPacket() { }

	public ExplosionKnockbackPacket(Vec3d vec) {
		this.motionX = (float) vec.x;
		this.motionY = (float) vec.y;
		this.motionZ = (float) vec.z;
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeFloat(this.motionX);
		buf.writeFloat(this.motionY);
		buf.writeFloat(this.motionZ);
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.motionX = buf.readFloat();
		this.motionY = buf.readFloat();
		this.motionZ = buf.readFloat();
	}

	public static class Handler implements IMessageHandler<ExplosionKnockbackPacket, IMessage> {

		@Override
		public IMessage onMessage(ExplosionKnockbackPacket message, MessageContext ctx) {

			// packets arrive on the network thread; changing player motion means returning to the client main thread
			NetworkHandler.scheduleClient(() -> {
				EntityPlayer player = MainRegistry.proxy.me();
				if(player == null) return;
				player.motionX += message.motionX;
				player.motionY += message.motionY;
				player.motionZ += message.motionZ;
			});
			return null;
		}
	}
}
