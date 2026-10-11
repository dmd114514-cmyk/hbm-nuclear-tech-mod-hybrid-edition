package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;
import com.hbm.items.ModItems;
import com.hbm.util.ItemStackUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

/**
 * Base for toolkit-style items: right-click dumps the stack stored in NBT into the inventory, and it loses one of itself.
 *
 * 1.12.2's getContainerItem no longer returns null but an empty stack, so the null check became isEmpty;
 * every handed-out piece must be copied, or the instance read from NBT goes straight into the inventory and edits reach back into the source.
 */
public class ItemKitNBT extends ItemBase {

	private static final SoundEvent UNPACK = ItemSounds.resolve("item.unpack");

	public ItemKitNBT() {
		this.setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);

		ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(stack);

		if(stacks != null) {
			for(ItemStack item : stacks) {
				if(item != null && !item.isEmpty()) {
					player.inventory.addItemStackToInventory(item.copy());
				}
			}
		}

		ItemStack container = stack.getItem().getContainerItem(stack);

		stack.shrink(1);

		if(!container.isEmpty()) {

			if(stack.getCount() > 0) {
				player.inventory.addItemStackToInventory(container.copy());
			} else {
				stack = container.copy();
			}
		}

		if(!world.isRemote) {
			world.playSound(null, player.posX, player.posY, player.posZ, UNPACK, SoundCategory.PLAYERS, 1.0F, 1.0F);
		}

		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(stack);

		if(stacks != null) {

			list.add("Contains:");

			for(ItemStack item : stacks) {
				if(item == null || item.isEmpty()) continue;
				list.add("-" + item.getDisplayName() + (item.getCount() > 1 ? (" x" + item.getCount()) : ""));
			}
		}
	}

	public static ItemStack create(ItemStack... contents) {
		ItemStack stack = new ItemStack(ModItems.kit_custom);
		stack.setTagCompound(new NBTTagCompound());
		ItemStackUtil.addStacksToNBT(stack, contents);

		return stack;
	}
}
