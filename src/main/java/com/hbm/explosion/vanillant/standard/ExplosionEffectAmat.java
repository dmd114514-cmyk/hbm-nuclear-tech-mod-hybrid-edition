package com.hbm.explosion.vanillant.standard;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.interfaces.IExplosionSFX;
import com.hbm.main.NTMSounds;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;
import com.hbm.sound.SoundRefs;

import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class ExplosionEffectAmat implements IExplosionSFX {

	@Override
	public void doEffect(ExplosionVNT explosion, World world, double x, double y, double z, float size) {

		if(size < 15)
			world.playSound(null, x, y, z, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.4F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
		else
			world.playSound(null, x, y, z, SoundRefs.resolve(new ResourceLocation(NTMSounds.GUN_MINI_NUKE_EXPLOSION)), SoundCategory.BLOCKS, 15.0F, 1.0F);

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "amat");
		data.setFloat("scale", size);
		AsyncPacketSender.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, x, y, z), new TargetPoint(world.provider.getDimension(), x, y, z, 200));
	}
}
