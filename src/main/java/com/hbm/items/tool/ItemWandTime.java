package com.hbm.items.tool;

import java.util.List;

import com.hbm.util.AstronomyUtil;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

/**
 * The time wand: scales the celestial time multiplier for everyone (sneak halves, plain doubles, floor 1), one charge only.
 *
 * It's a creative-only piece mutating a static field directly — so "probably won't work on a server" isn't a joke,
 * and a client-side change never syncs to anyone.
 */
public class ItemWandTime extends Item {

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add("Creative-only item");
		list.add(TextFormatting.ITALIC + "\"Wibbly wobbly, timey-wimey... stuff\"");
		list.add("Probably doesn't work on servers");
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float fx, float fy, float fz) {

		if(world.isRemote)
			return EnumActionResult.SUCCESS;

		ItemStack stack = player.getHeldItem(hand);

		if(player.isSneaking()) {
			AstronomyUtil.TIME_MULTIPLIER /= 2;

			if(AstronomyUtil.TIME_MULTIPLIER < 1)
				AstronomyUtil.TIME_MULTIPLIER = 1;
		} else {
			AstronomyUtil.TIME_MULTIPLIER *= 2;
		}

		player.sendMessage(new TextComponentString("Celestial Time Multiplier set to: " + AstronomyUtil.TIME_MULTIPLIER));

		return EnumActionResult.SUCCESS;
	}
}
