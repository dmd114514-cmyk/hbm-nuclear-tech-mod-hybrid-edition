package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/** The ink sac: small-chance damage negation, dropping a few flowers when it fires. */
public class ItemModInk extends ItemArmorMod {

	public ItemModInk() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(TextFormatting.LIGHT_PURPLE + "10% chance to nullify damage");
		list.add(TextFormatting.LIGHT_PURPLE + "Flowers!");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.LIGHT_PURPLE + "  " + stack.getDisplayName() + " (10% chance to nullify damage)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		World world = event.getEntity().world;

		if(world.rand.nextInt(10) == 0) {
			event.setAmount(0);

			if(!world.isRemote) {

				if(world.rand.nextInt(10) == 0)
					event.getEntity().entityDropItem(new ItemStack(Blocks.YELLOW_FLOWER), 1.0F);

				event.getEntity().entityDropItem(new ItemStack(Blocks.RED_FLOWER, 1, world.rand.nextInt(9)), 1.0F);
			}
		}
	}
}
