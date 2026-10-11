package com.hbm.tileentity.bomb;

import java.util.List;

import com.hbm.blocks.bomb.BlockLandmine;
import com.hbm.sound.SoundRefs;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The landmine's fuze: only decides "when it should go off"; the explosion settles on the block side (the IBomb port).
 *
 * Two state bits set the scan behavior: waitingForPlayer marks worldgen mines — players only at first, so generated mines don't
 * blow themselves up before anyone arrives; isPrimed means freshly placed and not settled — while the placer stands inside the trigger radius it stays unprimed,
 * arming once the radius clears, so the next creature walking in is the one that steps on it. A blocked cell directly above skips the whole check round.
 */
public class TileEntityLandmine extends TileEntity implements ITickable {

	private boolean isPrimed = false;
	public boolean waitingForPlayer = false;

	@Override
	public void update() {

		if(world.isRemote) return;

		Block block = world.getBlockState(this.pos).getBlock();

		if(!(block instanceof BlockLandmine)) return;
		BlockLandmine landmine = (BlockLandmine) block;

		double range = landmine.kind.range;
		double height = landmine.kind.height;

		if(waitingForPlayer) {
			range = 25;
			height = 25;
		} else if(!isPrimed) {
			range *= 2;
			height *= 2;
		}

		BlockPos above = this.pos.up();
		if(!world.isAirBlock(above)) return;

		List<Entity> list = world.getEntitiesWithinAABB(Entity.class,
				new AxisAlignedBB(this.pos.getX() - range, this.pos.getY() - height, this.pos.getZ() - range,
						this.pos.getX() + range + 1, this.pos.getY() + height, this.pos.getZ() + range + 1));

		for(Entity entity : list) {

			//aquatic and flying insects don't count as mine-triggering creatures, same as the old version
			if(entity.isCreatureType(EnumCreatureType.WATER_CREATURE, false)) continue;
			if(entity.isCreatureType(EnumCreatureType.AMBIENT, false)) continue;

			if(waitingForPlayer) {
				if(entity instanceof EntityPlayer) {
					waitingForPlayer = false;
					return;
				}
			} else {
				if(entity instanceof EntityLivingBase) {

					if(isPrimed) {
						landmine.explode(world, this.pos);
						//pending: the old version tallied a "mines stepped on" stat here (MainRegistry.statMines); the achievement surface isn't in the tree yet
						if(entity instanceof EntityPlayer) return;
					}

					return;
				}
			}
		}

		//arms itself once the trigger radius is clear; the beep is the player's warning tone
		if(!isPrimed && !waitingForPlayer) {
			world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
					SoundRefs.resolve(new ResourceLocation("hbm:weapon.fstbmbstart")), SoundCategory.PLAYERS, 3.0F, 1.0F);
			isPrimed = true;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		isPrimed = nbt.getBoolean("primed");
		waitingForPlayer = nbt.getBoolean("waiting");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("primed", isPrimed);
		nbt.setBoolean("waiting", waitingForPlayer);
		return nbt;
	}
}
