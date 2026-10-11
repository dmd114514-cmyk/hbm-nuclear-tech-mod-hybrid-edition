package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * The landing pads: boots slot, scaling fall damage by a ratio.
 *
 * The static tier also does one thing — recharging battery-carrying armor while walking, at half the drain rate,
 * falling back to consumption/40 when drain is 0, so even a zero-drain suit isn't drained uselessly.
 */
public class ItemModPads extends ItemArmorMod {

	float damageMod;

	public ItemModPads(float damageMod) {
		super(ArmorModHandler.boots_only, false, false, false, true);
		this.damageMod = damageMod;
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(damageMod != 1F)
			list.add(TextFormatting.RED + "-" + Math.round((1F - damageMod) * 100) + "% fall damage");

		if(this == ModItems.pads_static)
			list.add(TextFormatting.DARK_PURPLE + "Passively charges electric armor when walking");

		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {

		if(this == ModItems.pads_static)
			list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (-" + Math.round((1F - damageMod) * 100) + "% fall dmg / passive charge)");
		else
			list.add(TextFormatting.DARK_PURPLE + "  " + stack.getDisplayName() + " (-" + Math.round((1F - damageMod) * 100) + "% fall dmg)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(event.getSource() == DamageSource.FALL)
			event.setAmount(event.getAmount() * damageMod);
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(entity.world.isRemote || this != ModItems.pads_static || !(entity instanceof EntityPlayer))
			return;

		EntityPlayer player = (EntityPlayer) entity;

		if(player.distanceWalkedModified == player.prevDistanceWalkedModified)
			return;

		if(!ArmorFSB.hasFSBArmorIgnoreCharge(player))
			return;

		for(int i = 0; i < 4; i++) {

			ItemStack stack = player.inventory.armorInventory.get(i);

			if(stack.isEmpty() || !(stack.getItem() instanceof ArmorFSBPowered))
				continue;

			ArmorFSBPowered powered = (ArmorFSBPowered) stack.getItem();

			long charge = powered.drain / 2;

			if(charge == 0)
				charge = powered.consumption / 40;

			long power = Math.min(powered.getMaxCharge(stack), powered.getCharge(stack) + charge);
			powered.setCharge(stack, power);
		}
	}
}
