package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.MainRegistry;
import com.hbm.main.ServerProxy;
import com.hbm.tileentity.network.TileEntityPylonBase;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * The spool: click the first pylon to mark the start (written to NBT), click a second one and the link goes up.
 * Whether it can connect is TileEntityPylonBase.canConnect's call (same type, different pylons, long enough),
 * and both ends addConnection, so the link is stored bidirectionally in the save.
 *
 * Only the server actually does the wiring: the old version had this same shape (the client pass just swings the arm),
 * which is also why the start point gets written once on each side.
 */
public class ItemWiring extends Item {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if(player.isSneaking()) return EnumActionResult.PASS;

		ItemStack stack = player.getHeldItem(hand);

		BlockPos core = pos;
		Block block = world.getBlockState(pos).getBlock();

		if(block instanceof BlockDummyable) {
			BlockPos found = ((BlockDummyable) block).findCore(world, pos);
			if(found != null) core = found;
		}

		TileEntity tile = world.getTileEntity(core);

		if(!(tile instanceof TileEntityPylonBase)) return EnumActionResult.PASS;

		TileEntityPylonBase second = (TileEntityPylonBase) tile;
		NBTTagCompound tag = stack.getTagCompound();

		if(tag == null) {

			tag = new NBTTagCompound();
			tag.setInteger("x", core.getX());
			tag.setInteger("y", core.getY());
			tag.setInteger("z", core.getZ());
			stack.setTagCompound(tag);

			if(!world.isRemote) {
				player.sendMessage(new TextComponentString("Wire start"));
			}
		} else if(!world.isRemote) {

			BlockPos start = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
			TileEntity firstTile = world.getTileEntity(start);

			if(firstTile instanceof TileEntityPylonBase) {

				TileEntityPylonBase first = (TileEntityPylonBase) firstTile;

				switch(TileEntityPylonBase.canConnect(first, second)) {
					case 0:
						first.addConnection(core.getX(), core.getY(), core.getZ());
						second.addConnection(start.getX(), start.getY(), start.getZ());
						player.sendMessage(new TextComponentString("Wire end"));
						break;
					case 1:
						player.sendMessage(new TextComponentString("Wire error - Pylons are not the same type"));
						break;
					case 2:
						player.sendMessage(new TextComponentString("Wire error - Cannot connect to the same pylon"));
						break;
					case 3:
						player.sendMessage(new TextComponentString("Wire error - Pylon is too far away"));
						break;
				}
			} else {
				player.sendMessage(new TextComponentString("Wire error"));
			}

			stack.setTagCompound(null);
		}

		player.swingArm(EnumHand.MAIN_HAND);

		return EnumActionResult.SUCCESS;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		NBTTagCompound tag = stack.getTagCompound();

		if(tag != null) {
			list.add("Wire start x: " + tag.getInteger("x"));
			list.add("Wire start y: " + tag.getInteger("y"));
			list.add("Wire start z: " + tag.getInteger("z"));
		} else {
			list.add("Right-click poles to connect");
		}
	}

	/**
	 * While carried in hand, a "distance to start" line floats on the HUD.
	 *
	 * The old version had the client call the proxy directly; this version keeps the same door (the 1000ms duration is that two-arg overload's old default).
	 * But this version's ClientProxy doesn't draw the floating text yet (that side belongs to A2), so the call connects but nothing shows for now.
	 */
	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean inhand) {

		if(!world.isRemote) return;

		NBTTagCompound tag = stack.getTagCompound();

		if(tag != null) {
			double dist = entity.getDistance(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
			MainRegistry.proxy.displayTooltip(stack.getDisplayName() + ": " + ((int) dist) + "m", 1000, ServerProxy.ID_CABLE);
		}
	}
}
