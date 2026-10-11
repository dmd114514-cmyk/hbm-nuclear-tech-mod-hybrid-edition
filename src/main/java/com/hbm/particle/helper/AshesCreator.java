package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.main.ClientProxy;
import com.hbm.particle.ParticleAshes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Burns an entity into ash piles, each crowned with a small flame: the packet carries only the entity id; position is computed on the client from the entity at that moment.
 *
 * The old version newed an EntityFlameFX and fed it to the renderer; its constructor is protected here,
 * and vanilla's own door spawnParticle(FLAME, …) gives the same result.
 * The flame-follows-ash position is computed here and used twice — Particle.posX is protected in this version, unreadable from outside.
 */
public class AshesCreator implements IParticleCreator {

	public static void composeEffect(World world, Entity toPulverize, int ashesCount, float ashesScale) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "ashes");
		data.setInteger("entityID", toPulverize.getEntityId());
		data.setInteger("ashesCount", ashesCount);
		data.setFloat("ashesScale", ashesScale);
		IParticleCreator.sendPacket(world, toPulverize.posX, toPulverize.posY, toPulverize.posZ, 100, data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		int entityID = data.getInteger("entityID");
		Entity entity = world.getEntityByID(entityID);
		if(entity == null) return;

		ClientProxy.vanish(entityID);

		int amount = data.getInteger("ashesCount");
		float scale = data.getFloat("ashesScale");

		for(int i = 0; i < amount; i++) {
			double ashX = entity.posX + (entity.width + scale * 2) * (rand.nextDouble() - 0.5);
			double ashY = entity.posY + entity.height * rand.nextDouble();
			double ashZ = entity.posZ + (entity.width + scale * 2) * (rand.nextDouble() - 0.5);

			ParticleAshes particle = new ParticleAshes(world, ashX, ashY, ashZ, scale);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
			world.spawnParticle(EnumParticleTypes.FLAME, ashX, ashY, ashZ, 0D, 0D, 0D);
		}
	}
}
