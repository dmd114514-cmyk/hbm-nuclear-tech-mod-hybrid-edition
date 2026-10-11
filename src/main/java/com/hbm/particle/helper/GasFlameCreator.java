package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.particle.ParticleGasFlame;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The gas flame column: spout position + initial velocity + scale. Senders are util/ParticleUtil and two tiles (the turbocharger and the geyser),
 * this only places it on the client.
 *
 * scale 0 falls back to 6.5 in the old version (those callers never passed a scale, they just wanted a normal column of fire); copied.
 */
public class GasFlameCreator implements IParticleCreator {

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		double mX = data.getDouble("mX");
		double mY = data.getDouble("mY");
		double mZ = data.getDouble("mZ");
		float scale = data.getFloat("scale");

		ParticleGasFlame particle = new ParticleGasFlame(world, x, y, z, mX, mY, mZ, scale > 0 ? scale : 6.5F);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}
}
