package com.hbm.tileentity;

import com.hbm.sound.AudioWrapper;
import com.hbm.util.fauxpointtwelve.BlockPos;

import api.hbm.tile.ILoadedTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * The common base for tile entities: chunk-load flag, looped audio, the muffled/tilted persisted pair,
 * and the compact payload codec reserved for sync packets.
 */
public class TileEntityLoadedBase extends TileEntity implements ILoadedTile, IBufPacketReceiver {

	public boolean isLoaded = true;
	public boolean muffled = false;
	public boolean tilted = false;

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		this.isLoaded = false;
	}

	/** The "chunk contents changed, remember to save me" half of markDirty, without a block update */
	public void markChanged() {
		this.world.markChunkDirty(this.pos, this);
	}

	public AudioWrapper createAudioLoop() {
		return null;
	}

	public AudioWrapper rebootAudio(AudioWrapper wrapper) {
		wrapper.stopSound();
		AudioWrapper audio = createAudioLoop();
		audio.startSound();
		return audio;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.muffled = nbt.getBoolean("muffled");
		this.tilted = nbt.getBoolean("tilted");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("muffled", muffled);
		nbt.setBoolean("tilted", tilted);
		return nbt;
	}

	public float getVolume(float baseVolume) {
		return muffled ? baseVolume * 0.1F : baseVolume;
	}

	@Override
	public void serialize(ByteBuf buf) {
		buf.writeBoolean(muffled);
		buf.writeBoolean(tilted);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.muffled = buf.readBoolean();
		this.tilted = buf.readBoolean();
	}

	public int getFloorCount() {
		return 0;
	}

	public BlockPos getFloorPosFromIndex(int index) {
		return null;
	}

	public BlockPos standardFloor3x3(int index) {
		return new BlockPos(pos.getX() - 1 + (index / 2) * 2, pos.getY() - 1, pos.getZ() - 1 + (index % 2) * 2);
	}

	public BlockPos standardFloor5x5(int index) {
		return new BlockPos(pos.getX() - 2 + (index / 3) * 2, pos.getY() - 1, pos.getZ() - 2 + (index % 3) * 2);
	}

	public BlockPos standardFloor7x7(int index) {
		return new BlockPos(pos.getX() - 3 + (index / 4) * 2, pos.getY() - 1, pos.getZ() - 3 + (index % 4) * 2);
	}
}
