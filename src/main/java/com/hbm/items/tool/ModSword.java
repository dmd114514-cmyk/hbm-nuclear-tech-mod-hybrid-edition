package com.hbm.items.tool;

import java.util.List;

import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.world.World;

/** Sticks used as weapons. Three of them, each with its own chatter, copied into the tooltips. */
public class ModSword extends ItemSword {

	public ModSword(ToolMaterial mat) {
		super(mat);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.pipe_lead) {
			list.add("I'm going to attempt a manual override on this wall.");
		}

		if(this == ModItems.reer_graar) {
			list.add("Call now!");
			list.add("555-10-3728-ZX7-INFINITE");
		}
	}
}
