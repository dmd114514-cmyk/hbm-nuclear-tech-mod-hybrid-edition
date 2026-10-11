package com.hbm.tileentity;

import io.netty.buffer.ByteBuf;

/**
 * The payload codec reserved for sync packets: data packed compactly into a ByteBuf,
 * sent by the network layer's packet shell; the TE never worries about transport details.
 */
public interface IBufPacketReceiver {

	public void serialize(ByteBuf buf);
	public void deserialize(ByteBuf buf);
}
