package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityLockableBase;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/** The lockpick kit: use it on a locked container and it casts two fake keys off that lock's bitting. */
public class ItemCounterfeitKeys extends ItemBase {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float cx, float cy, float cz) {

		if(world.isRemote)
			return EnumActionResult.PASS;

		if(!(world.getTileEntity(pos) instanceof TileEntityLockableBase))
			return EnumActionResult.PASS;

		TileEntityLockableBase locked = (TileEntityLockableBase) world.getTileEntity(pos);

		if(!locked.isLocked())
			return EnumActionResult.PASS;

		if(!locked.cheesable) {
			player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "This lock is too elaborate for a counterfeit key to be made"));
			player.sendMessage(new TextComponentString(TextFormatting.LIGHT_PURPLE + "Perhaps there is another way around here to unlock it"));
			return EnumActionResult.PASS;
		}

		ItemStack counterfeit = new ItemStack(ModItems.key_fake);
		ItemKeyPin.setPins(counterfeit, locked.getPins());

		// the held one becomes the first key, the second goes to the inventory; only a full inventory drops it on the ground
		ItemStack spare = counterfeit.copy();
		player.inventory.setInventorySlotContents(player.inventory.currentItem, counterfeit);
		if(!player.inventory.addItemStackToInventory(spare)) {
			player.dropItem(spare, false);
		}

		player.inventoryContainer.detectAndSendChanges();
		player.swingArm(hand);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		list.add("Use on a locked container to create two counterfeit keys!");
	}
}
