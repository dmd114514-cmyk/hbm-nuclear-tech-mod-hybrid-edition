package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The effect's server-side packet door + client-side landing door.
 *
 * Splitting it into its own class has two benefits: no more registration branches in the already-bloated ClientProxy,
 * and one effect can get as messy as it likes without dragging other particles down.
 *
 * Packets ride A2's async facade. The contract is **queued means read-only snapshot**: packet data must be fully filled on the main thread before entering the queue,
 * with TargetPoint carrying just the dimension id and a few doubles — never BlockPos or entity references, hoping the sender thread reads the world.
 */
public interface IParticleCreator {

	@SideOnly(Side.CLIENT)
	void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data);

	// The particle packet fills NBT and coordinates in place, satisfying "queued means snapshot" exactly; no deferred world reads to worry about
	static void sendPacket(World world, double x, double y, double z, int range, NBTTagCompound data) {
		AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), x, y, z, range));
	}
}
