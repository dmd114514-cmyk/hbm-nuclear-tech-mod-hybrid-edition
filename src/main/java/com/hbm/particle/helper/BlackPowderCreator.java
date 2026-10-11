package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.particle.ParticleBlackPowderSmoke;
import com.hbm.particle.ParticleBlackPowderSpark;
import com.hbm.util.Vec3NT;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The black powder's bang: one direction-stretched smoke puff + a handful of sparks.
 *
 * The direction is computed server-side and packed (hX/hY/hZ); the client only normalizes and multiplies the speed factor;
 * smoke and sparks take cloudSpeedMult / sparkSpeedMult respectively — their overlay is what gives the "slow smoke, fast sparks" layering.
 */
public class BlackPowderCreator implements IParticleCreator {

	public static void composeEffect(World world, double x, double y, double z, double headingX, double headingY, double headingZ, int cloudCount, float cloudScale, float cloudSpeedMult, int sparkCount, float sparkSpeedMult) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "blackPowder");
		data.setInteger("cloudCount", cloudCount);
		data.setFloat("cloudScale", cloudScale);
		data.setFloat("cloudSpeedMult", cloudSpeedMult);
		data.setInteger("sparkCount", sparkCount);
		data.setFloat("sparkSpeedMult", sparkSpeedMult);
		data.setDouble("hX", headingX);
		data.setDouble("hY", headingY);
		data.setDouble("hZ", headingZ);
		IParticleCreator.sendPacket(world, x, y, z, 200, data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		double headingX = data.getDouble("hX");
		double headingY = data.getDouble("hY");
		double headingZ = data.getDouble("hZ");
		int cloudCount = data.getInteger("cloudCount");
		float cloudScale = data.getFloat("cloudScale");
		float cloudSpeedMult = data.getFloat("cloudSpeedMult");
		int sparkCount = data.getInteger("sparkCount");
		float sparkSpeedMult = data.getFloat("sparkSpeedMult");

		Vec3NT heading = new Vec3NT(headingX, headingY, headingZ).normalizeSelf();

		for(int i = 0; i < cloudCount; i++) {
			double speedMult = 0.85 + rand.nextDouble() * 0.3;
			ParticleBlackPowderSmoke particle = new ParticleBlackPowderSmoke(world, x, y, z, cloudScale)
					.setMotion(heading.x * cloudSpeedMult * speedMult + rand.nextGaussian() * 0.05,
							heading.y * cloudSpeedMult * speedMult + rand.nextGaussian() * 0.05,
							heading.z * cloudSpeedMult * speedMult + rand.nextGaussian() * 0.05);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}

		for(int i = 0; i < sparkCount; i++) {
			double speedMult = 0.85 + rand.nextDouble() * 0.3;
			ParticleBlackPowderSpark particle = new ParticleBlackPowderSpark(world, x, y, z,
					heading.x * sparkSpeedMult * speedMult + rand.nextGaussian() * 0.02,
					heading.y * sparkSpeedMult * speedMult + rand.nextGaussian() * 0.02,
					heading.z * sparkSpeedMult * speedMult + rand.nextGaussian() * 0.02);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}
}
