package com.hbm.sound;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

@SideOnly(Side.CLIENT)
public class AudioDynamic extends MovingSound {

	public float maxVolume = 1;
	public float range;
	public int keepAlive;
	public int timeSinceKA;
	public boolean shouldExpire = false;
	// Loop sounds attached to an entity (tools, guns) track position through this: attached to the local player means volume pinned at max, so the channels don't fight over 0,0,0
	public Entity parentEntity = null;

	protected AudioDynamic(ResourceLocation loc) {
		super(SoundRefs.resolve(loc), SoundCategory.PLAYERS);
		this.repeat = true;
		this.attenuationType = ISound.AttenuationType.NONE;
		this.range = 10;
	}

	public void setPosition(float x, float y, float z) {
		this.xPosF = x;
		this.yPosF = y;
		this.zPosF = z;
	}

	public void attachTo(Entity e) {
		this.parentEntity = e;
	}

	@Override
	public void update() {

		EntityPlayerSP player = Minecraft.getMinecraft().player;
		float f = 0;

		if(parentEntity != null && player != parentEntity) {
			this.setPosition((float) parentEntity.posX, (float) parentEntity.posY, (float) parentEntity.posZ);
		}

		// Only sources not attached to the local player fall off with distance
		if(player != null && player != parentEntity) {
			f = (float) Math.sqrt(Math.pow(getXPosF() - player.posX, 2) + Math.pow(getYPosF() - player.posY, 2) + Math.pow(getZPosF() - player.posZ, 2));
			volume = volumeAtDistance(f);
		} else {
			if(player == parentEntity) this.setPosition((float) parentEntity.posX, (float) parentEntity.posY + 10, (float) parentEntity.posZ);
			volume = maxVolume;
		}

		if(this.shouldExpire) {

			if(this.timeSinceKA > this.keepAlive) {
				this.stop();
			}

			this.timeSinceKA++;
		}
	}

	public void start() {
		Minecraft.getMinecraft().getSoundHandler().playSound(this);
	}

	public void stop() {
		Minecraft.getMinecraft().getSoundHandler().stopSound(this);
	}

	public void setVolume(float volume) {
		this.maxVolume = volume;
	}

	public void setRange(float range) {
		this.range = range;
	}

	public void setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
		this.shouldExpire = true;
	}

	public void keepAlive() {
		this.timeSinceKA = 0;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public float volumeAtDistance(float dist) {
		return (dist / range) * -maxVolume + maxVolume;
	}

	public boolean isPlaying() {
		return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this);
	}

	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
}
