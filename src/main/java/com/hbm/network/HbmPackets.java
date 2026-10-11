package com.hbm.network;

import com.hbm.network.packet.AuxParticlePacketNT;
import com.hbm.network.packet.ExplosionAffectedBlocksPacket;
import com.hbm.network.packet.ExplosionKnockbackPacket;
import com.hbm.network.packet.GuiLayerPacket;
import com.hbm.network.packet.KeybindPacket;
import com.hbm.network.packet.NbtControlPacket;
import com.hbm.network.packet.PlayerInformPacket;
import com.hbm.network.packet.TESirenPacket;
import com.hbm.network.packet.TESwitchPacket;

import net.minecraftforge.fml.relauncher.Side;

/**
 * The packet registry. Every packet registers through this one method, discriminators assigned by auto-increment,
 * so **new entries can only go at the end**: inserting mid-list shifts packet ids for existing saves/clients.
 */
public final class HbmPackets {

	private HbmPackets() { }

	public static void registerAll() {
		NetworkHandler.register(AuxParticlePacketNT.Handler.class, AuxParticlePacketNT.class, Side.CLIENT);
		NetworkHandler.register(PlayerInformPacket.Handler.class, PlayerInformPacket.class, Side.CLIENT);
		NetworkHandler.register(TESirenPacket.Handler.class, TESirenPacket.class, Side.CLIENT);
		NetworkHandler.register(ExplosionKnockbackPacket.Handler.class, ExplosionKnockbackPacket.class, Side.CLIENT);
		NetworkHandler.register(ExplosionAffectedBlocksPacket.Handler.class, ExplosionAffectedBlocksPacket.class, Side.CLIENT);
		NetworkHandler.register(TESwitchPacket.Handler.class, TESwitchPacket.class, Side.CLIENT);
		NetworkHandler.register(NbtControlPacket.Handler.class, NbtControlPacket.class, Side.SERVER);
		NetworkHandler.register(GuiLayerPacket.Handler.class, GuiLayerPacket.class, Side.SERVER);
		NetworkHandler.register(KeybindPacket.Handler.class, KeybindPacket.class, Side.SERVER);
	}
}
