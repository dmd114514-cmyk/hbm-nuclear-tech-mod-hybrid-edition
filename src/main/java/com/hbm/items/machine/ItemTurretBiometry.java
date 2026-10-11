package com.hbm.items.machine;

import java.util.Arrays;
import java.util.List;

import com.hbm.items.ItemBase;
import com.hbm.items.ItemSounds;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

/**
 * The turret biometry card. Right-click registers yourself; multiple players allowed, names stored under NBT's `player_i`,
 * with the count in a separate `playercount` — loops read by it, steadier than walking NBT keys.
 */
public class ItemTurretBiometry extends ItemBase {

	private static final SoundEvent BLEEP = ItemSounds.resolve("item.techBleep");

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		String[] names = getNames(stack);
		if(names == null) return;

		for(String name : names) list.add(name);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		ItemStack stack = player.getHeldItem(hand);
		addName(stack, player.getDisplayNameString());

		if(world.isRemote) {
			player.sendMessage(new TextComponentString("Added player data!"));
		}

		world.playSound(null, player.posX, player.posY, player.posZ, BLEEP, SoundCategory.PLAYERS, 1.0F, 1.0F);
		player.swingArm(hand);

		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	public static String[] getNames(ItemStack stack) {

		if(stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			return null;
		}

		String[] names = new String[stack.getTagCompound().getInteger("playercount")];

		for(int i = 0; i < names.length; i++) {
			names[i] = stack.getTagCompound().getString("player_" + i);
		}

		return names.length == 0 ? null : names;
	}

	public static void addName(ItemStack stack, String name) {

		if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());

		String[] names = getNames(stack);
		int count = 0;

		if(names != null && Arrays.asList(names).contains(name)) return;
		if(names != null) count = names.length;

		stack.getTagCompound().setInteger("playercount", count + 1);
		stack.getTagCompound().setString("player_" + count, name);
	}

	public static void clearNames(ItemStack stack) {

		if(stack.getTagCompound() == null) stack.setTagCompound(new NBTTagCompound());

		stack.getTagCompound().setInteger("playercount", 0);
	}
}
