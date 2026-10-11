package com.hbm.items.armor;

import java.util.List;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.ModItems;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * The bathwater: slaps poison back onto your attacker. MK-II applies wither at a higher level instead,
 * and the two are told apart by color alone — the regular jitter blue-purple, MK-II jittering yellow-green.
 */
public class ItemModBathwater extends ItemArmorMod {

	public ItemModBathwater() {
		super(ArmorModHandler.extra, true, true, true, true);
	}

	private String color() {

		if(this == ModItems.bathwater_mk2)
			return flicker(TextFormatting.GREEN, TextFormatting.YELLOW);

		return flicker(TextFormatting.BLUE, TextFormatting.LIGHT_PURPLE);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		list.add(color() + "Inflicts poison on the attacker");
		list.add("");
		super.addInformation(stack, world, list, flag);
	}

	@Override
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(color() + "  " + stack.getDisplayName() + " (Poisons attackers)");
	}

	@Override
	public void modDamage(LivingHurtEvent event, ItemStack armor) {

		if(event.getEntityLiving().world.isRemote)
			return;

		if(!(event.getSource() instanceof EntityDamageSource))
			return;

		Entity attacker = ((EntityDamageSource) event.getSource()).getTrueSource();

		if(attacker instanceof EntityLivingBase) {

			if(this == ModItems.bathwater)
				((EntityLivingBase) attacker).addPotionEffect(new PotionEffect(MobEffects.POISON, 200, 2));

			if(this == ModItems.bathwater_mk2)
				((EntityLivingBase) attacker).addPotionEffect(new PotionEffect(MobEffects.WITHER, 200, 4));
		}
	}
}
