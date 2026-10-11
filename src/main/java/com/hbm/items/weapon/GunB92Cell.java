package com.hbm.items.weapon;

import java.util.List;

import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * The B92's backup cell: carried in inventory, it siphons 1 power per tick from a B92 in your bag, and at 25 it swaps to the durability-1 look.
 *
 * The charge rides NBT's energy and it drains "the B92's stored power", not item durability — which makes it a slowly-armed bomb,
 * Those two tooltip lines aren't jokes.
 */
public class GunB92Cell extends Item {

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {

		if(!(entity instanceof EntityPlayer) || getPower(stack) >= 25)
			return;

		EntityPlayer player = (EntityPlayer) entity;

		for(int i = 0; i < player.inventory.mainInventory.size(); i++) {

			ItemStack other = player.inventory.mainInventory.get(i);

			if(other == null || other.isEmpty() || other.getItem() != ModItems.gun_b92)
				continue;

			int p = getPower(other);

			if(p > 1) {
				setPower(other, p - 1);
				setPower(stack, getPower(stack) + 1);

				if(getPower(stack) == 25)
					stack.setItemDamage(1);

				return;
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add("Draws energy from the B92, allowing you to");
		list.add("reload it an additional 25 times.");
		list.add("The cell will permanently hold its charge,");
		list.add("it is not meant to be used as a battery enhancement");
		list.add("for the B92, but rather as a bomb.");
		list.add("");
		list.add("Charges: " + getPower(stack) + " / 25");
	}

	private static int getPower(ItemStack stack) {

		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return 0;
		}

		return stack.getTagCompound().getInteger("energy");
	}

	private static void setPower(ItemStack stack, int i) {

		if(stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("energy", i);
	}

	public static ItemStack getFullCell() {

		ItemStack stack = new ItemStack(ModItems.gun_b92_ammo, 1, 1);
		setPower(stack, 25);

		return stack.copy();
	}
}
