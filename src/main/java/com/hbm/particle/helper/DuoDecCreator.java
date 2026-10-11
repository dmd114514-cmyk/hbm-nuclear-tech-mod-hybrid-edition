package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.particle.ParticleDuoDec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The two-tone minus family (the old version fumbled the names: duoewe spawns DuoDec, duodec spawns DuoDec2 — each type pointing at the other's particle).
 *
 * Carries particle-count gates (the game option "minimal" skips drawing entirely, "decreased" draws half the time) — the old version really had
 * these two gates, unlike the earlier few; not an omission.
 */
public class DuoDecCreator implements IParticleCreator {

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;

		if(particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean())) {
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDuoDec(texman, world, x, y, z));
		}
	}
}
