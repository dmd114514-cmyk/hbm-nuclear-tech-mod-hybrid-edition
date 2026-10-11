package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.particle.ParticleDuoDec2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** type says duodec but draws DuoDec2 (the old version crossed these names and it can't be fixed); same gates as DuoDecCreator */
public class DuoDec2Creator implements IParticleCreator {

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;

		if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean())) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDuoDec2(texman, world, x, y, z));
		}
	}
}
