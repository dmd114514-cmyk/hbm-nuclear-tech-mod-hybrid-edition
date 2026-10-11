package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.hbm.blocks.machine.ModBlocksMachine;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.NTMMaterial;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.material.MaterialStack;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * The casting basin: a tank that holds a whole lump of material, pours it into the next tank itself,
 * picking a receiver from the four horizontal directions that "doesn't take the same channel type"; failing that, splits evenly with horizontally adjacent tanks.
 */
public class TileEntityFoundryTank extends TileEntityFoundryBase {

	public int nextUpdate;

	@Override
	public void updateEntity() {

		if(!world.isRemote) {

			if(this.type == null && this.amount != 0) {
				this.amount = 0;
			}

			nextUpdate--;

			if(nextUpdate <= 0 && this.amount > 0 && this.type != null) {

				boolean hasOp = false;
				nextUpdate = world.rand.nextInt(6) + 5;

				TileEntity te = world.getTileEntity(pos.down());

				if(te instanceof TileEntityFoundryTank) {
					TileEntityFoundryTank tank = (TileEntityFoundryTank) te;

					if((tank.type == null || tank.type == this.type) && tank.amount < tank.getCapacity()) {
						tank.type = this.type;
						int toFill = Math.min(this.amount, tank.getCapacity() - tank.amount);
						this.amount -= toFill;
						tank.amount += toFill;
						hasOp = true;
					}
				}

				List<EnumFacing> dirs = new ArrayList<EnumFacing>(Arrays.asList(EnumFacing.HORIZONTALS));
				Collections.shuffle(dirs, world.rand);

				if(!hasOp) {

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
				}

				if(!hasOp) {

					for(EnumFacing dir : dirs) {
						TileEntity b = world.getTileEntity(pos.offset(dir));

						if(b instanceof TileEntityFoundryTank) {
							TileEntityFoundryTank acc = (TileEntityFoundryTank) b;

							if(acc.type == null || acc.type == this.type || acc.amount == 0) {
								acc.type = this.type;

								if(world.rand.nextInt(5) == 0) {
									//one chance in four of swapping both sides' amounts outright, so material spreads out and never hits the propagation cap
									int buf = this.amount;
									this.amount = acc.amount;
									acc.amount = buf;
								} else {
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
		}

		super.updateEntity();
	}

	@Override
	public int getCapacity() {
		return MaterialShapes.BLOCK.q(4);
	}
}
