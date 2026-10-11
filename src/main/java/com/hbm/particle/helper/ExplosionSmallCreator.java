package com.hbm.particle.helper;

import java.util.Random;

import com.hbm.particle.ParticleExplosionSmall;
import com.hbm.sound.SoundRefs;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The small explosion's (bullets, rockets — the no-crater kind) creator: one dust cloud + a distance-picked sound + a handful of debris when it grazes a block.
 *
 * Two notes on the debris:
 *   · the old version reflection-wrote particleMaxAge; Particle.setMaxAge is public now, no reflection needed;
 *   · the BLOCK_DUST factory reads the first integer after the coordinates as a **block state id** ({@code Block.getStateById}),
 *     so what's passed is {@code Block.getStateId(state)} — not an item id, not the old "block + meta" pair —
 *     a wrong value doesn't error, it just silently draws in some other color.
 * Both sounds declare block category in sounds.json, so the category follows; the old 100F volume was deliberate loudness, copied.
 */
public class ExplosionSmallCreator implements IParticleCreator {

	public static final double speedOfSound = (17.15D) * 0.5;

	public static void composeEffect(World world, double x, double y, double z, int cloudCount, float cloudScale, float cloudSpeedMult) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "explosionSmall");
		data.setInteger("cloudCount", cloudCount);
		data.setFloat("cloudScale", cloudScale);
		data.setFloat("cloudSpeedMult", cloudSpeedMult);
		data.setInteger("debris", 15);
		IParticleCreator.sendPacket(world, x, y, z, 200, data);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {

		int cloudCount = data.getInteger("cloudCount");
		float cloudScale = data.getFloat("cloudScale");
		float cloudSpeedMult = data.getFloat("cloudSpeedMult");
		int debris = data.getInteger("debris");

		float dist = (float) player.getDistance(x, y, z);
		float soundRange = 200F;

		if(dist <= soundRange) {
			String sound = dist <= soundRange * 0.4 ? "hbm:weapon.explosionsmallnear" : "hbm:weapon.explosionsmallfar";
			PositionedSoundRecord record = new PositionedSoundRecord(SoundRefs.resolve(new ResourceLocation(sound)),
					SoundCategory.BLOCKS, 100F, 0.9F + rand.nextFloat() * 0.2F, (float) x, (float) y, (float) z);
			Minecraft.getMinecraft().getSoundHandler().playDelayedSound(record, (int) (dist / speedOfSound));
		}

		for(int i = 0; i < cloudCount; i++) {
			ParticleExplosionSmall particle = new ParticleExplosionSmall(world, x, y, z, cloudScale, cloudSpeedMult);
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}

		IBlockState state = Blocks.AIR.getDefaultState();

		BlockPos center = new BlockPos(x, y, z);

		for(EnumFacing dir : EnumFacing.values()) {
			IBlockState probe = world.getBlockState(center.offset(dir));
			if(probe.getBlock() != Blocks.AIR) {
				state = probe;
				break;
			}
		}

		if(state.getBlock() != Blocks.AIR) for(int i = 0; i < debris; i++) {
			Particle fx = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(),
					x, y + 0.1, z,
					world.rand.nextGaussian() * 0.2, 0.5D + world.rand.nextDouble() * 0.7, world.rand.nextGaussian() * 0.2,
					Block.getStateId(state));
			if(fx != null) {
				fx.multipleParticleScaleBy(2);
				fx.setMaxAge(50 + rand.nextInt(20));
			}
		}
	}
}
