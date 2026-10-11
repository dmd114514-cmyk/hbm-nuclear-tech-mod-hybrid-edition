package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.particle.ParticleDust;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The dust storm: a puff of floating dust with initial velocity; scale is the caller's (no fallback here, none in the old version either) */
public class DustStormCreator implements IParticleCreator {

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		double mX = data.getDouble("mX");
		double mY = data.getDouble("mY");
		double mZ = data.getDouble("mZ");
		float scale = data.getFloat("scale");

		ParticleDust particle = new ParticleDust(world, x, y, z, mX, mY, mZ, scale);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}
}
