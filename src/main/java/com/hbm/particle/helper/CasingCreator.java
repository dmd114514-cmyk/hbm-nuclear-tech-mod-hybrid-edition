package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.main.MainRegistry;
import com.hbm.particle.ParticleSpentCasing;
import com.hbm.particle.SpentCasing;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The eject effect's creator: the server computes ejection direction and tumble angles and sends them; the client looks up the shell config by registry name and draws.
 *
 * That "pitch about X first, then yaw about Y" order stays untouched. 1.7.10's Vec3 had
 * rotateAroundX/rotateAroundY; this version's Vec3d offers rotatePitch/rotateYaw with identical formulas,
 * so the angles pass through unchanged, no sign flips.
 */
public class CasingCreator implements IParticleCreator {

	private static final double TO_RADIANS = Math.PI / 180.0F;

	/** Default casing without smoke */
	public static void composeEffect(World world, EntityLivingBase player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, String casing) {
		composeEffect(world, player, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance, 5F, 10F, casing, false, 0, 0, 0);
	}

	/** Casing without smoke */
	public static void composeEffect(World world, EntityLivingBase player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, float multPitch, float multYaw, String casing) {
		composeEffect(world, player, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance, multPitch, multYaw, casing, false, 0, 0, 0);
	}

	/** Default casing, but with smoke */
	// The old version dropped the four smoking parameters on that forwarding tier, making the smoking branch dead weight; wired here per the comment's intent
	public static void composeEffect(World world, EntityLivingBase player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
		composeEffect(world, player, frontOffset, heightOffset, sideOffset, frontMotion, heightMotion, sideMotion, motionVariance, 5F, 10F, casing, smoking, smokeLife, smokeLift, nodeLife);
	}

	public static void composeEffect(World world, double x, double y, double z, float yaw, float pitch, double frontMotion, double heightMotion, double sideMotion, double motionVariance, float mPitch, float mYaw, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {

		Vec3d motion = new Vec3d(sideMotion, heightMotion, frontMotion)
				.rotatePitch(-pitch * (float) TO_RADIANS)
				.rotateYaw(-yaw * (float) TO_RADIANS);

		double mX = motion.x + world.rand.nextGaussian() * motionVariance;
		double mY = motion.y + world.rand.nextGaussian() * motionVariance;
		double mZ = motion.z + world.rand.nextGaussian() * motionVariance;

		send(world, x, y, z, mX, mY, mZ, yaw, pitch, mPitch, mYaw, casing, smoking, smokeLife, smokeLift, nodeLife);
	}

	public static void composeEffect(World world, EntityLivingBase player, double frontOffset, double heightOffset, double sideOffset, double frontMotion, double heightMotion, double sideMotion, double motionVariance, float mPitch, float mYaw, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {

		if(player.isSneaking()) heightOffset -= 0.075F;

		Vec3d offset = new Vec3d(sideOffset, heightOffset, frontOffset)
				.rotatePitch(-player.rotationPitch * (float) TO_RADIANS)
				.rotateYaw(-player.rotationYaw * (float) TO_RADIANS);

		double x = player.posX + offset.x;
		double y = player.posY + player.getEyeHeight() + offset.y;
		double z = player.posZ + offset.z;

		Vec3d motion = new Vec3d(sideMotion, heightMotion, frontMotion)
				.rotatePitch(-player.rotationPitch * (float) TO_RADIANS)
				.rotateYaw(-player.rotationYaw * (float) TO_RADIANS);

		double mX = player.motionX + motion.x + player.getRNG().nextGaussian() * motionVariance;
		double mY = player.motionY + motion.y + player.getRNG().nextGaussian() * motionVariance;
		double mZ = player.motionZ + motion.z + player.getRNG().nextGaussian() * motionVariance;

		if(player instanceof EntityPlayer && ((EntityPlayer) player).capabilities.isFlying) mY -= 0.04D;

		send(world, x, y, z, mX, mY, mZ, player.rotationYaw, player.rotationPitch, mPitch, mYaw, casing, smoking, smokeLife, smokeLift, nodeLife);
	}

	// Both the local and remote paths just pack one NBT and send it; only how coordinates and angles are computed differs
	private static void send(World world, double x, double y, double z, double mX, double mY, double mZ, float yaw, float pitch, float mPitch, float mYaw, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "casingNT");
		data.setDouble("mX", mX);
		data.setDouble("mY", mY);
		data.setDouble("mZ", mZ);
		data.setFloat("yaw", yaw);
		data.setFloat("pitch", pitch);
		data.setFloat("mPitch", mPitch);
		data.setFloat("mYaw", mYaw);
		data.setString("name", casing);
		data.setBoolean("smoking", smoking);
		data.setInteger("smokeLife", smokeLife);
		data.setDouble("smokeLift", smokeLift);
		data.setInteger("nodeLife", nodeLife);

		IParticleCreator.sendPacket(world, x, y, z, 50, data);
	}

	/** Already on the client means dispatch locally, no need to loop through the network */
	public static void composeEffectClient(World world, double x, double y, double z, double mX, double mY, double mZ, float yaw, float pitch, float mPitch, float mYaw, String casing, boolean smoking, int smokeLife, double smokeLift, int nodeLife) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "casingNT");
		data.setDouble("mX", mX);
		data.setDouble("mY", mY);
		data.setDouble("mZ", mZ);
		data.setFloat("yaw", yaw);
		data.setFloat("pitch", pitch);
		data.setFloat("mPitch", mPitch);
		data.setFloat("mYaw", mYaw);
		data.setString("name", casing);
		data.setBoolean("smoking", smoking);
		data.setInteger("smokeLife", smokeLife);
		data.setDouble("smokeLift", smokeLift);
		data.setInteger("nodeLife", nodeLife);
		data.setDouble("posX", x);
		data.setDouble("posY", y);
		data.setDouble("posZ", z);
		MainRegistry.proxy.effectNT(data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		// The name arrives in the packet; if the registry doesn't know it, don't draw: this sits on the receive path, and a throw here crashes the client
		SpentCasing casingConfig = SpentCasing.casingMap.get(data.getString("name"));
		if(casingConfig == null) return;

		ParticleSpentCasing casing = new ParticleSpentCasing(texman, world, x, y, z,
				data.getDouble("mX"), data.getDouble("mY"), data.getDouble("mZ"),
				data.getFloat("mPitch"), data.getFloat("mYaw"),
				casingConfig, data.getBoolean("smoking"), data.getInteger("smokeLife"),
				data.getDouble("smokeLift"), data.getInteger("nodeLife"));
		casing.setRotation(data.getFloat("yaw"), data.getFloat("pitch"));

		Minecraft.getMinecraft().effectRenderer.addEffect(casing);
	}
}
