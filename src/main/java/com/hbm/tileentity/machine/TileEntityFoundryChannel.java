package com.hbm.tileentity.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.blocks.machine.ModBlocksMachine;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.material.MaterialStack;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The casting channel: carries material from tanks or the crucible all the way to outlets and molds.
 *
 * Feeds non-channel receivers first; only when none will take it does it split to horizontally adjacent channels. The direction it just fed from goes last in line,
 * so material doesn't slosh back and forth. A whole connected channel carries exactly one material, so pouring asks the whole chain first.
 */
public class TileEntityFoundryChannel extends TileEntityFoundryBase {

	/** A chain scans at most this many cells: pouring is a player action and small by nature; anything longer is treated as the cap */
	private static final int NET_SCAN_LIMIT = 128;

	public int nextUpdate;
	/** Which direction last fed us, stored as the facing's ordinal */
	public int lastFlow = 0;

	@Override
	public void updateEntity() {

		if(!world.isRemote) {

			if(this.type == null && this.amount != 0) {
				this.amount = 0;
			}

			nextUpdate--;

			if(nextUpdate <= 0 && this.amount > 0 && this.type != null) {

				boolean hasOp = false;
				nextUpdate = 5;

				List<EnumFacing> dirs = new ArrayList<EnumFacing>(Arrays.asList(EnumFacing.HORIZONTALS));
				Collections.shuffle(dirs, world.rand);

				if(lastFlow > 0) {
					EnumFacing prefer = EnumFacing.getFront(lastFlow);
					dirs.remove(prefer);
					dirs.add(prefer);
				}

				for(EnumFacing dir : dirs) {
					BlockPos target = pos.offset(dir);
					Block b = world.getBlockState(target).getBlock();

					if(b instanceof ICrucibleAcceptor && b != ModBlocksMachine.foundry_channel) {
						ICrucibleAcceptor acc = (ICrucibleAcceptor) b;

						if(acc.canAcceptPartialFlow(world, target.getX(), target.getY(), target.getZ(), dir.getOpposite(), new MaterialStack<NTMMaterial>(this.type, this.amount))) {
							MaterialStack<?> left = acc.flow(world, target.getX(), target.getY(), target.getZ(), dir.getOpposite(), new MaterialStack<NTMMaterial>(this.type, this.amount));
							if(left == null) {
								this.type = null;
								this.amount = 0;
							} else {
								this.amount = left.amount;
							}
							hasOp = true;
							break;
						}
					}
				}

				if(!hasOp) {

					for(EnumFacing dir : dirs) {
						TileEntity b = world.getTileEntity(pos.offset(dir));

						if(b instanceof TileEntityFoundryChannel) {
							TileEntityFoundryChannel acc = (TileEntityFoundryChannel) b;

							if(acc.type == null || acc.type == this.type || acc.amount == 0) {
								acc.type = this.type;
								acc.lastFlow = dir.getOpposite().ordinal();

								if(world.rand.nextInt(5) == 0 || this.amount == 1) { //with only one unit of material, force the swap so it doesn't sit still
									//one chance in four of swapping both sides' amounts outright, so material spreads out and never hits the propagation cap
									int buf = this.amount;
									this.amount = acc.amount;
									acc.amount = buf;
								} else {
									//otherwise just even out the two adjacent amounts
									int diff = this.amount - acc.amount;

									if(diff > 0) {
										diff /= 2;
										this.amount -= diff;
										acc.amount += diff;
									}
								}
							}
						}
					}
				}
			}

			if(this.amount == 0) {
				this.lastFlow = 0;
				this.nextUpdate = 5;
			}
		}

		super.updateEntity();
	}

	@Override
	public int getCapacity() {
		return MaterialShapes.INGOT.q(2);
	}

	/**
	 * One chain, one material: the old version asked the node network this; no node graph here, so it sweeps the connected channels locally.
	 * Empty channels don't count — the old side wiped the type from the net the moment a channel ran dry.
	 */
	private boolean netHoldsOtherMaterial(World world, NTMMaterial material) {

		ArrayDeque<BlockPos> todo = new ArrayDeque<BlockPos>();
		Set<BlockPos> seen = new HashSet<BlockPos>();

		todo.add(this.pos);
		seen.add(this.pos);

		while(!todo.isEmpty() && seen.size() <= NET_SCAN_LIMIT) {

			BlockPos here = todo.poll();
			TileEntity te = world.getTileEntity(here);

			if(te instanceof TileEntityFoundryChannel) {
				TileEntityFoundryChannel channel = (TileEntityFoundryChannel) te;

				if(channel.amount > 0 && channel.type != null && channel.type != material)
					return true;
			}

			for(EnumFacing dir : EnumFacing.HORIZONTALS) {

				BlockPos next = here.offset(dir);
				if(seen.contains(next)) continue;

				if(world.getTileEntity(next) instanceof TileEntityFoundryChannel) {
					seen.add(next);
					todo.add(next);
				}
			}
		}

		return false;
	}

	@Override
	public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, EnumFacing side, MaterialStack<?> stack) {
		if(netHoldsOtherMaterial(world, materialOf(stack))) return false;

		return super.canAcceptPartialPour(world, x, y, z, dX, dY, dZ, side, stack);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.lastFlow = nbt.getByte("flow");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("flow", (byte) this.lastFlow);
		return nbt;
	}
}
