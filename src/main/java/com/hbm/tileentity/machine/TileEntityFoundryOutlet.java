package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.machine.FoundryOutlet;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.network.AsyncPacketSender;
import com.hbm.network.packet.AuxParticlePacketNT;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.material.MaterialStack;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * The casting outlet: the channel's end, pouring material down onto the cell in front of and below it.
 *
 * It stores nothing itself (capacity 0) and only does "filter + gate + transfer". Intake happens on the back (opposite the front);
 * the front is left for the falling stream of metal; redstone and the scrap in hand decide whether it's open or plugged.
 */
public class TileEntityFoundryOutlet extends TileEntityFoundryBase {

	/** Only lets this one material through; with invertFilter it becomes "only blocks this one" */
	public NTMMaterial filter = null;
	public boolean invertFilter = false;
	/** Inverts the redstone behavior: when true it sits plugged by default and opens on signal */
	public boolean invertRedstone = false;

	/** These two feed only the client's redraw check, never the save */
	protected NTMMaterial lastFilter = null;
	protected boolean lastClosed = false;

	/** Blocks every metal stream; renders as a small barrier */
	public boolean isClosed() {
		return invertRedstone ^ (world.isBlockIndirectlyGettingPowered(pos) > 0);
	}

	/** The metal stream hangs from the outlet's lower edge, reaching at most four cells down */
	private static Vec3d streamStart(BlockPos pos) {
		return new Vec3d(pos.getX() + 0.5, pos.getY() - 0.125, pos.getZ() + 0.5);
	}

	private static Vec3d streamEnd(BlockPos pos) {
		return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.125 - 4, pos.getZ() + 0.5);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(world.isRemote) {
			boolean closed = isClosed();
			if(this.lastClosed != closed || this.filter != this.lastFilter) {
				this.lastFilter = this.filter;
				this.lastClosed = closed;
				refresh();
			}
		}
	}

	@Override public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) { return false; }
	@Override public MaterialStack<?> pour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) { return stack; }

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {

		if(filter != null && ((filter != materialOf(stack)) ^ invertFilter)) return false;
		if(isClosed()) return false;
		if(side != getNozzle(world, pos).getOpposite()) return false;

		BlockPos here = new BlockPos(x, y, z);
		RayTraceResult[] holder = new RayTraceResult[1];
		ICrucibleAcceptor acc = ICrucibleAcceptor.pouringTarget(world, streamStart(here), streamEnd(here), holder);

		if(acc == null) {
			return false;
		}

		RayTraceResult hit = holder[0];

		return acc.canAcceptPartialPour(world, hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ(), hit.hitVec.x, hit.hitVec.y, hit.hitVec.z, EnumFacing.UP, stack);
	}

	@Override
	public MaterialStack<?> flow(World world, int x, int y, int z, EnumFacing side, MaterialStack<?> stack) {

		BlockPos here = new BlockPos(x, y, z);
		RayTraceResult[] holder = new RayTraceResult[1];
		ICrucibleAcceptor acc = ICrucibleAcceptor.pouringTarget(world, streamStart(here), streamEnd(here), holder);

		if(acc == null)
			return stack;

		RayTraceResult hit = holder[0];
		MaterialStack<?> didPour = acc.pour(world, hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ(), hit.hitVec.x, hit.hitVec.y, hit.hitVec.z, EnumFacing.UP, stack);

		if(!world.isRemote) {
			emitStream(hit.getBlockPos(), side.getOpposite(), stack);
		}

		return didPour;
	}

	/**
	 * The metal stream is pure presentation, so it only sends packets and never draws locally. dir says "which side of the facing the stream hangs on",
	 * and the effect side reads by the same numbering; field names and values match the old version.
	 */
	private void emitStream(BlockPos target, EnumFacing dir, MaterialStack<?> stack) {

		NBTTagCompound data = new NBTTagCompound();
		data.setString("type", "foundry");
		data.setInteger("color", materialOf(stack).moltenColor);
		data.setByte("dir", (byte) dir.ordinal());
		data.setFloat("off", 0.375F);
		data.setFloat("base", 0F);
		data.setFloat("len", Math.max(1F, pos.getY() - (float) (Math.ceil(target.getY() + 1D) - 0.875)));

		AsyncPacketSender.createAllAroundThreadedPacket(
				new AuxParticlePacketNT(data, pos.getX() + 0.5D - dir.getFrontOffsetX() * 0.125, pos.getY() + 0.125, pos.getZ() + 0.5D - dir.getFrontOffsetZ() * 0.125),
				new TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 50));
	}

	/** Which way the front faces is the block property's job; the old version squeezed it into meta */
	public static EnumFacing getNozzle(World world, BlockPos pos) {
		return world.getBlockState(pos).getValue(FoundryOutlet.FACING);
	}

	@Override
	public int getCapacity() {
		return 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.invertRedstone = nbt.getBoolean("invert");
		this.invertFilter = nbt.getBoolean("invertFilter");
		this.filter = Mats.matById.get((int) nbt.getShort("filter"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setBoolean("invert", this.invertRedstone);
		nbt.setBoolean("invertFilter", this.invertFilter);
		nbt.setShort("filter", this.filter == null ? (short) -1 : (short) this.filter.id);
		return nbt;
	}

	@Override
	public NBTTagCompound getSettings(World world, BlockPos pos) {
		NBTTagCompound nbt = new NBTTagCompound();

		nbt.setBoolean("invert", this.invertRedstone);
		nbt.setBoolean("invertFilter", this.invertFilter);
		if(filter != null) {
			nbt.setIntArray("matFilter", new int[]{ filter.id });
		}

		return nbt;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, BlockPos pos) {

		if(nbt.hasKey("invert")) this.invertRedstone = nbt.getBoolean("invert");
		if(nbt.hasKey("invertFilter")) this.invertFilter = nbt.getBoolean("invertFilter");
		if(nbt.hasKey("matFilter")) {
			int[] ids = nbt.getIntArray("matFilter");
			if(ids.length > 0 && index < ids.length)
				this.filter = Mats.matById.get(ids[index]);
		}
	}

	@Override
	public String[] infoForDisplay(World world, BlockPos pos) {
		List<String> info = new ArrayList<String>();
		info.add("copytool.invertRedstone");
		info.add("copytool.invertFilter");
		if(filter != null) info.add(filter.getUnlocalizedName());
		return info.toArray(new String[0]);
	}
}
