package com.hbm.network.packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.main.MainRegistry;
import com.hbm.network.NetworkHandler;
import com.hbm.network.PacketBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sends the list of blocks an explosion touched to nearby clients; debris and smoke are drawn by each client itself.
 * Block coordinates go out as offsets from the blast center, one byte each — the whole packet is one byte per cell, cheap enough.
 */
public class ExplosionAffectedBlocksPacket extends PacketBase {

	private double posX;
	private double posY;
	private double posZ;
	private float size;
	private List<BlockPos> affectedBlocks = new ArrayList<BlockPos>();

	public ExplosionAffectedBlocksPacket() { }

	public ExplosionAffectedBlocksPacket(double x, double y, double z, float size, List<BlockPos> blocks) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.size = size;
		this.affectedBlocks = new ArrayList<BlockPos>(blocks);
	}

	@Override
	protected void serialize(ByteBuf buf) throws IOException {
		buf.writeFloat((float) this.posX);
		buf.writeFloat((float) this.posY);
		buf.writeFloat((float) this.posZ);
		buf.writeFloat(this.size);
		buf.writeInt(this.affectedBlocks.size());

		int ox = (int) this.posX;
		int oy = (int) this.posY;
		int oz = (int) this.posZ;

		for(BlockPos pos : this.affectedBlocks) {
			buf.writeByte(pos.getX() - ox);
			buf.writeByte(pos.getY() - oy);
			buf.writeByte(pos.getZ() - oz);
		}
	}

	@Override
	protected void deserialize(ByteBuf buf) throws IOException {
		this.posX = (double) buf.readFloat();
		this.posY = (double) buf.readFloat();
		this.posZ = (double) buf.readFloat();
		this.size = buf.readFloat();

		int count = buf.readInt();
		this.affectedBlocks = new ArrayList<BlockPos>(count);

		int ox = (int) this.posX;
		int oy = (int) this.posY;
		int oz = (int) this.posZ;

		for(int i = 0; i < count; ++i) {
			this.affectedBlocks.add(new BlockPos(buf.readByte() + ox, buf.readByte() + oy, buf.readByte() + oz));
		}
	}

	public static class Handler implements IMessageHandler<ExplosionAffectedBlocksPacket, IMessage> {

		@Override
		public IMessage onMessage(ExplosionAffectedBlocksPacket message, MessageContext ctx) {

			NetworkHandler.scheduleClient(() -> {
				EntityPlayer player = MainRegistry.proxy.me();
				if(player == null || player.world == null) return;
				ExplosionEffectStandard.performClient(player.world, message.posX, message.posY, message.posZ, message.size, message.affectedBlocks);
			});
			return null;
		}
	}
}
