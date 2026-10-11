package com.hbm.items.armor;

import java.util.List;

import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * The red room cards: only helmet and chest slots, never in the creative tab, loot-only.
 *
 * Each card's behavior hangs on item identity — the AOS card belongs to the gun side (only the tooltip here),
 * while QOS gives a one-in-three chance of negating a hit entirely, with a crunch.
 */
public class ItemModCard extends ItemArmorMod {

	public ItemModCard() {
		super(ArmorModHandler.helmet_only, true, true, false, false);
		this.setCreativeTab(null);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if(this == ModItems.card_aos) {
			list.add(TextFormatting.RED + "Top of the line!");
			list.add(TextFormatting.RED + "Guns now have a 33% chance to not consume ammo.");
		}
		if(this == ModItems.card_qos) {
			list.add(TextFormatting.RED + "Power!");
			list.add(TextFormatting.RED + "Adds a 33% chance to tank damage with no cap.");
		}
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.RED + stack.getDisplayName());
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(this != ModItems.card_qos)
			return;

		if(!(event.getEntityLiving() instanceof EntityPlayer))
			return;

		if(event.getEntityLiving().getRNG().nextInt(3) != 0)
			return;

		EntityPlayer player = (EntityPlayer) event.getEntityLiving();

		HbmPlayerProps.plink(player, SoundEvents.ENTITY_ITEM_BREAK, 0.5F, 1.0F + player.getRNG().nextFloat() * 0.5F);

		event.setAmount(0);
		event.setCanceled(true);
	}
}
