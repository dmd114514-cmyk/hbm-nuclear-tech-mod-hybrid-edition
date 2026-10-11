package com.hbm.items.bomb;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemN2 extends Item {

	@Override
	public void addInformation(ItemStack itemstack, net.minecraft.world.World world, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
		list.add(I18nUtil.resolveKey("item.bomb_part.used_in"));
	}
}
