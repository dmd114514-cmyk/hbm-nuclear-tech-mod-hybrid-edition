package com.hbm.tileentity.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.util.ColorUtil;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

/**
 * The base for every pylon/connector: manages "who I'm connected to and in what color"; rendering and networking both take their data from here.
 * Connections are double-entry bookkeeping per pylon pair; either side initiates the teardown.
 *
 * The connection table doubles as the grid's "long-distance direct" edges: while laying, the net uses them to merge two towers into one,
 * so towers never register their own nodes; after changing connections, one shout of refreshNet is enough.
 */
public abstract class TileEntityPylonBase extends TileEntityCableBaseNT {

	protected List<int[]> connected = new ArrayList<>();
	public int color;

	public static int canConnect(TileEntityPylonBase first, TileEntityPylonBase second) {

		if(first.getConnectionType() != second.getConnectionType())
			return 1;

		if(first == second)
			return 2;

		double len = Math.min(first.getMaxWireLength(), second.getMaxWireLength());

		Vec3d firstPos = first.getConnectionPoint();
		Vec3d secondPos = second.getConnectionPoint();

		Vec3d delta = new Vec3d(
				(secondPos.x) - (firstPos.x),
				(secondPos.y) - (firstPos.y),
				(secondPos.z) - (firstPos.z));

		return len >= delta.lengthVector() ? 0 : 3;
	}

	public boolean setColor(ItemStack stack) {
		if(stack.isEmpty()) return false;
		int color = ColorUtil.getColorFromDye(stack);
		if(color == 0 || color == this.color) return false;
		stack.shrink(1);
		this.color = color;

		this.markDirty();
		this.syncToClient();

		return true;
	}

	@Override
	public List<int[]> getExtraNetLinks() {
		return connected;
	}

	public void addConnection(int x, int y, int z) {

		connected.add(new int[] {x, y, z});

		this.markDirty();
		this.syncToClient();
		this.refreshNet();
	}

	public void disconnectAll() {

		for(int[] pos : connected) {

			TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

			if(te == this)
				continue;

			if(te instanceof TileEntityPylonBase) {
				((TileEntityPylonBase) te).removeLink(getPos());
			}
		}

		connected.clear();

		this.markDirty();
		this.syncToClient();
		this.refreshNet();
	}

	/** The far tower takes us off its connection table; each side re-lays its own net */
	private void removeLink(BlockPos origin) {

		for(int i = 0; i < connected.size(); i++) {
			int[] conPos = connected.get(i);

			if(conPos[0] == origin.getX() && conPos[1] == origin.getY() && conPos[2] == origin.getZ()) {
				connected.remove(i);
				i--;
			}
		}

		this.markDirty();
		this.syncToClient();
		this.refreshNet();
	}

	public abstract ConnectionType getConnectionType();
	public abstract Vec3d[] getMountPos();
	public abstract double getMaxWireLength();

	public Vec3d getConnectionPoint() {
		Vec3d[] mounts = this.getMountPos();

		if(mounts == null || mounts.length == 0)
			return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

		return mounts[0].addVector(pos.getX(), pos.getY(), pos.getZ());
	}

	public List<int[]> getConnected() {
		return connected;
	}

	/** When connections or color change, push an NBT copy to clients; the wire colors ride entirely on this sync */
	private void syncToClient() {
		if(world instanceof WorldServer) {
			((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(getPos());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		int count = nbt.getInteger("conCount");
		this.color = nbt.getInteger("color");

		this.connected.clear();

		for(int i = 0; i < count; i++) {
			connected.add(nbt.getIntArray("con" + i));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("conCount", connected.size());
		nbt.setInteger("color", color);

		for(int i = 0; i < connected.size(); i++) {
			nbt.setIntArray("con" + i, connected.get(i));
		}

		return nbt;
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.getPos(), 0, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.readFromNBT(pkt.getNbtCompound());
	}

	public enum ConnectionType {
		SINGLE,
		TRIPLE,
		QUAD
		//more to follow
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}

	@Override
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}
